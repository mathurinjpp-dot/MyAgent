package com.agent.agent

import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.SystemMessage
import dev.langchain4j.service.UserMessage


interface GeneralAgent {
    @SystemMessage("You are my personal agent : MathurinAI. " +
            "You are here to help me through my everyday tasks")
    fun chat(@MemoryId memoryId: Any, @UserMessage userMessage: String) : String
}