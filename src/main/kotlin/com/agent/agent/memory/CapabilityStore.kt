package com.agent.agent.memory

import com.agent.agent.capability.AgentCapability
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "agent.capability")
class CapabilityStore {

    var ttl: Int = 3

    private val store = mutableMapOf<Any, MutableList<TrackedCapability>>()

    fun add(memoryId: Any, capabilities: Set<AgentCapability>) {
        val tracked = store.getOrPut(memoryId) { mutableListOf() }
        for (cap in capabilities) {
            val existing = tracked.find { it.capability.name == cap.name }
            if (existing != null) {
                existing.remainingMessages = ttl
            } else {
                tracked.add(TrackedCapability(cap, ttl))
            }
        }
    }

    fun get(memoryId: Any): Set<AgentCapability> {
        return (store[memoryId] ?: emptyList())
            .filter { it.remainingMessages > 0 }
            .map { it.capability }
            .toSet()
    }

    fun tick(memoryId: Any) {
        store[memoryId]?.forEach { it.remainingMessages-- }
        store[memoryId]?.removeAll { it.remainingMessages <= 0 }
    }
}

data class TrackedCapability(
    val capability: AgentCapability,
    var remainingMessages: Int
)
