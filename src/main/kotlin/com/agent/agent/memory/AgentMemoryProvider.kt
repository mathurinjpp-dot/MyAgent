package com.agent.agent.memory

import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import org.springframework.stereotype.Component

@Component
class AgentMemoryProvider : ChatMemoryProvider {

    companion object {
        private const val MAX_MESSAGES = 20
    }

    private val memories = mutableMapOf<Any, ChatMemory>()

    override fun get(memoryId: Any): ChatMemory {
        return memories.getOrPut(memoryId) {
            MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(MAX_MESSAGES)
                .build()
        }
    }
}
