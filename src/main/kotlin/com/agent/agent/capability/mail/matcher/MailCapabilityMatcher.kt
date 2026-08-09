package com.agent.agent.capability.mail.matcher

import com.agent.agent.capability.AgentCapabilityMatcher
import org.springframework.stereotype.Component

@Component
class MailCapabilityMatcher : AgentCapabilityMatcher {

    private val keywords = listOf(
        "mail",
        "email",
        "e-mail",
        "courriel",
        "message",
        "messagerie"
    )

    private val actions = listOf(
        "envoyer",
        "envoie",
        "écrire",
        "ecris",
        "écrire",
        "rédiger",
        "redige",
        "rédige",
        "recevoir",
        "reçu",
        "reçois",
        "lire",
        "lis",
        "consulter",
        "regarder",
        "voir",
        "chercher",
        "rechercher",
        "supprimer",
        "effacer",
        "archiver",
        "répondre",
        "réponds",
        "reply"
    )

    override fun matches(message: String): Boolean {
        val normalized = message
            .lowercase()
            .trim()

        val hasMailKeyword = keywords.any {
            normalized.contains(it)
        }

        val hasMailAction = actions.any {
            normalized.contains(it)
        }

        return hasMailKeyword && hasMailAction
    }
}