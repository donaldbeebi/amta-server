package com.donald.abrsmappserver.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.donald.abrsmappserver.exercise.ExerciseGenerator
import com.donald.abrsmappserver.utils.*
import com.donald.abrsmappserver.utils.option.tryGetPracticeOptions
import com.donald.abrsmappserver.utils.user.Profile
import com.donald.abrsmappserver.utils.user.Profile.LangPref.Companion.tryGetProfileLangPref
import com.donald.abrsmappserver.utils.user.Profile.Type.Companion.tryGetProfileType
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.StorageOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import com.google.firebase.cloud.FirestoreClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method.*
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CONFLICT
import org.http4k.core.Status.Companion.CREATED
import org.http4k.core.Status.Companion.FOUND
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
import org.http4k.serverless.AppLoader
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*


object Server : AppLoader {
    private const val SQLITE_URL = "jdbc:sqlite:db/database.db"
    private const val CLIENT_ID = "453228511123-2i0jeo2td5t2cuqbi53edmd9q8nfvns1.apps.googleusercontent.com"
    private const val ISSUER_1 = "https://accounts.google.com"
    private const val ISSUER_2 = "accounts.google.com"
    private const val TOKEN_ISSUER = "https://amta.site"
    private const val TOKEN_VALID_PERIOD = 60 * 60 * 1000
    private const val TOKEN_INACTIVE_PERIOD = 1 * 1000
    private const val TOKEN_LEE_WAY = 1L
    private const val JSON_CONTENT_TYPE_HEADER_VALUE = "application/json; charset=utf-8"
    private const val GOOGLE_CREDENTIALS_JSON_PATH = "C:\\Users\\Donald\\IdeaProjects\\AmtaServer\\music-theory-app-1643350728268-firebase-adminsdk-2jagt-550c99dc93.json"
    private const val IMAGE_GOOGLE_CLOUD_STORAGE_BUCKET_NAME = "music-theory-app-1643350728268.appspot.com"

    private const val DEBUG_TEST_POINT_COST = 50
    private const val DEBUG_PRACTICE_POINT_COST = 5

    val sqliteDatabase: Connection = run {
        println("Connecting to database")
        DriverManager.getConnection(SQLITE_URL).also {
            println("Database connected")
        }
    }

    private val generator = ExerciseGenerator(sqliteDatabase)

    private val googleIdTokenVerifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
        .setAudience(Collections.singletonList(CLIENT_ID))
        .setIssuers(listOf(ISSUER_1, ISSUER_2))
        .build()

    private val tokenAlgorithm = run {
        //val secretKey = KeyGenerator.getInstance("HmacSHA256").generateKey()
        //Algorithm.HMAC256(secretKey.encoded)
        val signatureBase64 = JSONObject(File("./secret.json").readBytes().toString(StandardCharsets.UTF_8)).getString("secret")
        Algorithm.HMAC256(Base64.getDecoder().decode(signatureBase64))
    }

    private val accessTokenVerifier = JWT.require(tokenAlgorithm)
        .withIssuer(TOKEN_ISSUER)
        .acceptLeeway(TOKEN_LEE_WAY)
        .build()

