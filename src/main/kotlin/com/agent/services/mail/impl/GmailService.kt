package com.agent.services.mail.impl

import com.agent.services.mail.MailService
import com.agent.services.mail.model.Mail
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.Properties

@Service
class GoogleMailService(
    private val gmail: Gmail
) : MailService {

    override fun listEmails(maxResults: Int): List<Mail> {
        val response = gmail.users()
            .messages()
            .list("me")
            .setMaxResults(maxResults.toLong())
            .execute()

        return response.messages
            ?.map { message ->
                getMail(message.id)
            }
            ?: emptyList()
    }

    private fun getMail(messageId: String): Mail {
        val message = gmail.users()
            .messages()
            .get("me", messageId)
            .setFormat("metadata")
            .setMetadataHeaders(
                listOf(
                    "From",
                    "To",
                    "Subject"
                )
            )
            .execute()

        val headers = message.payload
            ?.headers
            ?.associate {
                it.name.lowercase() to it.value
            }
            .orEmpty()

        return Mail(
            id = message.id,
            threadId = message.threadId,
            from = headers["from"],
            to = headers["to"],
            subject = headers["subject"],
            snippet = message.snippet
        )
    }

    override fun sendEmail(
        to: String,
        subject: String,
        body: String
    ) {
        val session = Session.getDefaultInstance(
            Properties(),
            null
        )

        val email = MimeMessage(session).apply {
            setFrom(InternetAddress("me"))

            setRecipient(
                jakarta.mail.Message.RecipientType.TO,
                InternetAddress(to)
            )

            setSubject(
                subject,
                Charsets.UTF_8.name()
            )

            setText(
                body,
                Charsets.UTF_8.name()
            )
        }

        val output = ByteArrayOutputStream()

        email.writeTo(output)

        val encodedEmail = Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                output.toByteArray()
            )

        val message = Message()
            .setRaw(encodedEmail)

        gmail.users()
            .messages()
            .send("me", message)
            .execute()
    }

    override fun deleteEmail(
        messageId: String
    ) {
        gmail.users()
            .messages()
            .trash("me", messageId)
            .execute()
    }
}
