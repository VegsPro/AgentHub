package com.vegspro.agenthub.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val apiBaseUrlKey = stringPreferencesKey("api_base_url")
    private val apiKeyKey = stringPreferencesKey("api_key")
    private val defaultModelKey = stringPreferencesKey("default_model")
    private val temperatureKey = floatPreferencesKey("temperature")
    private val maxTokensKey = intPreferencesKey("max_tokens")
    private val systemPromptKey = stringPreferencesKey("system_prompt")
    private val enableToolsKey = booleanPreferencesKey("enable_tools")

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            apiBaseUrl = prefs[apiBaseUrlKey] ?: "https://api.openai.com/v1",
            apiKey = prefs[apiKeyKey] ?: "",
            defaultModel = prefs[defaultModelKey] ?: "gpt-4o-mini",
            temperature = prefs[temperatureKey] ?: 0.7f,
            maxTokens = prefs[maxTokensKey] ?: 2048,
            systemPrompt = prefs[systemPromptKey] ?: "You are a helpful AI assistant running on the user's Android phone. You have tools to work with files and make HTTP requests. Be concise and useful.",
            enableTools = prefs[enableToolsKey] ?: true
        )
    }

    suspend fun update(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[apiBaseUrlKey] = settings.apiBaseUrl
            prefs[apiKeyKey] = settings.apiKey
            prefs[defaultModelKey] = settings.defaultModel
            prefs[temperatureKey] = settings.temperature
            prefs[maxTokensKey] = settings.maxTokens
            prefs[systemPromptKey] = settings.systemPrompt
            prefs[enableToolsKey] = settings.enableTools
        }
    }
}
