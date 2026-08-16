package com.agent

import com.agent.agent.AgentBuilder
import com.agent.agent.capability.resolver.AgentCapabilityResolver
import com.agent.agent.memory.CapabilityStore
import com.agent.core.utils.logger
import org.springframework.stereotype.Component

@Component
class MyAgent(
    private val agentBuilder: AgentBuilder,
    private val agentCapabilityResolver: AgentCapabilityResolver,
    private val capabilityStore: CapabilityStore
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

        val generalAgent = agentBuilder.agent(allCapabilities)
        return generalAgent.chat(memoryId, message)
    }
}
