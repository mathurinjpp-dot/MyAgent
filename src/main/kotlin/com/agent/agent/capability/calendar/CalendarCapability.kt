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
        Gestion du calendrier Google de Mathurin :
        création, modification, suppression et recherche d'événements,
        y compris les événements récurrents.
    """.trimIndent()

    override fun context(): String = """
        CONTEXTE CALENDRIER

        Tu gères le calendrier personnel de Mathurin via l'API Google Calendar.

        Règles de comportement :
        - Sois autonome : ne demande pas d'information optionnelle.
        - Le lieu est facultatif.
        - Si une heure approximative est donnée, interprète-la raisonnablement :
          « le matin » ≈ 09h00, « l'après-midi » ≈ 14h00, « le soir » ≈ 20h00.
        - Déduis une durée raisonnable si elle peut être raisonnablement estimée.
        - Ne demande une précision que si elle est réellement indispensable.
        - Utilise les outils Calendar dès que nécessaire.
    """.trimIndent()

    override fun tools(): CalendarTool = calendarTool
    override fun matcher(): AgentCapabilityMatcher {
        return CalendarCapabilityMatcher()
    }
}