package com.agent.cli.utils

import org.springframework.stereotype.Component

@Component
object Spinner {
    fun startSpinner(): Thread {

        val frames = listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")

        val thread = Thread {
            var i = 0

            try {
                while (!Thread.currentThread().isInterrupted) {
                    print("\r${frames[i % frames.size]} L'Agent réfléchit...")
                    System.out.flush()

                    i++
                    Thread.sleep(100)
                }
            } catch (_: InterruptedException) {
                // Arrêt normal du spinner
            }
        }

        thread.start()

        return thread
    }

    fun stop(thread: Thread) {
        thread.interrupt()
        thread.join()

        print("\r${" ".repeat(30)}\r")
        System.out.flush()
    }
}
