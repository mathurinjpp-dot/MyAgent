package com.agent.core.config

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.services.gmail.Gmail
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GmailConfig {

    private val applicationName = "Personal AI Agent"

    @Bean
    fun gmail(
        httpTransport: NetHttpTransport,
        jsonFactory: JsonFactory,
        credential: Credential
    ): Gmail {

        return Gmail.Builder(
            httpTransport,
            jsonFactory,
            credential
        )
            .setApplicationName(applicationName)
            .build()
    }
}