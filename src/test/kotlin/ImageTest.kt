import com.google.api.client.util.IOUtils
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class ImageTest {

    val serverUrl = "https://asia-southeast1-music-theory-app-1643350728268.cloudfunctions.net/amta-server"

    @Test
    fun write() {
        val log = File("C:/Users/Donald/IdeaProjects/AmtaServer/log.txt")
        val writer = log.writer(StandardCharsets.UTF_8)
        writer.write("Hey")
        writer.write("Hey")
        //writer.close()
    }

    @Test
    fun test() {
        val directory = File("C:/Users/Donald/IdeaProjects/AmtaServer/DownloadedImages")
        val logWriter = File("C:/Users/Donald/IdeaProjects/AmtaServer/log.txt").writer(StandardCharsets.UTF_8)
        if (!directory.exists()) directory.mkdir()
        for (i in 1..52) {
            val imageName = "q_time_signature_$i"
            val client = ApacheClient()
            val request = Request(Method.GET, "$serverUrl/images/$imageName")
            val startTime = System.currentTimeMillis()
            val response = client(request)
            if (response.body.length == 0L) throw IllegalStateException("Length is zero with image name $imageName")
            val outputStream = FileOutputStream(File(directory, "q_time_signature_$i.png"))
            outputStream.write(response.body.stream.readAllBytes())
            val endTime = System.currentTimeMillis()
            outputStream.close()
            logWriter.write("File with name: $imageName took ${endTime - startTime} ms.\n")
        }
        logWriter.close()
    }

}