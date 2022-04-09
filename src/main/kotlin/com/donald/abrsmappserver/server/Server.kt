package com.donald.abrsmappserver.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.donald.abrsmappserver.exercise.ExerciseGenerator
import com.donald.abrsmappserver.utils.LangPref
import com.donald.abrsmappserver.utils.getJSONObjectOrNull
import com.donald.abrsmappserver.utils.getStringOrNull
import com.donald.abrsmappserver.utils.parseJSONObject
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import org.http4k.core.Method.*
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.findSingle
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.json.JSONException
import org.json.JSONObject
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*
import javax.crypto.KeyGenerator


object Server {

    private const val DATABASE_URL = "jdbc:sqlite:db/database.db"
    private const val CLIENT_ID = "453228511123-2i0jeo2td5t2cuqbi53edmd9q8nfvns1.apps.googleusercontent.com"
    private const val ISSUER_1 = "https://accounts.google.com"
    private const val ISSUER_2 = "accounts.google.com"
    private const val AUTH0_DOMAIN = "donaldmntam.us.auth0.com"
    private const val TOKEN_ISSUER = "https://amta.site"
    private const val TOKEN_VALID_PERIOD = 1 * 60 * 1000
    private const val TOKEN_INACTIVE_PERIOD = 1 * 1000
    private const val TOKEN_LEE_WAY = 1L
    private const val JSON_CONTENT_TYPE_HEADER_VALUE = "application/json; charset=utf-8"
    private const val GOOGLE_CREDENTIALS_JSON_PATH = "C:\\Users\\Donald\\IdeaProjects\\AmtaServer\\music-theory-app-1643350728268-firebase-adminsdk-i5lt2-6a0b2b0f0e.json"

    val database: Connection = run {
        println("Connecting to database")
        DriverManager.getConnection(DATABASE_URL).also {
            println("Database connected")
        }
    }

    private val generator = ExerciseGenerator(database)

    private val googleIdTokenVerifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
        .setAudience(Collections.singletonList(CLIENT_ID))
        .setIssuers(listOf(ISSUER_1, ISSUER_2))
        .build()

    private val tokenAlgorithm = run {
        val secretKey = KeyGenerator.getInstance("HmacSHA256").generateKey()
        Algorithm.HMAC256(secretKey.encoded)
    }

    private val accessTokenVerifier = JWT.require(tokenAlgorithm)
        .withIssuer(TOKEN_ISSUER)
        .acceptLeeway(TOKEN_LEE_WAY)
        .build()

    private val getUser = fun(request: Request): Response {
        val value = request.headers.findSingle("Authorization") ?: return responseNoAuth()
        val substrings = value.split(" ")
        if (substrings.size != 2 && substrings[0] != "Bearer") return responseBadAuth()

        //val token = googleIdTokenVerifier.verify(substrings[1]) ?: return responseBadGoogleToken()
        //val userId = token.payload.subject ?: return responseNullSubject()

        val token = try {
            FirebaseAuth.getInstance().verifyIdToken(substrings[1])
        } catch (e: FirebaseAuthException) {
            return responseBadIdToken()
        }
        val userId = token.uid

        val result = try {
            database.prepareStatement(
                "SELECT nickname, lang_pref FROM users " +
                        "WHERE id = ? LIMIT 1"
            ).apply { setString(1, userId) }.executeQuery()
        } catch (e: SQLException) {
            e.printStackTrace()
            return responseInternalError()
        }
        if (!result.next()) {
            return responseNoUserFound()
        }

        val nickname = try {
            result.getString("nickname")
        } catch (e: SQLException) {
            e.printStackTrace()
            return responseInternalError()
        } ?: return responseDbNullNickname()

        val langPref: String = try {
            result.getString("lang_pref")
        } catch (e: SQLException) {
            e.printStackTrace()
            return responseInternalError()
        } ?: return responseDbNullLangPref()

        val response = JSONObject().apply {
            put("nickname", nickname)
            put("lang_pref", langPref)
            put("access_token", generateAccessToken())
        }

        return Response(OK)
            .header("Content-Type", JSON_CONTENT_TYPE_HEADER_VALUE)
            .body(response.toString())
    }

