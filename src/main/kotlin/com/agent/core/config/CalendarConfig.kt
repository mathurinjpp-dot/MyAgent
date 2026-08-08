package com.agent.core.config

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.time.ZoneId

@Configuration
class CalendarConfig {

    private val applicationName = "Personal AI Agent"

    private val zone = ZoneId.of("Europe/Paris")

    private val calendarId = "primary"

    private val jsonFactory: JsonFactory =
        GsonFactory.getDefaultInstance()

    private val tokensDirectoryPath = "tokens"

    private val scopes = listOf(
        CalendarScopes.CALENDAR
    )

    private val credentialsFilePath =
        "/credentials/googleAuthentification.json"

    @Bean
    fun calendar(): Calendar {

        val httpTransport =
            GoogleNetHttpTransport.newTrustedTransport()

        val credential =
            getCredentials(httpTransport)

        return Calendar.Builder(
            httpTransport,
            jsonFactory,
            credential
        )
            .setApplicationName(applicationName)
            .build()
    }

    private fun getCredentials(
        httpTransport: NetHttpTransport
    ): Credential {

        val inputStream =
            CalendarConfig::class.java
                .getResourceAsStream(credentialsFilePath)
                ?: throw FileNotFoundException(
                    "Resource not found: $credentialsFilePath"
                )

        val clientSecrets =
            GoogleClientSecrets.load(
                jsonFactory,
                InputStreamReader(inputStream)
            )

        val flow =
            GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                clientSecrets,
                scopes
            )
                .setDataStoreFactory(
                    FileDataStoreFactory(
                        File(tokensDirectoryPath)
                    )
                )
                .setAccessType("offline")
                .build()

        val receiver =
            LocalServerReceiver.Builder()
                .setPort(8888)
                .build()

        return AuthorizationCodeInstalledApp(
            flow,
            receiver
        ).authorize("user")
    }
}