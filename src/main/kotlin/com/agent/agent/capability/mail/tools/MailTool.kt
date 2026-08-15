package com.agent.agent.capability.mail.tools

import com.agent.agent.capability.MyTool
import com.agent.core.utils.logger
import com.agent.services.mail.MailService
import com.agent.services.mail.model.Mail
import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import org.springframework.stereotype.Component

@Component
class MailTool(private val mailService : MailService) : MyTool{
    private val logger = logger()

    @Tool("Lister les emails de la boîte mail")
    fun getEmails(
        @P("Nombre maximum de messages à récupérer") limit : Int,
    ) : List<Mail> {
        logger.info("using getEmails tool")
        return mailService.listEmails(limit)
    }

    @Tool("Envoyer un email")
    fun sendEmail(
        @P("Adresse email du destinataire") to : String,
        @P("Objet de l'email") subject : String,
        @P("Contenu du corps de l'email") body : String,
    ) {
        logger.info("using sendEmail tool")
        mailService.sendEmail(to, subject, body)
    }

    @Tool("Supprimer un email par son ID de message")
    fun deleteEmail(
        @P("ID du message à supprimer") messageId : String,
    ) {
        logger.info("using deleteEmail tool")
        mailService.deleteEmail(messageId)
    }
}
