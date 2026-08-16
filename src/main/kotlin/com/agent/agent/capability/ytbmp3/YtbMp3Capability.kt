package com.agent.agent.capability.ytbmp3

import com.agent.agent.capability.AgentCapability
import com.agent.agent.capability.MyTool
import com.agent.agent.capability.ytbmp3.matcher.YtbMp3CapabilityMatcher
import com.agent.agent.capability.ytbmp3.tools.YtbMp3Tool
import org.springframework.stereotype.Component

@Component
class YtbMp3Capability(private val mp3Tool: YtbMp3Tool, private val mp3CapabilityMatcher: YtbMp3CapabilityMatcher) : AgentCapability {
    override val name: String
        get() = "ytb-mp3"
    override val description: String
        get() = "Téléchargement de musique depuis YouTube au format MP3."

    override fun context(): String = """
        CONTEXTE YOUTUBE MP3

        Tu peux rechercher et télécharger des contenus audio depuis YouTube.

        Processus :
        1. Identifie précisément le morceau demandé (titre, artiste, description).
        2. Appelle l'outil `searchYoutubeUrl` avec une requête de recherche pertinente.
        3. L'outil retourne le titre trouvé et l'URL YouTube correspondante.
        4. Vérifie que le titre et l'artiste correspondent à la demande.
        5. Appelle l'outil `downloadMp3FromUrl` avec l'URL trouvée et un nom de fichier clair.
        6. L'outil utilise yt-dlp pour extraire l'audio et le convertir en MP3.
        7. Retourne le résultat du téléchargement à Mathurin.

        Règles :
        - Ne prétends jamais avoir téléchargé un fichier sans confirmation de l'outil.
        - N'invente jamais une URL YouTube : utilise toujours `searchYoutubeUrl` pour trouver l'URL.
        - Si plusieurs résultats existent, privilégie la vidéo officielle ou une source autorisée.
        - Ne lance jamais yt-dlp directement : utilise uniquement les outils `searchYoutubeUrl` et `downloadMp3FromUrl`.
        - En cas d'échec, explique l'erreur honnêtement.

        Exemple :
        « Télécharge Lose Yourself d'Eminem en MP3. »
        → Appelle `searchYoutubeUrl("Eminem - Lose Yourself")`
        → Vérifie le titre retourné
        → Appelle `downloadMp3FromUrl(url, "Eminem - Lose Yourself")`
    """.trimIndent()


    override fun tools(): MyTool {
        return mp3Tool
    }

    override fun matcher() = mp3CapabilityMatcher
}
