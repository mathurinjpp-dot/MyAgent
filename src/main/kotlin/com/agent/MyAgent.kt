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
        val newCapabilities = agentCapabilityResolver.resolve(message)
        capabilityStore.add(memoryId, newCapabilities)
        val allCapabilities = capabilityStore.get(memoryId)
        capabilityStore.tick(memoryId)
        logger.info("capacités actives : $allCapabilities")
        val generalAgent = agentBuilder.agent(allCapabilities)
        return generalAgent.chat(memoryId, message)
    }
}
