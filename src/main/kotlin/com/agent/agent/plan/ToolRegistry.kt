package com.agent.agent.plan

import com.agent.agent.capability.AgentCapability
import com.agent.agent.capability.MyTool
import com.agent.core.utils.logger
import dev.langchain4j.agent.tool.Tool
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
class ToolRegistry {
    private val logger = logger()

    private val toolMethods = mutableMapOf<String, ToolEntry>()

    fun register(capabilities: Set<AgentCapability>) {
        toolMethods.clear()
        for (capability in capabilities) {
            val toolInstance = capability.tools()
            val methods = toolInstance::class.java.methods.filter { method ->
                method.isAnnotationPresent(Tool::class.java)
            }
            for (method in methods) {
                val annotation = method.getAnnotation(Tool::class.java)
                val toolName = annotation.name.ifBlank { method.name }
                toolMethods[toolName] = ToolEntry(
                    instance = toolInstance,
                    method = method,
                    description = annotation.name,
                    capabilityName = capability.name
                )
                logger.info("Tool注册: $toolName (${capability.name})")
            }
        }
    }

    fun invoke(toolName: String, arguments: Map<String, String>): String {
        val entry = toolMethods[toolName]
            ?: return "Outil '$toolName' non trouvé. Outils disponibles: ${toolMethods.keys}"

        return try {
            val params = entry.method.parameters
            val args = Array<Any?>(params.size) { null }

            for (i in params.indices) {
                val param = params[i]
                val annotation = param.getAnnotation(dev.langchain4j.agent.tool.P::class.java)
                val paramName = annotation?.value ?: param.getName()

                val value = arguments[paramName] ?: arguments.values.elementAtOrNull(i)
                if (value != null) {
                    args[i] = value
                }
            }

            val result = entry.method.invoke(entry.instance, *args)
            result?.toString() ?: "Exécution terminée (pas de résultat)"
        } catch (e: Exception) {
            logger.error("Erreur exécution tool $toolName: ${e.message}")
            "Erreur lors de l'exécution de $toolName: ${e.message}"
        }
    }

    fun getToolDescription(toolName: String): String? {
        val entry = toolMethods[toolName] ?: return null
        return "${entry.capabilityName}::$toolName: ${entry.description}"
    }

    fun getAllToolDescriptions(): String {
        return toolMethods.entries.joinToString("\n") { (name, entry) ->
            "- $name (${entry.capabilityName}): ${entry.description}"
        }
    }

    fun hasTool(toolName: String): Boolean = toolMethods.containsKey(toolName)
}

data class ToolEntry(
    val instance: MyTool,
    val method: Method,
    val description: String,
    val capabilityName: String
)
