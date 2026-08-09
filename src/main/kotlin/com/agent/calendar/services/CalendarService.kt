package com.agent.calendar.services

import com.google.api.services.calendar.model.Event
import java.time.LocalDateTime

interface CalendarService {
    fun createEvent(title: String, description: String?, start: LocalDateTime, end: LocalDateTime, location: String?) : Event
    fun getAllEventsInTime(start: LocalDateTime, end: LocalDateTime): List<Event>
    fun getEventById(eventId: String): Event
    fun getEventByTitle(title: String, start: LocalDateTime, end: LocalDateTime): Event?
    fun updateEvent(eventId: String, event: Event): Event
    fun updateEventWithRecurrence(eventId: String, event: Event, recurrencePattern: String?): Event
    fun deleteEvent(eventId: String)
    fun deleteEvents(eventIds: List<String>)
    fun createRecurrence(eventId: String, recurrencePattern: String): Event
    fun createRecurrentEvent(
        title: String,
        description: String?,
        start: LocalDateTime,
        end: LocalDateTime,
        location: String?,
        recurrenceRules: List<String>
    ): Event
}
