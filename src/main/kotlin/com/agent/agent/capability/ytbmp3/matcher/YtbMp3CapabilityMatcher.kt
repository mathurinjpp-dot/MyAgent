package com.agent.agent.capability.ytbmp3.matcher

import com.agent.agent.capability.AgentCapabilityMatcher
import org.springframework.stereotype.Component

@Component
class YtbMp3CapabilityMatcher : AgentCapabilityMatcher {
    override fun matches(message: String): Boolean {
        return true
    }
}