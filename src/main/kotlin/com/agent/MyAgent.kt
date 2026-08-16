package com.agent

import com.agent.agent.AgentBuilder
import com.agent.agent.capability.resolver.AgentCapabilityResolver
import com.agent.agent.memory.CapabilityStore
import com.agent.agent.plan.PlanOrchestrator
import com.agent.core.utils.logger
import org.springframework.stereotype.Component

@Component
class MyAgent(
    private val agentBuilder: AgentBuilder,
    private val agentCapabilityResolver: AgentCapabilityResolver,
    private val capabilityStore: CapabilityStore,
    private val planOrchestrator: PlanOrchestrator
) {
    private val logger = logger()

    fun chat(memoryId: String, message: String): String {
        logger.info("=== DEBUG RESOLVE ===")
        logger.info("Message reçu: $message")

        val newCapabilities = agentCapabilityResolver.resolve(message)
        logger.info("New capabilities résolues: ${newCapabilities.map { it.name }}")

        capabilityStore.add(memoryId, newCapabilities)
        val allCapabilities = capabilityStore.get(memoryId)
        capabilityStore.tick(memoryId)

        logger.info("All capabilities actives après store: ${allCapabilities.map { it.name }}")
        logger.info("=== FIN DEBUG RESOLVE ===")

        return if (allCapabilities.isNotEmpty()) {
            planOrchestrator.execute( message, allCapabilities)
        } else {
            val generalAgent = agentBuilder.agent(allCapabilities)
            generalAgent.chat(memoryId, message)
        }
    }
}
