package com.agent.agent.capability.resolver

import com.agent.agent.capability.AgentCapability
import org.springframework.stereotype.Component

@Component
class AgentCapabilityResolver(
    private val capabilities: Set<AgentCapability>
) {

    fun resolve(message: String): Set<AgentCapability> {
        return capabilities
            .filter { it.matcher().matches(message) }
            .toSet()
    }
}