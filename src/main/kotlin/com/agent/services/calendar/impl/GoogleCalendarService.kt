package com.agent.services.calendar.impl

import com.agent.services.calendar.CalendarService
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class GoogleCalendarService(private val calendar: Calendar) : CalendarService {

    private val calendarId = "primary"

    private val zone = ZoneId.of("Europe/Paris")

    override fun createEvent(
        title: String,
        description: String?,
        start: LocalDateTime,
        end: LocalDateTime,
        location: String?
    ): Event {

        val event = Event()
            .setSummary(title)
            .setDescription(description)
            .setLocation(location)

        event.start = EventDateTime()
            .setDateTime(toGoogleDateTime(start))
            .setTimeZone(zone.id)

        event.end = EventDateTime()
            .setDateTime(toGoogleDateTime(end))
            .setTimeZone(zone.id)

        return calendar.events()
            .insert(calendarId, event)
            .execute()
    }

    override fun getAllEventsInTime(start: LocalDateTime, end: LocalDateTime): List<Event> {
        val timeMin = toGoogleDateTime(start)
        val timeMax = toGoogleDateTime(end)

        val events = calendar.events()
            .list(calendarId)
            .setTimeMin(timeMin)
            .setTimeMax(timeMax)
            .setTimeZone(zone.id)
            .execute()

        return events.items ?: emptyList()
    }

    override fun getEventById(eventId: String): Event {
        return calendar.events().get(calendarId, eventId).execute()
    }

    override fun getEventByTitle(title: String, start: LocalDateTime, end: LocalDateTime): Event? {
        return getAllEventsInTime(start, end).firstOrNull {
            it.summary?.contains(title, ignoreCase = true) == true
        }
    }

    override fun updateEvent(eventId: String, event: Event): Event {
        return calendar.events().update(calendarId, eventId, event).execute()
    }

    override fun deleteEvent(eventId: String) {
        calendar.events().delete(calendarId, eventId).execute()
    }

    override fun deleteEvents(eventIds: List<String>) {
        eventIds.forEach { eventId ->
            calendar.events().delete(calendarId, eventId).execute()
        }
    }

    override fun updateEventWithRecurrence(eventId: String, event: Event, recurrencePattern: String?): Event {
        event.recurrence = if (recurrencePattern != null) listOf(recurrencePattern) else null
        return calendar.events().update(calendarId, eventId, event).execute()
    }

    override fun createRecurrence(eventId: String, recurrencePattern: String): Event {
        val originalEvent = getEventById(eventId)
        val recurrentEvent = Event()
            .setSummary(originalEvent.summary)
            .setDescription(originalEvent.description)
            .setLocation(originalEvent.location)
            .setStart(originalEvent.start)
            .setEnd(originalEvent.end)

        recurrentEvent.recurrence = listOf(recurrencePattern)

        return calendar.events()
            .insert(calendarId, recurrentEvent)
            .execute()
    }

    override fun createRecurrentEvent(
        title: String,
        description: String?,
        start: LocalDateTime,
        end: LocalDateTime,
        location: String?,
        recurrenceRules: List<String>
    ): Event {
        val event = Event()
            .setSummary(title)
            .setDescription(description)
            .setLocation(location)

        event.start = EventDateTime()
            .setDateTime(toGoogleDateTime(start))
            .setTimeZone(zone.id)

        event.end = EventDateTime()
            .setDateTime(toGoogleDateTime(end))
            .setTimeZone(zone.id)

        event.recurrence = recurrenceRules

        return calendar.events()
            .insert(calendarId, event)
            .execute()
    }

    private fun toGoogleDateTime(
        dateTime: LocalDateTime
    ): DateTime {
        return DateTime(
            dateTime
                .atZone(zone)
                .toInstant()
                .toEpochMilli()
        )
    }
}