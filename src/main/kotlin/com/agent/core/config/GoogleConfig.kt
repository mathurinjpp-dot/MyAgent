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
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.gmail.GmailScopes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader

@Configuration
class GoogleConfig {

    private val jsonFactory: JsonFactory =
        GsonFactory.getDefaultInstance()

    private val tokensDirectoryPath = "tokens"

    private val credentialsFilePath =
        "/credentials/googleAuthentification.json"

    private val scopes = listOf(
        CalendarScopes.CALENDAR,
        GmailScopes.GMAIL_READONLY,
        GmailScopes.GMAIL_SEND,
        GmailScopes.GMAIL_MODIFY
    )

    @Bean
    fun googleHttpTransport(): NetHttpTransport =
        GoogleNetHttpTransport.newTrustedTransport()

    @Bean
    fun googleJsonFactory(): JsonFactory =
        jsonFactory

    @Bean
    fun googleCredential(
        httpTransport: NetHttpTransport
    ): Credential {

        val inputStream =
            GoogleConfig::class.java
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