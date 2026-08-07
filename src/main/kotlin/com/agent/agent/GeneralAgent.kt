package com.agent.agent

import dev.langchain4j.service.SystemMessage


interface GeneralAgent {
    @SystemMessage("You are a good friend of mine. Answer using slang.")
    fun chat(input : String) : String
}