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
        1. Identifie précisément le morceau demandé.
        2. Recherche une URL YouTube correspondant à la version officielle ou la plus pertinente.
        3. Vérifie que le titre et l'artiste correspondent à la demande.
        4. Appelle l'outil `downloadMp3FromUrl` avec l'URL trouvée.
        5. L'outil utilise yt-dlp pour extraire l'audio et le convertir en MP3.
        6. Retourne le résultat du téléchargement à Mathurin.

        Règles :
        - Ne prétends jamais avoir téléchargé un fichier sans confirmation de l'outil.
        - N'invente jamais une URL YouTube.
        - Si plusieurs résultats existent, privilégie la vidéo officielle ou une source autorisée.
        - Ne lance jamais yt-dlp directement : utilise uniquement l'outil `downloadMp3FromUrl`.
        - En cas d'échec, explique l'erreur honnêtement.

        Exemple :
        « Télécharge Lose Yourself d'Eminem en MP3. »
        → Recherche « Eminem - Lose Yourself », puis utilise `downloadMp3FromUrl` avec l'URL retenue.
    """.trimIndent()


    override fun tools(): MyTool {
        return mp3Tool
    }

    override fun matcher() = mp3CapabilityMatcher
}
