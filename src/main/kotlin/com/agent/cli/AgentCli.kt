package com.agent.cli

import com.agent.MyAgent
import com.agent.cli.utils.Spinner
import com.agent.core.utils.logger
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.TerminalBuilder
import org.springframework.stereotype.Component
import org.vosk.Model
import org.vosk.Recognizer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem


@Component
class AgentCli(
    private val myAgent : MyAgent,
    private val waiting : Spinner,
) {
    private val logger = logger()
    fun startWithMic(){
        val model: Model = Model("vosk-test")
        val recognizer = Recognizer(model, 16000f)

        val format = AudioFormat(16000f, 16, 1, true, false)
        val mic = AudioSystem.getTargetDataLine(format)

        mic.open(format)
        mic.start()

        val buffer = ByteArray(4096)

        while (true) {
            val n = mic.read(buffer, 0, buffer.size)

            if (recognizer.acceptWaveForm(buffer, n)) {
                logger.info(recognizer.result)
            } else {
                logger.info(recognizer.partialResult)
            }
        }
    }

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

                        val response = myAgent.chat("mathurin",input)

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