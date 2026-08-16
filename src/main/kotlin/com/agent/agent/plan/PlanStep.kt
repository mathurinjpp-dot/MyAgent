package com.agent.agent.plan

data class PlanStep(
    val stepNumber: Int,
    val description: String,
    val toolName: String?,
    val arguments: Map<String, String> = emptyMap()
)
