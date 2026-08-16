package com.agent.agent.plan

import com.agent.agent.capability.AgentCapability
import com.agent.core.utils.logger
import org.springframework.stereotype.Component

@Component
class PlanOrchestrator(
    private val plannerAgent: PlannerAgent,
    private val planExecutor: PlanExecutor,
    private val synthesizerAgent: SynthesizerAgent,
    private val toolRegistry: ToolRegistry
) {
    private val logger = logger()

    fun execute(message: String, capabilities: Set<AgentCapability>): String {
        logger.info("=== Plan & Execute ===")
        logger.info("Message: $message")
        logger.info("Capabilities: ${capabilities.map { it.name }}")

        val toolDescriptions = buildToolDescriptions(capabilities)
        logger.info("Tool descriptions:\n$toolDescriptions")

        val plan = plannerAgent.plan(message, toolDescriptions)
        logger.info("Plan généré: ${plan.steps.size} steps - ${plan.reasoning}")

        if (plan.steps.isEmpty()) {
            logger.info("Pas de steps à exécuter, retour direct au synthesizer")
            return synthesizerAgent.synthesize(message, emptyList())
        }

        val results = planExecutor.execute(plan, capabilities)
        logger.info("Résultats: ${results.map { "${it.step.stepNumber}=${it.success}" }}")

        val response = synthesizerAgent.synthesize(message, results)
        logger.info("Réponse finale: ${response.take(100)}...")

        return response
    }

    private fun buildToolDescriptions(capabilities: Set<AgentCapability>): String {
        toolRegistry.register(capabilities)
        return capabilities.joinToString("\n\n") { capability ->
            val tools = capability.tools()::class.java.methods
                .filter { it.isAnnotationPresent(dev.langchain4j.agent.tool.Tool::class.java) }
                .map { method ->
                    val annotation = method.getAnnotation(dev.langchain4j.agent.tool.Tool::class.java)
                    val name = annotation.name.ifBlank { method.name }
                    "  - $name: ${annotation.value}"
                }
                .joinToString("\n")
            "${capability.name}: ${capability.description}\n$tools"
        }
    }
}