    private val postUser = fun(request: Request): Response {
        val value = request.headers.findSingle("Authorization") ?: return responseNoAuth()
        val substrings = value.split(" ")
        if (substrings.size != 2 && substrings[0] != "Bearer") return responseBadAuth()

        val token = try {
            FirebaseAuth.getInstance().verifyIdToken(substrings[1])
        } catch (e: FirebaseAuthException) {
            return responseBadIdToken()
        }
        val userId = token.uid


        val body = try {
            JSONObject(request.bodyString())
        } catch (e: JSONException) {
            e.printStackTrace()
            return responseBadJson()
        }

        val nickname = try {
            body.getString("nickname")
        } catch (e: JSONException) {
            e.printStackTrace()
            return responseJsonNoNickname()
        } ?: return responseJsonNullNickname()

        val langPref = try {
            body.getString("lang_pref")
        } catch (e: JSONException) {
            e.printStackTrace()
            return responseJsonNoLangPref()
        } ?: return responseJsonNullLangPref()

        try {
            database.prepareStatement(
                "INSERT INTO users (id, nickname, lang_pref)" +
                        "VALUES (?, ?, ?)"
            ).apply { setString(1, userId); setString(2, nickname); setString(3, langPref) }.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
            return responseInternalError()
        }

        val response = JSONObject().apply {
            put("access_token", generateAccessToken())
        }

        return Response(CREATED)
            .body(response.toString())
    }

    private val deleteUser = fun(request: Request): Response {
        val value = request.headers.findSingle("Authorization") ?: return responseNoAuth()
        val substrings = value.split(" ")
        if (substrings.size != 2 && substrings[0] != "Bearer") return responseBadAuth()

        val token = try {
            FirebaseAuth.getInstance().verifyIdToken(substrings[1])
        } catch (e: FirebaseAuthException) {
            return responseBadIdToken()
        }
        val userId = token.uid

        val result = try {
            database.prepareStatement("DELETE FROM users WHERE id= ?")
                .apply { setString(1, userId) }.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
            return responseInternalError()
        }

        if (result == 0) return responseNoUserFound()

        return Response(OK)
    }

    private val getAccessToken = fun(request: Request): Response {
        val value = request.headers.findSingle("Authorization") ?: return responseNoAuth()
        val substrings = value.split(" ")
        if (substrings.size != 2 && substrings[0] != "Bearer") return responseBadAuth()

        try {
            FirebaseAuth.getInstance().verifyIdToken(substrings[1])
        } catch (e: FirebaseAuthException) {
            return responseBadIdToken()
        }

        val response = JSONObject().apply {
            put("access_token", generateAccessToken())
        }

        return Response(OK)
            .header("Content-Type", JSON_CONTENT_TYPE_HEADER_VALUE)
            .body(response.toString())
    }

    private val getExercise = fun(request: Request): Response {
        val value = request.headers.findSingle("Authorization") ?: return responseNoAuth()
        val substrings = value.split(" ")
        if (substrings.size != 2 && substrings[0] != "Bearer") return responseBadAuth()

        try {
            accessTokenVerifier.verify(substrings[1])
        } catch (e: JWTVerificationException) {
            e.printStackTrace()
            return responseBadAccessToken()
        }

        val body = parseJSONObject(request.bodyString()) ?: return responseBadJson()

        val langPrefString = body.getStringOrNull("lang_pref") ?: return responseNoLangPref()
        val langPref = LangPref.fromStringOrNull(langPrefString) ?: return responseBadLangPref()
        val bundle = ResourceBundle.getBundle("resources.strings", Locale(langPref.string))

        val exercise = when (body.getStringOrNull("mode")) {
            "test" -> {
                generator.generateTest(bundle)
            }
            "practice" -> {
                val options = body.getJSONObjectOrNull("options") ?: return responseNoOptions()
                generator.generatePractice(bundle, options) ?: return responseBadOptions()
            }
            else -> {
                return responseBadExerciseMode()
            }
        }

        return Response(OK)
            .header("Content-Type", JSON_CONTENT_TYPE_HEADER_VALUE)
            .body(exercise.toJson().toString())
    }

    private val getImage = fun(request: Request): Response {
        val value = request.headers.findSingle("Authorization") ?: return responseNoAuth()
        val substrings = value.split(" ")
        if (substrings.size != 2 && substrings[0] != "Bearer") return responseBadAuth()

        try {
            accessTokenVerifier.verify(substrings[1]) ?: return responseBadAccessToken()
        } catch (e: JWTVerificationException) {
            e.printStackTrace()
            return responseBadAccessToken()
        }

        val image = try {
            FileInputStream("./images/${request.path("img")}.png")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return responseNoImage()
        }

        return Response(OK)
            .body(image)
    }

