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
    Télécharger l'audio d'une URL YouTube fournie au format MP3.
    Le fichier MP3 est sauvegardé dans /home/music/Musique.
    """
    )
    fun downloadMp3FromUrl(
        @P("L'URL YouTube à télécharger") url: String,
        @P("Nom du fichier musical (le plus clair possible)") name: String
    ): String {
        if (!isValidYoutubeUrl(url)) {
            return "L'URL YouTube fournie n'est pas accessible."
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
                "MP3 téléchargé avec succès dans /home/music/Musique."
            } else {
                "Échec du téléchargement de l'audio YouTube : $output"
            }
        } catch (e: Exception) {
            "Une erreur est survenue lors du téléchargement de l'audio : ${e.message}"
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
