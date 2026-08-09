package com.agent.agent

import com.agent.agent.capability.AgentCapability
import com.agent.agent.memory.AgentMemoryProvider
import dev.langchain4j.data.message.SystemMessage.systemMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.service.AiServices
import org.springframework.stereotype.Component

@Component
class AgentBuilder(private val model : ChatModel, private val agentMemoryProvider: AgentMemoryProvider) {
        private val generalSystemMessage = """tu es MathurinAi Mon IA personnel qui m'aide au quotidien \n"""
    fun agent(capabilities : Set<AgentCapability>): GeneralAgent {
        val tools = capabilities.map { it.tools() }
        val toolContext = capabilities.map { it.context() }.joinToString { "\n\n" }

        return AiServices.builder(GeneralAgent::class.java)
            .chatModel(model)
            .tools(*tools.toTypedArray())
            .systemMessage(generalSystemMessage+toolContext)
            .chatMemoryProvider(agentMemoryProvider)
            .build()
    }
}
