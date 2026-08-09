package com.agent.agent.tools

import com.agent.calendar.services.CalendarService
import com.agent.core.utils.logger
import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CalendarTool(private val calendarService: CalendarService) {
    private val logger = logger()

    @Tool("Create an event in the user's Google Calendar, " +
            "Be the more autonomous possible don't ask for much clarifications," +
            " only the date is needed." +
            "For example if i ask in the afternoon : you choose in the afternoon")
    fun createEvent(
        @P("Event title") title: String,

        @P("Detailed description of the event") description: String?,

        @P("Start date and time in ISO-8601 format, e.g. 2026-08-10T14:00:00")
        start: String,

        @P("End date and time in ISO-8601 format, e.g. 2026-08-10T15:00:00")
        end: String,

        @P("Location of the event") location: String?
    ): String {
        logger.info("Using createEvent tool")

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

    @Tool("Get all the event in the calendar during the given time")
    fun getAllEventInTime(
        @P("Start date and time in ISO-8601 format, e.g. 2026-08-10T14:00:00")
        start: String,

        @P("End date and time in ISO-8601 format, e.g. 2026-08-10T15:00:00")
        end: String
    ): String {
        logger.info("Using getAllEventInTime tool")
        val events = calendarService.getAllEventsInTime(
            start = LocalDateTime.parse(start),
            end = LocalDateTime.parse(end)
        )

        if (events.isEmpty()) {
            return "No events found in the specified time range."
        }

        val formattedEvents = events.joinToString("\n\n") { event ->
            """
                Événement : ${event.summary ?: "Sans titre"}
                Début : ${event.start?.dateTime}
                Fin : ${event.end?.dateTime}
                Lieu : ${event.location ?: "Non renseigné"}
                Lien : ${event.htmlLink ?: "Non disponible"}
            """.trimIndent()
        }

        return formattedEvents
    }
}


