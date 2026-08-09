package com.agent.cli

import com.agent.agent.GeneralAgent
import com.agent.cli.utils.Spinner
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.TerminalBuilder
import org.springframework.stereotype.Component

@Component
class AgentCli(
    private val generalAgent: GeneralAgent,
    private val waiting : Spinner
) {

    fun start() {

        val terminal = TerminalBuilder.builder()
            .system(true)
            .build()

        val reader = LineReaderBuilder.builder()
            .terminal(terminal)
            .build()

        println(
            """
            ╭────────────────────────────╮
            │ 🤖 Agent Console            │
            │                             │
            │ /help pour les commandes    │
            ╰────────────────────────────╯
            
            """.trimIndent()
        )

        var currentAgent = "general"

        while (true) {

            val input = reader.readLine("toi [$currentAgent] > ")

            when {
                input == "/exit" -> {
                    println("Bye 👋")
                    break
                }

                input == "/help" -> {
                    println(
                        """
                        Commandes :
                          /exit            quitter
                        
                        """.trimIndent()
                    )
                }



                else -> {

                    val spinner = waiting.startSpinner()

                    try {

                        val response = generalAgent.chat(input)

                        spinner.interrupt()
                        spinner.join()

                        print("\r${" ".repeat(30)}\r")

                        println("ia > $response\n")

                    } catch (e: Exception) {

                        Spinner.stop(spinner)
                    }
                }
            }
        }
    }
}