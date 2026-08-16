package com.agent.agent.plan

data class StepResult(
    val step: PlanStep,
    val output: String,
    val success: Boolean
)
