package com.agent.agent

import com.agent.agent.capability.AgentCapability
import com.agent.agent.memory.AgentMemoryProvider
import com.agent.core.utils.logger
import dev.langchain4j.data.message.SystemMessage.systemMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.service.AiServices
import org.springframework.stereotype.Component

@Component
class AgentBuilder(private val model : ChatModel, private val agentMemoryProvider: AgentMemoryProvider) {
    private val logger = logger()
    private val generalSystemMessage = """tu es MathurinAi Mon IA personnel qui m'aide au quotidien \n"""
    fun agent(capabilities : Set<AgentCapability>): GeneralAgent {
        val tools = capabilities.map { it.tools() }
        val toolContext = capabilities
            .joinToString("\n\n") { it.context() }
        logger.info("tool context = $toolContext")
        logger.info("=== DEBUG AGENT ===")
        logger.info("Nombre de capabilities actives: ${capabilities.size}")
        logger.info("Capabilities: ${capabilities.map { it.name }}")
        logger.info("Tool context longueur: ${toolContext.length}")
        logger.info("System prompt complet:\n${generalSystemMessage + toolContext}")
        logger.info("=== FIN DEBUG AGENT ===")

        return AiServices.builder(GeneralAgent::class.java)
            .chatModel(model)
            .tools(*tools.toTypedArray())
            .systemMessage(generalSystemMessage+toolContext)
            .chatMemoryProvider(agentMemoryProvider)
            .build()
    }
}
