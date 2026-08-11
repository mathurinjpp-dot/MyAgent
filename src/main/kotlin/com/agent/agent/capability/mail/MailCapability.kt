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
        """CONTEXTE — EMAILS

Les outils disponibles dans ce contexte permettent d'interagir avec des emails.

Utilise les outils appropriés en fonction de la demande de l'utilisateur.
Lorsque l'utilisateur demande une action destructive, commence par identifier précisément les éléments concernés avant d'utiliser l'outil de suppression.
Pour une demande comme « supprime les emails de test », recherche d'abord les emails correspondant au critère « test », récupère leurs IDs, puis utilise l'outil de suppression avec les IDs trouvés.
Ne supprime pas d'emails sur une simple supposition : les emails supprimés doivent correspondre au critère demandé.
Si le destinataire d'un email est ambigu ou si plusieurs personnes peuvent correspondre, demande une clarification plutôt que de choisir arbitrairement.
Pour la rédaction d'un email, déduis toi-même une formulation adaptée à partir des informations fournies par l'utilisateur. Inutile de lui demander de rédiger le contenu s'il a déjà donné suffisamment de contexte.
Adapte naturellement le ton, le niveau de formalité et la formulation au contexte de l'utilisateur.
Pour la description ou le contenu d'un email, tu peux prendre des initiatives de rédaction tant que tu respectes l'intention de l'utilisateur.
N'invente pas les informations nécessaires à l'exécution d'une action.
Lorsque la demande est claire, évite les demandes de confirmation inutiles.
        """.trimIndent()

    override fun tools(): MailTool = mailTool

    override fun matcher(): MailCapabilityMatcher {
        return mailCapabilityMatcher
    }
}




