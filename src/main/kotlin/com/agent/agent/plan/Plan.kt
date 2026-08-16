package com.agent.agent.plan

data class Plan(
    val steps: List<PlanStep>,
    val reasoning: String
)
