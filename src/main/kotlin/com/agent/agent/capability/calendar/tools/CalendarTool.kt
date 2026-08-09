package com.agent.agent.capability.calendar.tools

import com.agent.agent.capability.MyTool
import com.agent.services.calendar.CalendarService
import com.agent.core.utils.logger
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class CalendarTool(private val calendarService: CalendarService) : MyTool {
    private val logger = logger()
    private val zone = "Europe/Paris"
    private val zoneId = ZoneId.of(zone)

        @Tool("Create a recurrent event in the user's Google Calendar.")
    fun createRecurrentEvent(
        @P("Event title") title: String,
        @P("Detailed description of the event") description: String?,
        @P("Start date and time in ISO-8601 format, e.g. 2026-08-10T14:00:00")
        start: String,
        @P("End date and time in ISO-8601 format, e.g. 2026-08-10T15:00:00")
        end: String,
        @P("Location of the event") location: String?,
        @P("Recurrence rules, e.g. \"RRULE:FREQ=DAILY;INTERVAL=1\"")
        recurrencePattern: String
    ): String {
        logger.info("Using createRecurrentEvent tool")

        val event = calendarService.createRecurrentEvent(
            title = title,
            description = description,
            start = LocalDateTime.parse(start),
            end = LocalDateTime.parse(end),
            location = location,
            recurrenceRules = listOf(recurrencePattern)
        )

        return """
            Événement récurrent créé avec succès.
            Titre : ${event.summary}
            Début : ${event.start.dateTime}
            Fin : ${event.end.dateTime}
            Lieu : ${event.location ?: "Non renseigné"}
            Récurrence : ${event.recurrence}
            Lien : ${event.htmlLink ?: "Non disponible"}
        """.trimIndent()
    }

    @Tool("Update an existing event with optional recurrence pattern." +
            " If a recurrence pattern is provided, the event will become a recurring event." +
            " If null is provided, the existing recurrence will be removed.")
    fun updateEventWithRecurrence(
        @P("Event ID to update") eventId: String,
        @P("New title (null to keep current)") newTitle: String?,
        @P("New description (null to keep current)") newDescription: String?,
        @P("New start date/time (null to keep current)") newStart: String?,
        @P("New end date/time (null to keep current)") newEnd: String?,
        @P("New location (null to keep current)") newLocation: String?,
        @P("New recurrence pattern (null to remove recurrence, e.g. \"RRULE:FREQ=DAILY\")")
        recurrencePattern: String?
    ): String {
        logger.info("Using updateEventWithRecurrence tool")

        val existingEvent = calendarService.getEventById(eventId)

        newTitle?.let { existingEvent.summary = it }
        newDescription?.let { existingEvent.description = it }
        newLocation?.let { existingEvent.location = it }
        newStart?.let {
            existingEvent.start = EventDateTime()
                .setDateTime(toGoogleDateTime(LocalDateTime.parse(it)))
                .setTimeZone(zone)
        }
        newEnd?.let {
            existingEvent.end = EventDateTime()
                .setDateTime(toGoogleDateTime(LocalDateTime.parse(it)))
                .setTimeZone(zone)
        }

        val updated = calendarService.updateEventWithRecurrence(eventId, existingEvent, recurrencePattern)

        return """
            Événement modifié avec succès.
            Titre : ${updated.summary}
            Début : ${updated.start.dateTime}
            Fin : ${updated.end.dateTime}
            Lieu : ${updated.location ?: "Non renseigné"}
            Récurrence : ${updated.recurrence}
            Lien : ${updated.htmlLink ?: "Non disponible"}
        """.trimIndent()
    }

    @Tool("Create a recurrence based on an existing event." +
            " This creates a new recurrent event based on an existing one with the specified recurrence pattern.")
    fun createRecurrence(
        @P("Event ID to create recurrence from") eventId: String,
        @P("Recurrence rules, e.g. \"RRULE:FREQ=DAILY;INTERVAL=1\"")
        recurrencePattern: String
    ): String {
        logger.info("Using createRecurrence tool")

        val recurrentEvent = calendarService.createRecurrence(eventId, recurrencePattern)

        return """
            Événement récurrent créé avec succès.
            ID original : ${recurrentEvent.id}
            Titre : ${recurrentEvent.summary}
            Début : ${recurrentEvent.start.dateTime}
            Fin : ${recurrentEvent.end.dateTime}
            Récurrence : ${recurrentEvent.recurrence}
            Lien : ${recurrentEvent.htmlLink ?: "Non disponible"}
        """.trimIndent()
    }

    @Tool("Update an existing event in the user's Google Calendar." +
            " The event is identified by a keyword from its title and its approximate date." +
            " Use a single clear keyword (e.g. \"sprint\" not \"réunion sprint dev\")." +
            " Only the provided fields will be modified, the others remain unchanged.")
    fun modifyEvent(
        @P("Keyword from the title of the event to modify (single clear word recommended)") title: String,

        @P("Approximate date of the event in ISO-8601 format, e.g. 2026-08-10T14:00:00")
        date: String,

        @P("New title for the event (null to keep current)") newTitle: String?,

        @P("New description for the event (null to keep current)") newDescription: String?,

        @P("New start date and time in ISO-8601 format (null to keep current)") newStart: String?,

        @P("New end date and time in ISO-8601 format (null to keep current)") newEnd: String?,

        @P("New location for the event (null to keep current)") newLocation: String?
    ): String {
        logger.info("Using modifyEvent tool")

        val searchDate = LocalDateTime.parse(date)
        val event = findEvent(title, searchDate)
            ?: return "Aucun événement trouvé contenant \"$title\" autour du ${searchDate.toLocalDate()}."

        val existingEvent = calendarService.getEventById(event.id)

        newTitle?.let { existingEvent.summary = it }
        newDescription?.let { existingEvent.description = it }
        newLocation?.let { existingEvent.location = it }
        newStart?.let {
            existingEvent.start = EventDateTime()
                .setDateTime(toGoogleDateTime(LocalDateTime.parse(it)))
                .setTimeZone(zone)
        }
        newEnd?.let {
            existingEvent.end = EventDateTime()
                .setDateTime(toGoogleDateTime(LocalDateTime.parse(it)))
                .setTimeZone(zone)
        }

        val updated = calendarService.updateEvent(event.id, existingEvent)

        return """
            Événement modifié avec succès.
            Titre : ${updated.summary}
            Début : ${updated.start.dateTime}
            Fin : ${updated.end.dateTime}
            Lieu : ${updated.location ?: "Non renseigné"}
            Lien : ${updated.htmlLink ?: "Non disponible"}
        """.trimIndent()
    }

    @Tool("Delete a single event from the user's Google Calendar." +
            " The event is identified by a keyword from its title and its approximate date." +
            " Use a single clear keyword (e.g. \"sprint\" not \"réunion sprint dev\").")
    fun deleteEvent(
        @P("Keyword from the title of the event to delete (single clear word recommended)") title: String,

        @P("Approximate date of the event in ISO-8601 format, e.g. 2026-08-10T14:00:00")
        date: String
    ): String {
        logger.info("Using deleteEvent tool")

        val searchDate = LocalDateTime.parse(date)
        val event = findEvent(title, searchDate)
            ?: return "Aucun événement trouvé contenant \"$title\" autour du ${searchDate.toLocalDate()}."

        calendarService.deleteEvent(event.id)

        return "Événement \"${event.summary}\" supprimé avec succès."
    }

    @Tool("Delete multiple events from the user's Google Calendar." +
            " Events can be filtered by a keyword from their title within a time range." +
            " Use a single clear keyword (e.g. \"sprint\" not \"réunion sprint dev\")." +
            " If no title is provided, all events in the time range will be deleted.")
    fun deleteEvents(
        @P("Optional keyword to filter events by title (single clear word recommended). If null, all events in range are deleted.") title: String?,

        @P("Start of the time range in ISO-8601 format, e.g. 2026-08-10T00:00:00") start: String,

        @P("End of the time range in ISO-8601 format, e.g. 2026-08-10T23:59:59") end: String
    ): String {
        logger.info("Using deleteEvents tool")

        val events = calendarService.getAllEventsInTime(
            start = LocalDateTime.parse(start),
            end = LocalDateTime.parse(end)
        )

        val filteredEvents = if (!title.isNullOrBlank()) {
            events.filter { it.summary?.contains(title, ignoreCase = true) == true }
        } else {
            events
        }

        if (filteredEvents.isEmpty()) {
            return "Aucun événement trouvé dans la plage donnée" +
                    if (!title.isNullOrBlank()) " contenant \"$title\"." else "."
        }

        val eventIds = filteredEvents.map { it.id }
        calendarService.deleteEvents(eventIds)

        return "${eventIds.size} événement(s) supprimé(s) avec succès."
    }

    private fun findEvent(title: String, date: LocalDateTime): Event? {
        val events = calendarService.getAllEventsInTime(
            start = date.minusDays(1),
            end = date.plusDays(1)
        )
        return events.firstOrNull {
            it.summary?.contains(title, ignoreCase = true) == true
        }
    }

    private fun toGoogleDateTime(dateTime: LocalDateTime): DateTime {
        return DateTime(
            dateTime
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli()
        )
    }
}
