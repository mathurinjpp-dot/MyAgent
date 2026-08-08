package com.agent.agent

import com.agent.agent.tools.CalendarTool
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.service.AiServices
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class AgentBuilder(private val model : ChatModel,private val calendarTool: CalendarTool) {
    @Bean
    fun agent(): GeneralAgent {
        return AiServices.builder(GeneralAgent::class.java).chatModel(model).tools(calendarTool).build()
    }
}
