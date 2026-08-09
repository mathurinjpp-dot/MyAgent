package com.agent.calendar.services.impl

import com.agent.calendar.services.CalendarService
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

