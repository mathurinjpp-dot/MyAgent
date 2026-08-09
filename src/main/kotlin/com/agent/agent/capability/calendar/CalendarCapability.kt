package com.agent.agent.capability.calendar

import com.agent.agent.capability.AgentCapability
import com.agent.agent.capability.AgentCapabilityMatcher
import com.agent.agent.capability.calendar.matcher.CalendarCapabilityMatcher
import com.agent.agent.capability.calendar.tools.CalendarTool
import org.springframework.stereotype.Component

@Component
class CalendarCapability(
    private val calendarTool: CalendarTool
) : AgentCapability {

    override val name = "calendar"

    override val description = """
        Gestion du calendrier Google de l'utilisateur :
        création, modification, suppression et recherche d'événements,
        y compris les événements récurrents.
    """.trimIndent()

    override fun context(): String = """
        CONTEXTE CALENDRIER

        Tu manipules le calendrier personnel de l'utilisateur.

        Règles :

        - Sois autonome.
        - Ne demande pas d'information optionnelle.
        - Le lieu est facultatif.
        - Si l'utilisateur donne une heure approximative, fais une interprétation raisonnable.
        - "le matin" peut être interprété comme environ 09:00.
        - "l'après-midi" comme environ 14:00.
        - "le soir" comme environ 20:00.
        - Si une durée raisonnable peut être déduite, déduis-la.
        - Ne demande une précision que si elle est réellement indispensable.
        - Utilise les outils Calendar dès que nécessaire.
    """.trimIndent()

    override fun tools(): CalendarTool = calendarTool
    override fun matcher(): AgentCapabilityMatcher {
        return CalendarCapabilityMatcher()
    }
}