    private val routes: RoutingHttpHandler = routes(
        "/user"         bind GET    to getUser,
        "/user"         bind POST   to postUser,
        "/user"         bind DELETE to deleteUser,

        "/access-token" bind GET    to getAccessToken,
        "/exercise"     bind GET    to getExercise,
        "/exercise"     bind POST   to getExercise, // TODO: WEIRD
        "/images/{img}" bind GET    to getImage,
        "/{message}"    bind GET    to { Response(OK).body(it.path("message") ?: "Send a message!") },
    )

    fun run() {
        val options = FirebaseOptions.builder()
            .setCredentials(
                GoogleCredentials.fromStream(FileInputStream(GOOGLE_CREDENTIALS_JSON_PATH))
                //GoogleCredentials.getApplicationDefault()
            )
            //.setDatabaseUrl("https://<DATABASE_NAME>.firebaseio.com/")
            .build()
        FirebaseApp.initializeApp(options)
        routes.asServer(Undertow(80)).start()
        /*
        for (fifths in INTERVAL_FIFTHS_RANGE) {
            val interval = Interval(fifths, 0)
            println("""
                pitch fifths = $fifths
                interval = ${interval.quality} ${interval.number}
            """.trimIndent())
        }

         */
    }

    private fun responseNoAuth(): Response {
        println("No authorization header detected")
        return Response(UNAUTHORIZED)
    }

    private fun responseBadAuth(): Response {
        println("Bad authorization header detected")
        return Response(BAD_REQUEST)
    }

    private fun responseBadGoogleToken(): Response {
        println("Bad Google id token detected")
        return Response(UNAUTHORIZED)
    }

    private fun responseNullSubject(): Response {
        println("Null subject from Google id token detected")
        return Response(BAD_REQUEST)
    }

    private fun responseInternalError(): Response {
        println("Internal error detected")
        return Response(INTERNAL_SERVER_ERROR)
    }

    private fun responseNoUserFound(): Response {
        println("No user found")
        return Response(NO_CONTENT)
    }

    private fun responseDbNullNickname(): Response {
        println("Null nickname from database detected")
        return Response(INTERNAL_SERVER_ERROR)
    }

    private fun responseDbNullLangPref(): Response {
        println("Null language preference from database detected")
        return Response(INTERNAL_SERVER_ERROR)
    }

    private fun responseBadAccessToken(): Response {
        println("Bad access token detected")
        return Response(UNAUTHORIZED)
    }

    private fun responseBadJson(): Response {
        println("Bad Json detected")
        return Response(BAD_REQUEST)
    }

    private fun responseJsonNoNickname(): Response {
        println("No nickname from Json detected")
        return Response(BAD_REQUEST)
    }

    private fun responseJsonNullNickname(): Response {
        println("Null nickname from Json detected")
        return Response(BAD_REQUEST)
    }

    private fun responseJsonNoLangPref(): Response {
        println("No language preference from Json detected")
        return Response(BAD_REQUEST)
    }

    private fun responseJsonNullLangPref(): Response {
        println("Null language preference from Json detected")
        return Response(BAD_REQUEST)
    }

    private fun responseBadOptions(): Response {
        println("Exercise bad options detected")
        return Response(BAD_REQUEST)
    }

    private fun responseNoLangPref(): Response {
        println("No language preference detected")
        return Response(BAD_REQUEST)
    }

    private fun responseBadLangPref(): Response {
        println("Bad language preference detected")
        return Response(BAD_REQUEST)
    }

    private fun responseNoOptions(): Response {
        println("No options detected")
        return Response(BAD_REQUEST)
    }

    private fun responseBadGroupOptions(): Response {
        println("Bad group options detected")
        return Response(BAD_REQUEST)
    }

    private fun responseBadExerciseMode(): Response {
        println("Bad exercise mode detected")
        return Response(BAD_REQUEST)
    }

    private fun responseBadIdToken(): Response {
        println("Bad id token detected")
        return Response(UNAUTHORIZED)
    }

    private fun responseNoImage(): Response {
        println("No image detected")
        return Response(NOT_FOUND)
    }

    private fun generateAccessToken(): String {
        println("Token generated.")
        val issuedAt = Date()
        val expiresAt = Date(issuedAt.time + TOKEN_VALID_PERIOD)
        val notBefore = Date(issuedAt.time + TOKEN_INACTIVE_PERIOD)
        return JWT.create()
            .withIssuer(TOKEN_ISSUER)
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            .withNotBefore(notBefore)
            .sign(tokenAlgorithm)
    }

}