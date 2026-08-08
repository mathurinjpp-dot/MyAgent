package com.agent.calendar.services

import com.google.api.services.calendar.model.Event
import java.time.LocalDateTime

interface CalendarService {
    fun createEvent(title: String, description: String?, start: LocalDateTime, end: LocalDateTime, location: String?) : Event
}