    init {
        println("Initializing server")
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.getApplicationDefault())
            //.setCredentials(GoogleCredentials.fromStream(FileInputStream(GOOGLE_CREDENTIALS_JSON_PATH)))
            .setProjectId("music-theory-app-1643350728268")
            .build()
        FirebaseApp.initializeApp(options)
    }

    private val firestore = FirestoreClient.getFirestore()

    private val checkIfUserExists = fun(request: Request): Response {
        val uid = request.query("uid") ?: return responseNoUidProvided()
        return if (userExists(uid)) Response(OK)
        else Response(NO_CONTENT)
    }

    private val getUser = fun(request: Request): Response {
        val token = tryExtractAccessToken(request) otherwise { errorResponse -> return errorResponse }
        val uid = token.uid ?: return responseNoUidClaim()
        val profile = tryGetUser(uid) otherwise { errorResponse -> return errorResponse }
        val response = JSONObject().apply {
            put("profile", profile.toJson())
        }

        return Response(OK)
            .header("Content-Type", JSON_CONTENT_TYPE_HEADER_VALUE)
            .body(response.toString()).also { println("Sending profile with response: $response") }
    }

    private val postUser = fun(request: Request): Response {
        val token = tryExtractFirebaseToken(request) otherwise { errorResponse -> return errorResponse }

        val body = parseJsonObject(request.bodyString()) ?: return responseBadJson()
        val nickname = body.tryGetString("nickname")
            .otherwise { return responseBadProfileNickname() }
            ?: return responseBadProfileNickname()
        val langPref = body.tryGetProfileLangPref("lang_pref")
            .otherwise { return responseBadProfileLangPref() }
            ?: return responseBadProfileLangPref()
        val type = body.tryGetProfileType("type")
            .otherwise { return responseBadProfileType() }
            ?: return responseBadProfileType()

        val initialPoints = 0

        /*try {
            sqliteDatabase.prepareStatement("""
                INSERT INTO users (id, nickname, lang_pref, type, points)
                VALUES (?, ?, ?, ?, ?)
            """.trimMargin()).apply {
                setString(1, token.uid)
                setString(2, nickname)
                setProfileLangPref(3, langPref)
                setProfileType(4, type)
                setInt(5, initialPoints)
            }.executeUpdate()
        } catch (e: SQLException) {
            return responseSqlException(e)
        }*/

        val profile = Profile(token.uid, nickname, langPref, type, initialPoints)
        tryAddUser(profile) otherwise { errorResponse -> return errorResponse }

        val response = JSONObject().apply {
            put("profile", profile.toJson())
        }

        return Response(CREATED)
            .body(response.toString())
    }

    private val deleteUser = fun(request: Request): Response {
        val uid = tryExtractFirebaseToken(request).otherwise { return it }.uid

        /*val result = try {
            sqliteDatabase.prepareStatement("DELETE FROM users WHERE id = ?")
                .apply { setString(1, userId) }.executeUpdate()
        } catch (e: SQLException) {
            return responseSqlException(e)
        }

        if (result == 0) return responseNoUserFound()*/
        tryDeleteUser(uid) otherwise { errorResponse -> return errorResponse }
        return Response(OK)
    }



    private val debug_giveUserPoints = fun(request: Request): Response {
        val token = tryExtractAccessToken(request) otherwise { return it }
        val uid = token.uid ?: return responseNoUidClaim()

        /*val queryResult = try {
            sqliteDatabase.prepareStatement("""
                SELECT points FROM users
                WHERE id = ?
            """.trimIndent()).apply {
                setString(1, uid)
            }.executeQuery()
        } catch (e: SQLException) {
            return responseSqlException(e)
        }

        if (!queryResult.next()) return responseNoUserFound()

        val oldPoints = queryResult.tryGetInt("points")
            .otherwise { return responseSqlException(it) }
            ?: return responseDbNullProfilePoints()
        val newPoints = oldPoints + 100

        val updateResult = try {
            sqliteDatabase.prepareStatement("""
                UPDATE users
                SET points = ?
                WHERE id = ?
            """.trimIndent()).apply {
                setInt(1, newPoints)
                setString(2, uid)
            }.executeUpdate()
        } catch (e: SQLException) {
            return responseSqlException(e)
        }

        if (updateResult == 0) return responseNoUserFound()
        else if (updateResult != 1) return responseUnexpectedRowReturned(updateResult)*/
        val pointsGiven = 100
        val newPoints = tryAddPoints(uid, pointsToAdd = pointsGiven) otherwise { errorResponse -> return errorResponse }

        return Response(OK)
            .body(JSONObject().apply { put("points", newPoints) }.toString())
    }



    private val getAccessToken = fun(request: Request): Response {
        val idToken = tryExtractFirebaseToken(request) otherwise { errorResponse -> return errorResponse }

        if (!userExists(idToken.uid)) return responseNoUserFound()

        val response = JSONObject().apply {
            put("access_token", generateAccessToken(idToken.uid))
        }

        return Response(OK)
            .header("Content-Type", JSON_CONTENT_TYPE_HEADER_VALUE)
            .body(response.toString().also { println("Access token response: ${response.toString()}") })
    }



    private val getExercise = fun(request: Request): Response {
        val token = tryExtractAccessToken(request) otherwise { errorResponse -> return errorResponse }
        val uid = token.uid ?: return responseNoUidClaim()

        val body = parseJsonObject(request.bodyString()) ?: return responseBadJson()

        val langPref = body.tryGetProfileLangPref("lang_pref")
            .otherwise { return responseBadProfileLangPref() }
            ?: return responseBadProfileLangPref()
        val bundle = ResourceBundle.getBundle("resources.strings", Locale(langPref.code))

        val mode = body.tryGetString("mode")
            .otherwise { return responseBadExerciseMode() }
            ?: return responseBadExerciseMode()

        /*val profile = run {
            val result = sqliteDatabase.prepareStatement("""
                SELECT nickname, lang_pref, type, points FROM users
                WHERE id = ?
            """.trimIndent()).apply {
                setString(1, uid)
            }.executeQuery()
            if (!result.next()) return responseNoUserFound()
            result.tryGetProfile(uid) otherwise { return responseSqlException(it) }
        }*/

        val exercise = when (mode) {
            "test" -> {
                //tryDeductPoints(uid, DEBUG_TEST_POINT_COST) otherwise { badResponse -> return badResponse }
                generator.generateTest(bundle)
            }
            "practice" -> {
                val options = body.tryGetPracticeOptions("options")
                    .otherwise { it.printStackTrace(); return responseBadOptions() }
                    ?: return responseBadOptions()
                //val cost = options.countCost(costPerQuestion = DEBUG_PRACTICE_POINT_COST)
                //tryDeductPoints(uid, cost) otherwise { badResponse -> return badResponse }
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

    /*private val debug_getExercise = fun(request: Request): Response {
        val body = parseJsonObject(request.bodyString()) ?: return responseBadJson()

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
    }*/



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

        /*val image = try {
            FileInputStream("./images/${request.path("img")}.png").readBytes()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return responseNoSuchImageFound()
        }*/

        val imageName = request.path("img") ?: return responseNoImageSpecified()
        val blob = StorageOptions.getDefaultInstance().service.get(IMAGE_GOOGLE_CLOUD_STORAGE_BUCKET_NAME).get("images/$imageName.png") ?: return responseNoSuchImageFound()
        val image = blob.getContent()
        println("Successfully sent image")
        return Response(OK)
            .body(ByteArrayInputStream(image))//ByteArrayInputStream(image))
            .header("Content-Type", "image/png")
            .header("Content-Length", image.size.toString())//bytes.size.toString())
    }

    private val debug_getImage = fun(request: Request): Response {
        val imageName = request.path("img") ?: return responseNoImageSpecified()
        val point1 = System.currentTimeMillis()
        val blob = StorageOptions.getDefaultInstance().service.get(IMAGE_GOOGLE_CLOUD_STORAGE_BUCKET_NAME).get("images/$imageName.png") ?: return responseNoSuchImageFound()
        val point2 = System.currentTimeMillis()
        val bytes = blob.getContent()
        val point3 = System.currentTimeMillis()
        println("Successfully sent image")
        return Response(OK)
            .body(ByteArrayInputStream(bytes))
            .header("Content-Type", "image/png")
            .header("Content-Length", bytes.size.toString()).also {
                val point4 = System.currentTimeMillis()
                println("""
                    image name: $imageName
                    getting blob took: ${point2 - point1} ms
                    getting content from blob took: ${point3 - point2} ms
                    producing response took: ${point4 - point3} ms
                """.trimIndent())
            }
    }



    private val routes: RoutingHttpHandler = routes(
        //"/log-in"       bind GET    to logIn,
        "/user-exists"  bind GET    to checkIfUserExists,
        "/user"         bind GET    to getUser,
        "/user"         bind POST   to postUser,
        "/user"         bind DELETE to deleteUser,

        "/access-token" bind GET    to getAccessToken,

        "/exercise"     bind GET    to getExercise,
        "/exercise"     bind POST   to getExercise, // TODO: WEIRD
        //"/debug_exercise" bind GET to debug_getExercise,

        "/images/{img}" bind GET    to getImage,
        "/debug_images/{img}" bind GET to debug_getImage,

        "/debug_free-points" bind POST to debug_giveUserPoints,

        "/{message}"    bind GET    to { Response(OK).body(it.path("message") ?: "Send a message!") },
    )

    fun run() {
        routes.asServer(Undertow(80)).start()
    }

    override fun invoke(env: Map<String, String>): HttpHandler {
        return routes
    }

    private fun responseNoAuth(): Response {
        println("No authorization header detected")
        return Response(UNAUTHORIZED)
            .body("No authorization header found")
    }

    private fun responseBadAuth(): Response {
        println("Bad authorization header detected")
        return Response(BAD_REQUEST)
            .body("The authorization header is invalid")
    }

    // TODO: MAKE THIS MORE SPECIFIC TO AID DEBUGGING
    /*private fun responseInternalError(): Response {
        println("Internal error detected")
        return Response(INTERNAL_SERVER_ERROR)
    }*/

    private fun responseNoUserFound(): Response {
        println("No user found")
        return Response(NO_CONTENT)
            .body("No such user is found")
    }

    private fun responseDbNullProfileNickname(): Response {
        println("Null nickname from database detected")
        return Response(INTERNAL_SERVER_ERROR)
    }

    private fun responseDbNullProfileLangPref(): Response {
        println("Null language preference from database detected")
        return Response(INTERNAL_SERVER_ERROR)
    }

    private fun responseBadAccessToken(): Response {
        println("Bad access token detected")
        return Response(UNAUTHORIZED)
            .body("Access token is invalid")
    }

    private fun responseBadJson(): Response {
        println("Bad Json detected")
        return Response(BAD_REQUEST)
            .body("Json is invalid")
    }

    private fun responseBadOptions(): Response {
        println("Exercise bad options detected")
        return Response(BAD_REQUEST)
            .body("Practice options are invalid")
    }

    private fun responseNoLangPref(): Response {
        println("No language preference detected")
        return Response(BAD_REQUEST)
            .body("No language preference is provided")
    }

    private fun responseBadLangPref(): Response {
        println("Bad language preference detected")
        return Response(BAD_REQUEST)
            .body("The language preference provided is invalid")
    }

    private fun responseNoOptions(): Response {
        println("No options detected")
        return Response(BAD_REQUEST)
            .body("No practice options are provided")
    }

    private fun responseBadExerciseMode(): Response {
        println("Bad exercise mode detected")
        return Response(BAD_REQUEST)
            .body("The exercise mode is invalid")
    }

    private fun responseBadIdToken(): Response {
        println("Bad id token detected")
        return Response(UNAUTHORIZED)
            .body("The id token is invalid")
    }

    private fun responseNoSuchImageFound(): Response {
        println("No such image detected")
        return Response(NOT_FOUND)
            .body("No such image is found")
    }

    private fun responseNoImageSpecified(): Response {
        println("No image specified detected")
        return Response(BAD_REQUEST)
            .body("No image was specified")
    }

    private fun responseBadProfileLangPref(): Response {
        println("Bad profile lang pref detected")
        return Response(BAD_REQUEST)
            .body("The profile lang pref is invalid")
    }

    private fun responseBadProfileNickname(): Response {
        println("Bad profile nickname detected")
        return Response(BAD_REQUEST)
            .body("The profile nickname is invalid")
    }

    private fun responseBadProfileType(): Response {
        println("Bad profile type detected")
        return Response(BAD_REQUEST)
            .body("The profile type is invalid")
    }

    private fun responseDbNullProfilePoints(): Response {
        println("Null profile points from database detected")
        return Response(INTERNAL_SERVER_ERROR)
    }

    private fun responseDbNullProfileType(): Response {
        println("Null profile type from database detected")
        return Response(INTERNAL_SERVER_ERROR)
    }

    private fun responseInsufficientProfilePoints(): Response {
        println("Insufficient profile points detected")
        return Response(UNAUTHORIZED)
            .body("You do no have insufficient points")
    }

    private fun responseNoUidClaim(): Response {
        println("No uid claim detected")
        return Response(BAD_REQUEST)
            .body("No uid found in the claims of the access token given")
    }

    private fun responseSqlException(e: SQLException): Response {
        println("An sql exception was thrown")
        e.printStackTrace()
        return Response(INTERNAL_SERVER_ERROR)
    }

    private fun responseUnexpectedRowReturned(result: Int): Response {
        println("$result row(s) were returned unexpectedly")
        return Response(INTERNAL_SERVER_ERROR)
    }

    private fun responseFirestoreFieldError(e: RuntimeException): Response {
        println("Firestore field error detected")
        e.printStackTrace()
        return Response(INTERNAL_SERVER_ERROR)
    }

    private fun responseFirestoreNullField(field: String): Response {
        println("Firestore value for field $field is null")
        return Response(INTERNAL_SERVER_ERROR)
    }

    private fun responseUserAlreadyExists(): Response {
        println("User already exists")
        return Response(CONFLICT)
    }

    private fun responseNoUidProvided(): Response {
        println("No uid was provided when checking if a user exists")
        return Response(BAD_REQUEST)
            .body("Please provide a uid when checking if a user exists")
    }

    private fun generateAccessToken(uid: String): String {
        println("Token generated.")
        val issuedAt = Date()
        val expiresAt = Date(issuedAt.time + TOKEN_VALID_PERIOD)
        val notBefore = Date(issuedAt.time + TOKEN_INACTIVE_PERIOD)
        return JWT.create()
            .withIssuer(TOKEN_ISSUER)
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            .withNotBefore(notBefore)
            .withClaim("uid", uid)
            .sign(tokenAlgorithm)
    }

    private fun tryExtractFirebaseToken(request: Request): Result<FirebaseToken, Response> {
        val value = request.headers.findSingle("Authorization") ?: return Result.Error(responseNoAuth())
        val substrings = value.split(" ")
        if (substrings.size != 2 && substrings[0] != "Bearer") return Result.Error(responseBadAuth())

        val token = try {
            FirebaseAuth.getInstance().verifyIdToken(substrings[1])
        } catch (e: FirebaseAuthException) {
            return Result.Error(responseBadIdToken())
        }
        return Result.Value(token)
    }

    private fun tryExtractAccessToken(request: Request): Result<DecodedJWT, Response> {
        val value = request.headers.findSingle("Authorization") ?: return Result.Error(responseNoAuth())
        val substrings = value.split(" ")
        if (substrings.size != 2 && substrings[0] != "Bearer") return Result.Error(responseBadAuth())

        val token = try {
            accessTokenVerifier.verify(substrings[1])
        } catch (e: JWTVerificationException) {
            return Result.Error(responseBadAccessToken())
        }
        return Result.Value(token)
    }

    /*private fun tryDeductPoints(profile: Profile, cost: Int): Result<Unit, Response> {
        if (profile.points < cost) return Result.Error(responseInsufficientProfilePoints())
        val newPoints = profile.points - cost
        val result = sqliteDatabase.prepareStatement("""
            UPDATE users
            SET points = ?
            WHERE id = ?
        """.trimIndent()).apply {
            setInt(1, newPoints)
            setString(2, profile.uid)
        }.executeUpdate()
        return when {
            result == 0 -> Result.Error(responseNoUserFound())
            result != 1 -> Result.Error(responseUnexpectedRowReturned(result))
            else -> Result.Value(Unit)
        }
    }*/

    private fun userExists(uid: String): Boolean {
        val result = firestore.collection("users").document(uid).get().get()
        return result.exists()
    }

    private fun tryGetUser(uid: String): Result<Profile, Response> {
        val result = run {
            val query = firestore.collection("users").document(uid).get()
            query.get()
        }

        if (!result.exists()) return Result.Error(responseNoUserFound())

        val nickname = result.tryGetString("nickname")
            .otherwise { return Result.Error(responseFirestoreFieldError(it)) }
            ?: return Result.Error(responseFirestoreNullField("nickname"))
        val langPref = result.tryGetProfileLangPref("lang_pref")
            .otherwise { return Result.Error(responseFirestoreFieldError(it)) }
            ?: return Result.Error(responseFirestoreNullField("lang_pref"))
        val type = result.tryGetProfileType("type")
            .otherwise { return Result.Error(responseFirestoreFieldError(it)) }
            ?: return Result.Error(responseFirestoreNullField("type"))
        val points = result.tryGetLong("points")
            .otherwise { return Result.Error(responseFirestoreFieldError(it)) }
            ?: return Result.Error(responseFirestoreNullField("points"))

        return Result.Value(Profile(uid, nickname, langPref, type, points.toInt()))
    }

    private fun tryAddUser(profile: Profile): Result<Unit, Response> {
        val newDoc = firestore.collection("users").document(profile.uid)
        if (newDoc.get().get().exists()) return Result.Error(responseUserAlreadyExists())
        newDoc.set(profile.toMap()).get()
        return Result.Value(Unit)
    }

    private fun tryDeductPoints(uid: String, cost: Int): Result<Unit, Response> {
        val profile = tryGetUser(uid) otherwise { return Result.Error(it) }

        val oldPoints = profile.points

        val newPoints = oldPoints - cost
        if (newPoints < 0) return Result.Error(responseInsufficientProfilePoints())

        firestore.collection("users").document(uid).set(
            profile.copy(points = newPoints).toMap()
        ).get()
        return Result.Value(Unit)
    }

    private fun tryDeleteUser(uid: String): Result<Unit, Response> {
        val doc = firestore.collection("users").document(uid)
        if (!doc.get().get().exists()) return Result.Error(responseNoUserFound())
        doc.delete().get()
        return Result.Value(Unit)
    }

    private fun tryAddPoints(uid: String, pointsToAdd: Int): Result<Int, Response> {
        val profile = tryGetUser(uid) otherwise { return Result.Error(it) }
        val newPoints = profile.points + pointsToAdd

        firestore.collection("users").document(uid).set(
            profile.copy(points = newPoints).toMap()
        ).get()
        return Result.Value(newPoints)
    }
}

private val DecodedJWT.uid: String? get() = getClaim("uid").asString()