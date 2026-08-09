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

    @Tool("List emails from the mailbox")
    fun getEmails(
        @P("limit number of messages") limit : Int,
    ) : List<Mail> {
        logger.info("using getEmails tool")
        return mailService.listEmails(limit)
    }

    @Tool("Send an email")
    fun sendEmail(
        @P("recipient email address") to : String,
        @P("email subject") subject : String,
        @P("email body content") body : String,
    ) {
        logger.info("using sendEmail tool")
        mailService.sendEmail(to, subject, body)
    }

    @Tool("Delete an email by its message ID")
    fun deleteEmail(
        @P("the message ID to delete") messageId : String,
    ) {
        logger.info("using deleteEmail tool")
        mailService.deleteEmail(messageId)
    }
}
