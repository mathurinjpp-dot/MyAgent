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

        @Tool("Créer un événement récurrent dans le calendrier Google de l'utilisateur.")
    fun createRecurrentEvent(
        @P("Titre de l'événement") title: String,
        @P("Description détaillée de l'événement") description: String?,
        @P("Date et heure de début au format ISO-8601, ex. 2026-08-10T14:00:00")
        start: String,
        @P("Date et heure de fin au format ISO-8601, ex. 2026-08-10T15:00:00")
        end: String,
        @P("Lieu de l'événement") location: String?,
        @P("Règle de récurrence, ex. \"RRULE:FREQ=DAILY;INTERVAL=1\"")
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

    @Tool("Modifier un événement existant avec une règle de récurrence optionnelle." +
            " Si une règle de récurrence est fournie, l'événement devient récurrent." +
            " Si null est fourni, la récurrence existante est supprimée.")
    fun updateEventWithRecurrence(
        @P("ID de l'événement à modifier") eventId: String,
        @P("Nouveau titre (null pour conserver l'actuel)") newTitle: String?,
        @P("Nouvelle description (null pour conserver l'actuelle)") newDescription: String?,
        @P("Nouvelle date/heure de début (null pour conserver l'actuelle)") newStart: String?,
        @P("Nouvelle date/heure de fin (null pour conserver l'actuelle)") newEnd: String?,
        @P("Nouveau lieu (null pour conserver l'actuel)") newLocation: String?,
        @P("Nouvelle règle de récurrence (null pour supprimer la récurrence, ex. \"RRULE:FREQ=DAILY\")")
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

    @Tool("Créer une récurrence à partir d'un événement existant." +
            " Crée un nouvel événement récurrent basé sur un événement existant avec la règle de récurrence spécifiée.")
    fun createRecurrence(
        @P("ID de l'événement source") eventId: String,
        @P("Règle de récurrence, ex. \"RRULE:FREQ=DAILY;INTERVAL=1\"")
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

    @Tool("Modifier un événement existant dans le calendrier Google." +
            " L'événement est identifié par un mot-clé de son titre et sa date approximative." +
            " Utilisez un mot-clé clair et unique (ex. « sprint » plutôt que « réunion sprint dev »)." +
            " Seuls les champs fournis sont modifiés, les autres restent inchangés.")
    fun modifyEvent(
        @P("Mot-clé du titre de l'événement à modifier (un seul mot recommandé)") title: String,

        @P("Date approximative de l'événement au format ISO-8601, ex. 2026-08-10T14:00:00")
        date: String,

        @P("Nouveau titre de l'événement (null pour conserver l'actuel)") newTitle: String?,

        @P("Nouvelle description de l'événement (null pour conserver l'actuelle)") newDescription: String?,

        @P("Nouvelle date et heure de début au format ISO-8601 (null pour conserver l'actuelle)") newStart: String?,

        @P("Nouvelle date et heure de fin au format ISO-8601 (null pour conserver l'actuelle)") newEnd: String?,

        @P("Nouveau lieu de l'événement (null pour conserver l'actuel)") newLocation: String?
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

    @Tool("Supprimer un événement du calendrier Google." +
            " L'événement est identifié par un mot-clé de son titre et sa date approximative." +
            " Utilisez un mot-clé clair et unique (ex. « sprint » plutôt que « réunion sprint dev »).")
    fun deleteEvent(
        @P("Mot-clé du titre de l'événement à supprimer (un seul mot recommandé)") title: String,

        @P("Date approximative de l'événement au format ISO-8601, ex. 2026-08-10T14:00:00")
        date: String
    ): String {
        logger.info("Using deleteEvent tool")

        val searchDate = LocalDateTime.parse(date)
        val event = findEvent(title, searchDate)
            ?: return "Aucun événement trouvé contenant \"$title\" autour du ${searchDate.toLocalDate()}."

        calendarService.deleteEvent(event.id)

        return "Événement \"${event.summary}\" supprimé avec succès."
    }

    @Tool("Supprimer plusieurs événements du calendrier Google." +
            " Les événements peuvent être filtrés par un mot-clé du titre dans une plage horaire." +
            " Utilisez un mot-clé clair et unique (ex. « sprint » plutôt que « réunion sprint dev »)." +
            " Si aucun titre n'est fourni, tous les événements de la plage seront supprimés.")
    fun deleteEvents(
        @P("Mot-clé optionnel pour filtrer les événements par titre (un seul mot recommandé). Si null, tous les événements de la plage sont supprimés.") title: String?,

        @P("Début de la plage horaire au format ISO-8601, ex. 2026-08-10T00:00:00") start: String,

        @P("Fin de la plage horaire au format ISO-8601, ex. 2026-08-10T23:59:59") end: String
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
