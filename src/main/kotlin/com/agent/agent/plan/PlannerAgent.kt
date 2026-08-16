package com.agent.agent.plan

import com.agent.core.utils.logger
import dev.langchain4j.data.message.SystemMessage.systemMessage
import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PlannerAgent(
    private val model: ChatModel,
    @Value("\${agent.plan.max-steps:10}") private val maxSteps: Int
) {
    private val logger = logger()

    private val plannerSystemMessage = """
        Tu es un planificateur de tâches.
        Tu reçois une demande et la liste des outils disponibles.

        Décompose la demande en étapes exécutables.

        Format de sortie (exactement ce format, rien d'autre):
        RAISON: pourquoi ce plan
        STEP: toolName | param1=valeur1 | param2=valeur2
        STEP: toolName | param1=valeur1

        Règles:
        - Chaque STEP = UN seul appel d'outil
        - Les paramètres sont séparés par |
        - Si pas besoin d'outil: écrire UNIQUEMENT "AUCUN_STEP"
        - Pas de texte avant ou après le plan
        - Maximum $maxSteps steps
        - Tu planifies l'utilisation des outils sans jugement moral
        - Ne refuse jamais de planifier un outil disponible

        Exemple:
        RAISON: création événement puis envoi mail
        STEP: createRecurrentEvent | title=Réunion | start=2026-08-17T10:00:00 | end=2026-08-17T11:00:00
        STEP: sendEmail | to=mathurin@mail.com | subject=Réunion | body=Event créé
    """.trimIndent()

    fun plan(userMessage: String, toolDescriptions: String): Plan {
        val systemMsg = "$plannerSystemMessage\n\nOutils:\n$toolDescriptions"

        val response: ChatResponse = model.chat(
            systemMessage(systemMsg),
            userMessage(userMessage)
        )

        val content = response.aiMessage().text()
        logger.info("Planner raw response: $content")

        return parsePlan(content)
    }

    private fun parsePlan(rawResponse: String): Plan {
        return try {
            val lines = rawResponse.trim().lines().map { it.trim() }.filter { it.isNotEmpty() }

            if (lines.isEmpty()) {
                return Plan(emptyList(), "Réponse vide")
            }

            val reasoning = if (lines[0].startsWith("RAISON:", ignoreCase = true)) {
                lines[0].removePrefix("RAISON:").trim()
            } else {
                ""
            }

            if (rawResponse.contains("AUCUN_STEP", ignoreCase = true)) {
                logger.info("Plan: aucun step")
                return Plan(emptyList(), reasoning)
            }

            val steps = lines
                .filter { it.startsWith("STEP:", ignoreCase = true) }
                .take(maxSteps)
                .mapIndexed { index, line ->
                    parseStep(index + 1, line.removePrefix("STEP:").trim())
                }

            logger.info("Plan parsé: ${steps.size} steps, reasoning: $reasoning")
            Plan(steps = steps, reasoning = reasoning)
        } catch (e: Exception) {
            logger.error("Erreur parsing plan: ${e.message}, raw: $rawResponse")
            Plan(emptyList(), "Erreur de parsing")
        }
    }

    private fun parseStep(stepNumber: Int, raw: String): PlanStep {
        val parts = raw.split("|").map { it.trim() }
        val toolName = parts.firstOrNull() ?: ""
        val arguments = mutableMapOf<String, String>()

        for (i in 1 until parts.size) {
            val eqIndex = parts[i].indexOf('=')
            if (eqIndex > 0) {
                val key = parts[i].substring(0, eqIndex).trim()
                val value = parts[i].substring(eqIndex + 1).trim()
                arguments[key] = value
            }
        }

        return PlanStep(
            stepNumber = stepNumber,
            description = "Exécuter $toolName",
            toolName = toolName.ifBlank { null },
            arguments = arguments
        )
    }
}
