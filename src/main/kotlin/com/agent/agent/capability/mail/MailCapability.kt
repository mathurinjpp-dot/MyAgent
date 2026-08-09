package com.agent.agent.capability.mail

import com.agent.agent.capability.AgentCapability
import com.agent.agent.capability.mail.matcher.MailCapabilityMatcher
import com.agent.agent.capability.mail.tools.MailTool
import org.springframework.stereotype.Component

@Component
class MailCapability(private val mailTool : MailTool, private val mailCapabilityMatcher: MailCapabilityMatcher) : AgentCapability {
    override val name = "mail"
    override val description = "the mail capability"

    override fun context() =
        """CONTEXTE MAIL
        Tu peux supprimer envoyer ou lister des mails, soit intelligent si on te demande 
        de supprimer les email de test cherche les ids d'email ou il y a test dedans et supprime les avec 
        le tools de suppresion, tu peux avoir des doutes sur le destinataire des mails.
        Quand à la description tu peux la faire bien toi même n'hésite pas
                
        """.trimIndent()

    override fun tools(): MailTool = mailTool

    override fun matcher(): MailCapabilityMatcher {
        return mailCapabilityMatcher
    }
}




