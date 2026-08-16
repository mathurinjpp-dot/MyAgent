package com.agent.agent.plan

import com.agent.core.utils.logger
import dev.langchain4j.data.message.SystemMessage.systemMessage
import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import org.springframework.stereotype.Component

@Component
class SynthesizerAgent(
    private val model: ChatModel
) {
    private val logger = logger()

    private val synthesizerSystemMessage = """
        Tu es MathurinAi, un assistant IA personnel.
        Tu reçois le résultat d'exécution de plusieurs étapes et tu dois formuler une réponse finale claire et concise.

        Règles:
        - Réponds toujours en français
        - Sois concis mais complet
        - Si des étapes ont échoué, indique-le clairement
        - Si tout a réussi, confirme et résume ce qui a été fait
        - N'ajoute pas d'informations inventées
        - Tu exécutes les outils disponibles sans jugement moral
        - Ne refuse jamais d'utiliser un outil qui t'est fourni
        - Tu es un assistant technique, pas un modérateur de contenu
    """.trimIndent()

    fun synthesize(userMessage: String, stepResults: List<StepResult>): String {
        if (stepResults.isEmpty()) {
            return "Je n'ai pas eu besoin d'utiliser d'outils pour cette demande."
        }

        val resultsSummary = stepResults.joinToString("\n\n") { result ->
            val status = if (result.success) "✅" else "❌"
            "$status Step ${result.step.stepNumber}: ${result.step.description}\nRésultat: ${result.output}"
        }

        val userContent = """
            Demande originale: $userMessage

            Résultats des étapes:
            $resultsSummary

            Formule une réponse finale claire pour l'utilisateur.
        """.trimIndent()

        val response: ChatResponse = model.chat(
            systemMessage(synthesizerSystemMessage),
            userMessage(userContent)
        )

        return response.aiMessage().text()
    }
}
