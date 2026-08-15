package com.agent.agent.capability.ytbmp3.tools

import com.agent.agent.capability.MyTool
import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

@Component
class YtbMp3Tool : MyTool {

    @Tool(
        """
    Download the audio from the provided YouTube URL as an MP3 file.
    The MP3 file is saved to /home/mathurin/Musique.
    """
    )
    fun downloadMp3FromUrl(
        @P("The YouTube URL to download") url: String,@P("the name of the musique choose it the clearer possible") name: String
    ): String {
        if (!isValidYoutubeUrl(url)) {
            return "The provided YouTube URL is not accessible."
        }
        return try {
            val process = ProcessBuilder(
                "/home/mathurin/.local/bin/yt-dlp",
                "--js-runtimes", "deno",
                "-x",
                "--audio-format", "mp3",
                "--output", "/home/mathurin/Musique/$name",
                url
            )
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                "MP3 successfully downloaded to /home/mathurin/Musique."
            } else {
                "Failed to download the YouTube audio: $output"
            }
        } catch (e: Exception) {
            "An error occurred while downloading the audio: ${e.message}"
        }

    }

    private fun isValidYoutubeUrl(url: String): Boolean {
        try {
            val uri: URI = URI.create(url)

            var host: String? = uri.host ?: return false

            host = host?.lowercase(Locale.getDefault())

            if ((host != "youtube.com") && (host != "www.youtube.com") && (host != "youtu.be") && (host != "www.youtu.be")) {
                return false
            }

            val client: HttpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()

            val request: HttpRequest? = HttpRequest.newBuilder(uri)
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build()

            val response: HttpResponse<Void?> =
                client.send(request, HttpResponse.BodyHandlers.discarding())

            return response.statusCode() in 200..<400
        } catch (e: Exception) {
            return false
        }
    }



}
