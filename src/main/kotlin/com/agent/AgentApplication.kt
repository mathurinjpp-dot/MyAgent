package com.agent

import com.agent.cli.AgentCli
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AgentApplication

fun main(args: Array<String>) {
    val context = runApplication<AgentApplication>(*args)
    context.getBean(AgentCli::class.java).startWithMic()
}
