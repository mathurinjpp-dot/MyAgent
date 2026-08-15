package com.agent.cli

import com.agent.MyAgent
import com.agent.cli.utils.Spinner
import com.agent.core.utils.logger
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.TerminalBuilder
import org.springframework.stereotype.Component
import org.vosk.Model
import org.vosk.Recognizer
import java.awt.BorderLayout
import java.awt.GraphicsEnvironment
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.util.concurrent.atomic.AtomicReference
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.TargetDataLine
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument

@Component
class AgentCli(
    private val myAgent: MyAgent,
    private val waiting: Spinner,
) {
    private val logger = logger()

    private enum class MicState { IDLE, RECORDING }

    private lateinit var frame: JFrame
    private lateinit var chatArea: JTextPane
    private lateinit var inputField: JTextField
    private lateinit var sendButton: JButton
    private lateinit var micButton: JButton
    private lateinit var statusLabel: JLabel

    @Volatile private var micState = MicState.IDLE
    private var micLine: TargetDataLine? = null
    private var micThread: Thread? = null
    private var voskModel: Model? = null
    private val lastTranscript = AtomicReference("")

    private val BG_COLOR = Color(30, 30, 30)
    private val FG_COLOR = Color(220, 220, 220)
    private val INPUT_BG = Color(45, 45, 45)
    private val MIC_IDLE_COLOR = Color(100, 100, 100)
    private val MIC_RECORDING_COLOR = Color(220, 50, 50)
    private val SEND_BG = Color(79, 140, 255)

    fun start() {
        if (GraphicsEnvironment.isHeadless()) {
            logger.info("No display detected, falling back to terminal CLI")
            startCli()
        } else {
            SwingUtilities.invokeLater { buildUI() }
        }
    }

    private fun startCli() {
        val terminal = TerminalBuilder.builder().system(true).build()
        val reader = LineReaderBuilder.builder().terminal(terminal).build()

        println(
            """
            ╭────────────────────────────╮
            │ 🤖 MathurinAi               │
            │                             │
            │ /help pour les commandes    │
            ╰────────────────────────────╯
            
            """.trimIndent()
        )

        while (true) {
            val input = reader.readLine("toi > ")
            when {
                input == "/exit" -> { println("À bientôt !"); break }
                input == "/help" -> println("Commandes : /exit, /help, /micro")
                input == "/micro" -> startCliMic()
                input.isNullOrBlank() -> continue
                else -> {
                    val spinner = waiting.startSpinner()
                    try {
                        val response = myAgent.chat("mathurin", input)
                        spinner.interrupt()
                        spinner.join()
                        print("\r${" ".repeat(30)}\r")
                        println("ia > $response\n")
                    } catch (e: Exception) {
                        Spinner.stop(spinner)
                        println("Erreur: ${e.message}\n")
                    }
                }
            }
        }
    }

    private fun startCliMic() {
        val model = voskModel ?: run {
            println("Vosk model pas encore chargé, patientez...")
            voskModel = try { Model("vosk-test-powerfull") } catch (e: Exception) { null }
            voskModel ?: return
        }
        val recognizer = Recognizer(model, 16000f)
        val format = AudioFormat(16000f, 16, 1, true, false)
        val mic = AudioSystem.getTargetDataLine(format)
        mic.open(format)
        mic.start()
        val buffer = ByteArray(4096)
        println("🎤 Micro actif, parlez... (Ctrl+C pour arrêter)")
        while (true) {
            val n = mic.read(buffer, 0, buffer.size)
            if (recognizer.acceptWaveForm(buffer, n)) {
                val result = parseVoskResult(recognizer.result)
                if (result.isNotBlank()) {
                    println("→ $result")
                    mic.stop()
                    mic.close()
                    sendMessage(result)
                    return
                }
            } else {
                val partial = parseVoskResult(recognizer.partialResult)
                if (partial.isNotBlank()) print("\r... $partial...")
            }
        }
    }

    private fun buildUI() {
        println("[AgentCli] Building Swing UI...")

        frame = JFrame("MathurinAi")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.size = Dimension(700, 500)
        frame.setLocationRelativeTo(null)
        frame.background = BG_COLOR
        frame.layout = BorderLayout(0, 0)

        val titleBar = JPanel(BorderLayout())
        titleBar.background = Color(40, 40, 40)
        titleBar.border = EmptyBorder(10, 15, 10, 15)
        val titleLabel = JLabel("\uD83E\uDD16 Agent Console")
        titleLabel.foreground = FG_COLOR
        titleLabel.font = Font("SansSerif", Font.BOLD, 16)
        titleBar.add(titleLabel, BorderLayout.WEST)
        frame.add(titleBar, BorderLayout.NORTH)

        chatArea = JTextPane()
        chatArea.isEditable = false
        chatArea.background = BG_COLOR
        chatArea.foreground = FG_COLOR
        chatArea.font = Font("Monospaced", Font.PLAIN, 14)
        chatArea.border = EmptyBorder(15, 15, 15, 15)
        chatArea.caretColor = FG_COLOR

        val scrollPane = JScrollPane(chatArea)
        scrollPane.border = null
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        scrollPane.background = BG_COLOR
        scrollPane.viewport.background = BG_COLOR
        frame.add(scrollPane, BorderLayout.CENTER)

        val bottomPanel = JPanel(BorderLayout(8, 0))
        bottomPanel.background = BG_COLOR
        bottomPanel.border = EmptyBorder(10, 15, 15, 15)

        inputField = JTextField()
        inputField.background = INPUT_BG
        inputField.foreground = FG_COLOR
        inputField.caretColor = FG_COLOR
        inputField.font = Font("SansSerif", Font.PLAIN, 14)
        inputField.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color(60, 60, 60), 1),
            EmptyBorder(8, 12, 8, 12)
        )
        inputField.addActionListener {
            val text = inputField.text
            println("[AgentCli] Enter pressed, text='$text'")
            if (!text.isNullOrBlank()) sendMessage(text)
        }
        bottomPanel.add(inputField, BorderLayout.CENTER)

        val rightPanel = JPanel(FlowLayout(FlowLayout.LEFT, 6, 0))
        rightPanel.background = BG_COLOR

        sendButton = createButton("Envoyer", SEND_BG)
        sendButton.addActionListener {
            val text = inputField.text
            println("[AgentCli] Send button clicked, text='$text'")
            if (!text.isNullOrBlank()) sendMessage(text)
        }

        micButton = createButton("\uD83C\uDFA4 Micro", MIC_IDLE_COLOR)
        micButton.addActionListener {
            println("[AgentCli] Mic button clicked, state=$micState")
            toggleMic()
        }

        rightPanel.add(sendButton)
        rightPanel.add(micButton)
        bottomPanel.add(rightPanel, BorderLayout.EAST)

        statusLabel = JLabel(" ")
        statusLabel.foreground = Color(150, 150, 150)
        statusLabel.font = Font("SansSerif", Font.ITALIC, 11)

        val southPanel = JPanel(BorderLayout())
        southPanel.background = BG_COLOR
        southPanel.add(bottomPanel, BorderLayout.CENTER)
        southPanel.add(statusLabel, BorderLayout.SOUTH)
        frame.add(southPanel, BorderLayout.SOUTH)

        frame.isVisible = true
        inputField.requestFocusInWindow()

        appendSystemMessage("Bienvenue ! Tapez un message ou utilisez le micro.")

        loadVoskModel()
        println("[AgentCli] UI built successfully")
    }

    private fun createButton(text: String, bgColor: Color): JButton {
        val btn = JButton(text)
        btn.background = bgColor
        btn.foreground = Color.WHITE
        btn.font = Font("SansSerif", Font.BOLD, 12)
        btn.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            EmptyBorder(8, 14, 8, 14)
        )
        btn.isFocusPainted = false
        btn.cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
        return btn
    }

    private fun loadVoskModel() {
        Thread {
            try {
                println("[AgentCli] Loading Vosk model from vosk-test/ ...")
                voskModel = Model("vosk-test")
                println("[AgentCli] Vosk model loaded OK")
                logger.info("Vosk model loaded")
            } catch (e: Exception) {
                println("[AgentCli] Failed to load Vosk model: ${e.message}")
                logger.error("Failed to load Vosk model: ${e.message}")
                SwingUtilities.invokeLater {
                    statusLabel.text = "Modèle Vosk non chargé. Micro désactivé."
                }
            }
        }.start()
    }

    private fun toggleMic() {
        when (micState) {
            MicState.IDLE -> startRecording()
            MicState.RECORDING -> stopRecording()
        }
    }

    private fun startRecording() {
        val model = voskModel
        if (model == null) {
            SwingUtilities.invokeLater {
                statusLabel.text = "Modèle Vosk en cours de chargement..."
            }
            return
        }

        try {
            val format = AudioFormat(16000f, 16, 1, true, false)
            val line = AudioSystem.getTargetDataLine(format)
            line.open(format)
            line.start()
            micLine = line

            lastTranscript.set("")

            micState = MicState.RECORDING
            SwingUtilities.invokeLater {
                micButton.text = "\u23F9 Arrêter"
                micButton.background = MIC_RECORDING_COLOR
                inputField.isEnabled = false
                sendButton.isEnabled = false
                statusLabel.text = "\uD83D\uDD34 Enregistrement en cours..."
            }

            val recognizer = Recognizer(model, 16000f)
            println("[AgentCli] Recognizer created, mic thread starting...")
            micThread = Thread {
                val buffer = ByteArray(4096)
                var readCount = 0
                try {
                    while (micState == MicState.RECORDING) {
                        val n = line.read(buffer, 0, buffer.size)
                        readCount++
                        if (readCount % 50 == 1) {
                            println("[AgentCli] Mic read #$readCount: $n bytes")
                        }
                        if (n > 0) {
                            if (recognizer.acceptWaveForm(buffer, n)) {
                                val raw = recognizer.result
                                val result = parseVoskResult(raw)
                                println("[AgentCli] Vosk FINAL: raw=$raw | parsed='$result'")
                                if (result.isNotBlank()) {
                                    lastTranscript.set(result)
                                    SwingUtilities.invokeLater {
                                        statusLabel.text = "\uD83D\uDD34 $result"
                                    }
                                }
                            } else {
                                val raw = recognizer.partialResult
                                val partial = parseVoskResult(raw)
                                if (readCount % 50 == 1 || partial.isNotBlank()) {
                                    println("[AgentCli] Vosk PARTIAL: raw=$raw | parsed='$partial'")
                                }
                                if (partial.isNotBlank()) {
                                    SwingUtilities.invokeLater {
                                        statusLabel.text = "\uD83D\uDD34 ...$partial..."
                                    }
                                }
                            }
                        }
                    }
                    println("[AgentCli] Mic loop ended, final transcript='${lastTranscript.get()}'")
                } catch (e: Exception) {
                    println("[AgentCli] Mic capture error: ${e.message}")
                    e.printStackTrace()
                    logger.error("Mic capture error: ${e.message}")
                }
            }
            micThread?.isDaemon = true
            micThread?.start()

        } catch (e: Exception) {
            println("[AgentCli] Failed to open microphone: ${e.message}")
            logger.error("Failed to open microphone: ${e.message}")
            SwingUtilities.invokeLater {
                statusLabel.text = "Impossible d'accéder au micro: ${e.message}"
            }
            micState = MicState.IDLE
        }
    }

    private fun stopRecording() {
        SwingUtilities.invokeLater {
            micButton.text = "\uD83C\uDFA4 Micro"
            micButton.background = MIC_IDLE_COLOR
            statusLabel.text = "Transcription..."
        }

        Thread {
            try {
                micState = MicState.IDLE

                val line = micLine
                if (line != null) {
                    try { line.stop() } catch (_: Exception) {}
                    try { line.close() } catch (_: Exception) {}
                }
                micLine = null

                micThread?.join(2000)
                micThread = null

                val text = lastTranscript.get()
                println("[AgentCli] Recording stopped, transcript='$text'")

                SwingUtilities.invokeLater {
                    inputField.isEnabled = true
                    sendButton.isEnabled = true
                    statusLabel.text = " "
                }

                if (text.isNotBlank()) {
                    sendMessage(text)
                }

            } catch (e: Exception) {
                println("[AgentCli] Error stopping recording: ${e.message}")
                logger.error("Error stopping recording: ${e.message}")
                micState = MicState.IDLE
                SwingUtilities.invokeLater {
                    micButton.text = "\uD83C\uDFA4 Micro"
                    micButton.background = MIC_IDLE_COLOR
                    inputField.isEnabled = true
                    sendButton.isEnabled = true
                    statusLabel.text = " "
                }
            }
        }.start()
    }

    private fun parseVoskResult(json: String): String {
        val match = Regex(""""(?:text|partial)"\s*:\s*"([^"]*)"""").find(json)
        return match?.groupValues?.get(1) ?: ""
    }

    private fun sendMessage(text: String) {
        if (text.isNullOrBlank()) return

        val message = text.trim()
        println("[AgentCli] sendMessage: '$message'")

        SwingUtilities.invokeLater {
            inputField.text = ""
            appendMessage("toi", message)
            inputField.isEnabled = false
            sendButton.isEnabled = false
            micButton.isEnabled = false
            statusLabel.text = "L'Agent réfléchit..."
        }

        Thread {
            try {
                println("[AgentCli] Calling myAgent.chat()...")
                val response = myAgent.chat("mathurin", message)
                println("[AgentCli] Got response: ${response.take(100)}...")
                SwingUtilities.invokeLater {
                    appendMessage("ia", response)
                    inputField.isEnabled = true
                    sendButton.isEnabled = true
                    micButton.isEnabled = true
                    statusLabel.text = " "
                    inputField.requestFocusInWindow()
                }
            } catch (e: Exception) {
                println("[AgentCli] Agent error: ${e.message}")
                e.printStackTrace()
                logger.error("Agent error: ${e.message}")
                SwingUtilities.invokeLater {
                    appendMessage("ia", "Erreur: ${e.message}")
                    inputField.isEnabled = true
                    sendButton.isEnabled = true
                    micButton.isEnabled = true
                    statusLabel.text = " "
                }
            }
        }.start()
    }

    private fun appendSystemMessage(text: String) {
        val doc: StyledDocument = chatArea.styledDocument

        val attr = SimpleAttributeSet()
        StyleConstants.setForeground(attr, Color(150, 150, 150))
        StyleConstants.setItalic(attr, true)
        StyleConstants.setFontFamily(attr, "Monospaced")
        StyleConstants.setFontSize(attr, 13)

        doc.insertString(doc.length, "$text\n", attr)
        chatArea.caretPosition = doc.length
    }

    private fun appendMessage(sender: String, text: String) {
        val doc: StyledDocument = chatArea.styledDocument

        val prefixAttr = SimpleAttributeSet()
        StyleConstants.setForeground(prefixAttr, if (sender == "toi") Color(79, 140, 255) else Color(80, 200, 120))
        StyleConstants.setBold(prefixAttr, true)
        StyleConstants.setFontFamily(prefixAttr, "Monospaced")
        StyleConstants.setFontSize(prefixAttr, 14)

        val textAttr = SimpleAttributeSet()
        StyleConstants.setForeground(textAttr, FG_COLOR)
        StyleConstants.setFontFamily(textAttr, "Monospaced")
        StyleConstants.setFontSize(textAttr, 14)

        val prefix = if (sender == "toi") "\ntoi > " else "\nia > "

        doc.insertString(doc.length, prefix, prefixAttr)
        doc.insertString(doc.length, text, textAttr)

        chatArea.caretPosition = doc.length
    }
}
