package com.agent.agent.capability.mail.matcher

import com.agent.agent.capability.AgentCapabilityMatcher
import org.springframework.stereotype.Component

@Component
class MailCapabilityMatcher : AgentCapabilityMatcher {
    override fun matches(message: String): Boolean {
        return true
    }
}
