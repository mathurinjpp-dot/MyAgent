package com.agent.services.mail.model

data class Mail(
    val id: String,
    val threadId: String?,
    val from: String?,
    val to: String?,
    val subject: String?,
    val snippet: String?
) {
    override fun toString(): String {
        return "Mail(id=$id, from=$from, to=$to, subject=$subject, snippet=$snippet)"
    }
}
