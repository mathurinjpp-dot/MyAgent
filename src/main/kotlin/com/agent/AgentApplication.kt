package com.agent

import com.agent.cli.AgentCli
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import javax.swing.SwingUtilities

@SpringBootApplication
class AgentApplication

fun main(args: Array<String>) {
    val context = runApplication<AgentApplication>(*args)
    SwingUtilities.invokeLater {
        context.getBean(AgentCli::class.java).start()
    }
}
