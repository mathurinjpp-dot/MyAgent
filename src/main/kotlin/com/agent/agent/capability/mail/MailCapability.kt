package com.agent.agent.capability.mail

import com.agent.agent.capability.AgentCapability
import com.agent.agent.capability.mail.matcher.MailCapabilityMatcher
import com.agent.agent.capability.mail.tools.MailTool
import org.springframework.stereotype.Component

@Component
class MailCapability(private val mailTool : MailTool, private val mailCapabilityMatcher: MailCapabilityMatcher) : AgentCapability {
    override val name = "mail"
    override val description = "Gestion des emails : lecture, envoi et suppression."

    override fun context() = """
        CONTEXTE EMAILS

        Tu gères les emails de Mathurin via l'API Gmail.

        Règles de comportement :
        - Utilise les outils appropriés en fonction de la demande.
        - Pour une action de suppression, identifie précisément les emails concernés avant de les supprimer.
        - Par exemple, pour « supprime les emails de test », recherche d'abord les emails correspondant au critère, récupère leurs IDs, puis utilise l'outil de suppression.
        - Ne supprime jamais d'emails sur une simple supposition.
        - Si le destinataire est ambigu, demande une clarification.
        - Pour la rédaction d'un email, déduis une formulation adaptée à partir du contexte fourni.
        - Adapte le ton et le niveau de formalité au contexte.
        - Tu peux prendre des initiatives de rédaction tant que tu respectes l'intention de Mathurin.
        - N'invente jamais les informations nécessaires à l'exécution d'une action.
        - Lorsque la demande est claire, évite les confirmations inutiles.
    """.trimIndent()

    override fun tools(): MailTool = mailTool

    override fun matcher(): MailCapabilityMatcher {
        return mailCapabilityMatcher
    }
}




