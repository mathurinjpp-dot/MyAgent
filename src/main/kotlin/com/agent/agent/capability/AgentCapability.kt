package com.agent.agent.capability

interface AgentCapability {

    val name: String

    val description: String

    fun context(): String

    fun tools(): MyTool

    fun matcher(): AgentCapabilityMatcher
}
