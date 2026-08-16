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

    @Tool("""
Recherche réellement une vidéo sur YouTube à partir d'un titre, artiste,
ou description. Le résultat provient directement de yt-dlp.

IMPORTANT :
- Ne fabrique jamais d'URL YouTube.
- Ne devine jamais une URL.
- Utilise cet outil lorsque tu dois trouver une vidéo YouTube à partir
  d'un titre ou d'une description.
- Si l'utilisateur fournit déjà une URL YouTube, n'utilise pas cet outil.
""")
    fun searchYoutubeUrl(
        @P("Titre, artiste ou description exacte de la vidéo/musique à rechercher")
        query: String
    ): String {

        return try {

            val process = ProcessBuilder(
                "/home/mathurin/.local/bin/yt-dlp",

                // Recherche YouTube
                "ytsearch1:$query",

                // Retourne uniquement les informations demandées
                "--print",
                "%(title)s|||%(webpage_url)s",

                // Pas de téléchargement
                "--skip-download",

                // Évite certains messages inutiles
                "--no-warnings"
            )
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream
                .bufferedReader()
                .readText()
                .trim()

            val exitCode = process.waitFor()

            if (exitCode != 0 || output.isBlank()) {
                return """
                {
                  "success": false,
                  "query": "$query",
                  "error": "Aucun résultat YouTube trouvé"
                }
            """.trimIndent()
            }

            val parts = output.split("|||", limit = 2)

            if (parts.size != 2) {
                return """
                {
                  "success": false,
                  "query": "$query",
                  "error": "yt-dlp a retourné un résultat invalide",
                  "raw": "$output"
                }
            """.trimIndent()
            }

            val title = parts[0].trim()
            val url = parts[1].trim()

            """
        {
          "success": true,
          "query": "$query",
          "title": "$title",
          "url": "$url",
          "source": "yt-dlp"
        }
        """.trimIndent()

        } catch (e: Exception) {

            """
        {
          "success": false,
          "query": "$query",
          "error": "${e.message ?: "Erreur inconnue"}"
        }
        """.trimIndent()
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
