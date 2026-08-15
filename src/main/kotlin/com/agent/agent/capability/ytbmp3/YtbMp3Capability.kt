package com.agent.agent.capability.ytbmp3

import com.agent.agent.capability.AgentCapability
import com.agent.agent.capability.MyTool
import com.agent.agent.capability.ytbmp3.matcher.YtbMp3CapabilityMatcher
import com.agent.agent.capability.ytbmp3.tools.YtbMp3Tool
import org.springframework.stereotype.Component

@Component
class YtbMp3Capability(private val mp3Tool: YtbMp3Tool, private val mp3CapabilityMatcher: YtbMp3CapabilityMatcher) : AgentCapability {
    override val name: String
        get() = "ytb to mp3 capability"
    override val description: String
        get() = "the capability of the agent to convert youtube vidéo to mp3 and download it"

    override fun context(): String =
        """Tu es un agent capable d'utiliser des outils locaux pour rechercher et télécharger des contenus audio.

Objectif :
L'utilisateur peut te demander de télécharger une musique. Dans ce cas, tu dois :
1. Identifier précisément le morceau demandé.
2. Rechercher une URL YouTube correspondant au morceau officiel ou à la source la plus pertinente.
3. Vérifier que le titre et l'artiste correspondent bien à la demande.
4. Appeler le tool `download_audio` avec l'URL YouTube trouvée.
5. Le tool utilise `yt-dlp` pour extraire l'audio et le convertir en MP3.
6. Retourner à l'utilisateur le résultat du téléchargement.

Règles :
- Ne prétends jamais avoir téléchargé un fichier si le tool n'a pas confirmé le téléchargement.
- N'invente jamais une URL YouTube.
- Si plusieurs résultats sont possibles, privilégie la vidéo officielle ou une source autorisée.
- Ne lance pas `yt-dlp` directement : utilise uniquement le tool `download_audio`.
- Si le téléchargement échoue, explique simplement l'erreur au lieu de prétendre que l'opération a réussi.

Exemple de demande utilisateur :
« Télécharge Lose Yourself d'Eminem en MP3. »

Pour cette demande, recherche d'abord la vidéo correspondant à « Eminem - Lose Yourself », puis utilise `download_audio` avec l'URL retenue.""".trimIndent()


    override fun tools(): MyTool {
        return mp3Tool
    }

    override fun matcher() = mp3CapabilityMatcher
}
