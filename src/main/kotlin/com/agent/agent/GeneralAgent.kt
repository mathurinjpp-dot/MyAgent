package com.agent.agent

import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.UserMessage


interface GeneralAgent {
    fun chat(@MemoryId memoryId: Any, @UserMessage userMessage: String, ) : String
}