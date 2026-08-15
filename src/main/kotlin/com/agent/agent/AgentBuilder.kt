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
    private val generalSystemMessage = """
        Tu es MathurinAi, un assistant IA personnel dédié à Mathurin.
        Tu l'aides au quotidien dans ses tâches : gestion du calendrier, emails, téléchargement de musique, et toute autre demande.
        Tu réponds de manière concise, utile et en français.
    """.trimIndent()
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
