package com.agent.agent.plan

import com.agent.agent.capability.AgentCapability
import com.agent.core.utils.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PlanExecutor(
    private val toolRegistry: ToolRegistry,
    @Value("\${agent.plan.max-steps:10}") private val maxSteps: Int
) {
    private val logger = logger()

    fun execute(plan: Plan, capabilities: Set<AgentCapability>): List<StepResult> {
        toolRegistry.register(capabilities)

        val results = mutableListOf<StepResult>()
        val stepsToExecute = plan.steps.take(maxSteps)

        logger.info("Exécution du plan: ${stepsToExecute.size} steps")

        for (step in stepsToExecute) {
            logger.info("Step ${step.stepNumber}: ${step.description}")

            if (step.toolName == null) {
                results.add(
                    StepResult(
                        step = step,
                        output = "Step informatif: ${step.description}",
                        success = true
                    )
                )
                continue
            }

            if (!toolRegistry.hasTool(step.toolName)) {
                results.add(
                    StepResult(
                        step = step,
                        output = "Outil '${step.toolName}' non disponible",
                        success = false
                    )
                )
                continue
            }

            try {
                val output = toolRegistry.invoke(step.toolName, step.arguments)
                val success = !output.startsWith("Erreur")
                results.add(StepResult(step = step, output = output, success = success))
                logger.info("Step ${step.stepNumber} terminé: success=$success")
            } catch (e: Exception) {
                logger.error("Erreur step ${step.stepNumber}: ${e.message}")
                results.add(
                    StepResult(
                        step = step,
                        output = "Erreur inattendue: ${e.message}",
                        success = false
                    )
                )
            }
        }

        return results
    }
}
