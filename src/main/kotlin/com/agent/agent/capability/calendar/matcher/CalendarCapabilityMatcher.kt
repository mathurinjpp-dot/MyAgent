package com.agent.agent.capability.calendar.matcher

import com.agent.agent.capability.AgentCapabilityMatcher
import java.text.Normalizer

class CalendarCapabilityMatcher : AgentCapabilityMatcher {

    override fun matches(message: String): Boolean {
        val text = normalize(message)

        // 1. Référence explicite au calendrier / agenda
        if (containsAny(text, CALENDAR_WORDS)) {
            return true
        }

        // 2. Demande de consultation de l'agenda
        //
        // Ex:
        // "qu'est-ce que j'ai demain ?"
        // "montre mes événements"
        // "quelles sont mes réunions cette semaine ?"
        val hasCalendarQuery =
            containsAny(text, CALENDAR_QUERY_INTENTS)

        val hasEvent =
            containsAny(text, EVENT_WORDS)

        val hasTemporalContext =
            containsAny(text, TEMPORAL_WORDS)

        if (hasCalendarQuery && (hasEvent || hasTemporalContext)) {
            return true
        }

        // 3. Création / planification
        //
        // Ex:
        // "planifie une réunion demain"
        // "prévois un rendez-vous mardi"
        // "ajoute un événement à 15h"
        val hasPlanningIntent =
            containsAny(text, PLANNING_INTENTS)

        val hasTime =
            TIME_PATTERN.containsMatchIn(text)

        val hasDate = DATE_PATTERN.containsMatchIn(text)

        if (hasPlanningIntent && (hasEvent || hasTemporalContext || hasTime || hasDate)) {
            return true
        }

        // 4. Modification / suppression
        //
        // Ex:
        // "annule ma réunion"
        // "déplace mon rendez-vous à 17h"
        // "modifie mon événement"
        val hasModificationIntent =
            containsAny(text, MODIFICATION_INTENTS)

        if (hasModificationIntent && hasEvent) {
            return true
        }

        // 5. Recherche de disponibilité
        //
        // Ex:
        // "quand suis-je libre ?"
        // "trouve-moi un créneau demain"
        if (containsAny(text, AVAILABILITY_INTENTS)) {
            return true
        }

        // 6. Recherche explicite d'événements
        //
        // Ex:
        // "cherche ma réunion avec Paul"
        // "retrouve mon rendez-vous"
        val hasSearchIntent =
            containsAny(text, SEARCH_INTENTS)

        if (hasSearchIntent && hasEvent) {
            return true
        }

        return false
    }

    private fun normalize(value: String): String {
        return Normalizer
            .normalize(value.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .replace("’", "'")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun containsAny(
        text: String,
        values: Set<String>
    ): Boolean {
        return values.any { it in text }
    }

    companion object {

        /**
         * Référence directe au calendrier.
         *
         * Ces termes sont suffisamment explicites pour déclencher
         * la capability à eux seuls.
         */
        private val CALENDAR_WORDS = setOf(
            "calendrier",
            "calendar",
            "agenda",
            "emploi du temps",
            "planning"
        )

        /**
         * Intentions de consultation du calendrier.
         */
        private val CALENDAR_QUERY_INTENTS = setOf(
            "qu'est-ce que j'ai",
            "qu'est ce que j'ai",
            "j'ai quoi",
            "montre-moi",
            "montre moi",
            "affiche",
            "quels sont mes",
            "quelles sont mes"
        )

        /**
         * Entités typiquement liées au calendrier.
         */
        private val EVENT_WORDS = setOf(
            "evenement",
            "evenements",
            "rendez-vous",
            "rendez vous",
            "reunion",
            "reunions",
            "meeting",
            "meetings",
            "appointment",
            "appointments",
            "appel",
            "appels",
            "conference",
            "dejeuner",
            "diner",
            "creneau",
            "creneaux",
            "date",
            "seance",
            "seances"
        )

        /**
         * Intentions de création / planification.
         */
        private val PLANNING_INTENTS = setOf(
            "planifie",
            "planifier",
            "planification",
            "prevois",
            "prevoir",
            "programme",
            "programmer",
            "ajoute",
            "ajouter",
            "cree",
            "creer",
            "organise",
            "organiser",
            "reserve",
            "reserver",
            "bloque",
            "bloquer"
        )

        /**
         * Intentions de modification / suppression.
         */
        private val MODIFICATION_INTENTS = setOf(
            "modifie",
            "modifier",
            "change",
            "changer",
            "deplace",
            "deplacer",
            "decale",
            "decaler",
            "annule",
            "annuler",
            "supprime",
            "supprimer",
            "efface",
            "effacer",
            "reprogramme",
            "reprogrammer",
            "replanifie",
            "replanifier"
        )

        /**
         * Intentions de disponibilité.
         */
        private val AVAILABILITY_INTENTS = setOf(
            "quand suis-je libre",
            "quand suis je libre",
            "quand je suis libre",
            "suis-je libre",
            "suis je libre",
            "est-ce que je suis libre",
            "est ce que je suis libre",
            "trouve-moi un creneau",
            "trouve moi un creneau",
            "cherche un creneau",
            "quel creneau",
            "quels creneaux",
            "creneau disponible",
            "creneaux disponibles",
            "mes disponibilites",
            "ma disponibilite"
        )

        /**
         * Intentions de recherche d'un événement.
         */
        private val SEARCH_INTENTS = setOf(
            "cherche",
            "chercher",
            "recherche",
            "rechercher",
            "retrouve",
            "retrouver"
        )

        /**
         * Expressions temporelles suffisamment précises.
         *
         * Attention : elles ne déclenchent JAMAIS Calendar toutes seules.
         */
        private val TEMPORAL_WORDS = setOf(
            "aujourd'hui",
            "aujourd hui",
            "demain",
            "apres-demain",
            "apres demain",
            "ce soir",
            "ce matin",
            "cet apres-midi",
            "cet apres midi",
            "cette semaine",
            "la semaine prochaine",
            "semaine prochaine",
            "la semaine derniere",
            "semaine derniere",
            "ce week-end",
            "ce weekend",
            "week-end prochain",
            "weekend prochain",
            "lundi prochain",
            "mardi prochain",
            "mercredi prochain",
            "jeudi prochain",
            "vendredi prochain",
            "samedi prochain",
            "dimanche prochain"
        )

        /**
         * Détecte les dates absolues :
         *
         * 15 aout
         * 15 aout 2026
         * septembre 2026
         */
        private val DATE_PATTERN = Regex(
            """\b\d{1,2}\s+(?:janvier|fevrier|mars|avril|mai|juin|juillet|aout|septembre|octobre|novembre|decembre)(?:\s+\d{4})?\b""" +
            """|\b(?:janvier|fevrier|mars|avril|mai|juin|juillet|aout|septembre|octobre|novembre|decembre)(?:\s+\d{4})?\b"""
        )

        /**
         * Détecte :
         *
         * 15h
         * 15h30
         * 09:00
         * 9:30
         * 23h
         */
        private val TIME_PATTERN = Regex(
            """\b(?:[01]?\d|2[0-3])(?:h(?:[0-5]\d)?|:[0-5]\d)\b"""
        )
    }
}