package com.agent.agent.capability

interface AgentCapabilityMatcher {

    fun matches(message : String) : Boolean
}
