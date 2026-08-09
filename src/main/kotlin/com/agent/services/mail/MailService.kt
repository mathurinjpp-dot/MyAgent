package com.agent.services.mail

import com.agent.services.mail.model.Mail

interface MailService {

    fun listEmails(maxResults: Int = 20): List<Mail>

    fun sendEmail(
        to: String,
        subject: String,
        body: String
    )

    fun deleteEmail(
        messageId: String
    )
}

