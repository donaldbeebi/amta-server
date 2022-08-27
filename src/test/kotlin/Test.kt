import com.google.cloud.functions.HttpRequest
import com.google.cloud.functions.HttpResponse
import com.donald.abrsmappserver.MainFunction
import com.donald.abrsmappserver.server.Server
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.functions.HttpFunction
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.StorageOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.serverless.AppLoader
import org.http4k.serverless.GoogleCloudHttpFunction
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.HashMap

class FakeRequest(
    private val _method: String,
    private val _uri: String,
    private val _path: String = "",
    private val _headers: MutableMap<String, MutableList<String>>,
    private val _body: String,
) : HttpRequest {
    override fun getContentType(): Optional<String> {
        return Optional.empty()
    }

    override fun getContentLength(): Long {
        return _body.length.toLong()
    }

    override fun getCharacterEncoding(): Optional<String> {
        return Optional.empty()
    }

    override fun getInputStream(): InputStream {
        return ByteArrayInputStream(_body.toByteArray(StandardCharsets.UTF_8))
    }

    override fun getReader(): BufferedReader {
        return BufferedReader(StringReader(_body))
    }

    override fun getHeaders(): MutableMap<String, MutableList<String>> {
        return _headers
    }

    override fun getMethod(): String {
        return _method
    }

    override fun getUri(): String {
        return _uri
    }

    override fun getPath(): String {
        return _path
    }

    override fun getQuery(): Optional<String> {
        return Optional.empty()
    }

    override fun getQueryParameters(): MutableMap<String, MutableList<String>> {
        return HashMap()
    }

    override fun getParts(): MutableMap<String, HttpRequest.HttpPart> {
        return HashMap()
    }
}

class FakeResponse : HttpResponse {

    var code: Int? = null
        private set
    var message: String? = null
        private set
    var contentType: String? = null
        private set
    var headers = HashMap<String, MutableList<String>>()
        private set
    //var body: String? = null
    private val _outputStream = object : OutputStream() {
        private val builder = StringBuilder()
        override fun write(b: Int) {
            builder.append(b.toChar())
        }

        override fun toString(): String {
            return builder.toString()
        }
    }

    override fun setStatusCode(code: Int) {
        this.code = code
    }

    override fun setStatusCode(code: Int, message: String?) {
        this.code = code
        this.message = message
    }

    override fun setContentType(contentType: String) {
        this.contentType = contentType
    }

    override fun getContentType(): Optional<String> {
        return contentType?.let { Optional.of(it) } ?: Optional.empty()
    }

    override fun appendHeader(header: String, value: String) {
        val values = headers[header]
        if (values != null) {
            values += value
        } else {
            headers[header] = ArrayList<String>(1).apply { this.add(value) }
        }
    }

    override fun getHeaders(): MutableMap<String, MutableList<String>> {
        return headers
    }

    override fun getOutputStream(): OutputStream {
        return _outputStream
    }

    override fun getWriter(): BufferedWriter {
        return BufferedWriter(OutputStreamWriter(_outputStream))
    }
}

private val routes = routes(
    "/exercise" bind Method.GET to { request: Request -> Response(Status.OK).body("Here's an exercise with name: ${request.queries("name").getOrNull(0)}") },
    "/user" bind Method.POST to { request: Request -> Response(Status.CREATED).body("User with email ${request.queries("email").getOrNull(0)} created") },
    "/ping/{message}" bind Method.GET to { request: Request -> Response(Status.OK).body(request.path("message") ?: "No message received") },
    "/" bind Method.GET to { _: Request -> Response(Status.OK).body("Hello cruel world!") }
)

object FakeAppLoader : AppLoader {
    override fun invoke(env: Map<String, String>): HttpHandler {
        return routes
    }
}

class FakeCloudFunction : GoogleCloudHttpFunction(FakeAppLoader)

class FakeMainFunction : HttpFunction {
    override fun service(request: HttpRequest, response: HttpResponse) {
        println("Servicing!")
        val function = FakeCloudFunction()
        function.service(request, response)
    }
}

class Test {

    @RepeatedTest(100)
    fun serverTest() {
        val server = Server
        server.invoke(emptyMap()).invoke(
            object : Request {
                override val body: Body
                    get() = TODO("Not yet implemented")
                override val headers: Headers
                    get() = TODO("Not yet implemented")
                override val method: Method
                    get() = TODO("Not yet implemented")
                override val source: RequestSource?
                    get() = TODO("Not yet implemented")
                override val uri: Uri
                    get() = TODO("Not yet implemented")
                override val version: String
                    get() = TODO("Not yet implemented")

                override fun body(body: InputStream, length: Long?): Request {
                    TODO("Not yet implemented")
                }

                override fun body(body: String): Request {
                    TODO("Not yet implemented")
                }

                override fun body(body: Body): Request {
                    TODO("Not yet implemented")
                }

                override fun header(name: String, value: String?): Request {
                    TODO("Not yet implemented")
                }

                override fun headers(headers: Headers): Request {
                    TODO("Not yet implemented")
                }

                override fun method(method: Method): Request {
                    TODO("Not yet implemented")
                }

                override fun queries(name: String): List<String?> {
                    TODO("Not yet implemented")
                }

                override fun query(name: String): String? {
                    TODO("Not yet implemented")
                }

                override fun query(name: String, value: String?): Request {
                    TODO("Not yet implemented")
                }

                override fun removeHeader(name: String): Request {
                    TODO("Not yet implemented")
                }

                override fun removeHeaders(prefix: String): Request {
                    TODO("Not yet implemented")
                }

                override fun removeQueries(prefix: String): Request {
                    TODO("Not yet implemented")
                }

                override fun removeQuery(name: String): Request {
                    TODO("Not yet implemented")
                }

                override fun replaceHeader(name: String, value: String?): Request {
                    TODO("Not yet implemented")
                }

                override fun replaceHeaders(source: Headers): Request {
                    TODO("Not yet implemented")
                }

                override fun source(source: RequestSource): Request {
                    TODO("Not yet implemented")
                }

                override fun uri(uri: Uri): Request {
                    TODO("Not yet implemented")
                }

            }
        )
    }

    fun test() {
        /*
        val function = MainFunction()
        val request = FakeRequest(
            _uri = "https://localhost/exercise",
            _method = "GET",
            _body = """
                {
                    "mode": "test",
                    "lang_pref": "en"
                }
            """.trimIndent(),
            _headers = HashMap()
        )
        val response = FakeResponse()
        function.service(request, response)
        println("response = ${response.outputStream}")
        function.service(request, response)

         */

        //val response = routes(Request(Method.GET, "/exercise?name=dingus"))
        //println(response.bodyString())
        val storage = StorageOptions.getDefaultInstance().service
        //val bucket = storage.create(BucketInfo.of("hello_bitch"))
        val blob = storage.get("music-theory-app-1643350728268.appspot.com").get("images/7_1_test.png")
        println("Bucket got with name ${blob.name}")
    }

}