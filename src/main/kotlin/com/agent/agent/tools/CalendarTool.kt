package com.agent.agent.tools

import com.agent.calendar.services.CalendarService
import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CalendarTool(private val calendarService: CalendarService) {


    @Tool("Create an event in the user's Google Calendar")
    @Observed(name = "createEvent")
    fun createEvent(
        @P("Event title") title: String,

        @P("Detailed description of the event") description: String?,

        @P("Start date and time in ISO-8601 format, e.g. 2026-08-10T14:00:00")
        start: String,

        @P("End date and time in ISO-8601 format, e.g. 2026-08-10T15:00:00")
        end: String,

        @P("Location of the event") location: String?
    ): String {

        val event = calendarService.createEvent(
            title = title,
            description = description,
            start = LocalDateTime.parse(start),
            end = LocalDateTime.parse(end),
            location = location
        )

        return """
            Événement créé avec succès.
            Titre : ${event.summary}
            Début : ${event.start.dateTime}
            Fin : ${event.end.dateTime}
            Lieu : ${event.location ?: "Non renseigné"}
            Lien : ${event.htmlLink ?: "Non disponible"}
        """.trimIndent()
    }
}


