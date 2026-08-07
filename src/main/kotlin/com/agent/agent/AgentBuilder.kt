package com.agent.agent

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.service.AiServices
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class AgentBuilder(private val model : ChatModel) {
    @Bean
    fun agent(): GeneralAgent {
        return AiServices.builder(GeneralAgent::class.java).chatModel(model).build()
    }
}
