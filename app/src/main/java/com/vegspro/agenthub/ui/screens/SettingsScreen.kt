package com.vegspro.agenthub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vegspro.agenthub.data.AppSettings
import com.vegspro.agenthub.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ChatViewModel) {
    val current by viewModel.settings.collectAsState()
    var baseUrl by remember(current) { mutableStateOf(current.apiBaseUrl) }
    var apiKey by remember(current) { mutableStateOf(current.apiKey) }
    var model by remember(current) { mutableStateOf(current.defaultModel) }
    var temperature by remember(current) { mutableFloatStateOf(current.temperature) }
    var maxTokens by remember(current) { mutableIntStateOf(current.maxTokens) }
    var systemPrompt by remember(current) { mutableStateOf(current.systemPrompt) }
    var enableTools by remember(current) { mutableStateOf(current.enableTools) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Settings") })

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("API Base URL") },
                supportingText = { Text("OpenAI / OpenRouter / local llama-server / Ollama") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key (optional for local)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model name") },
                supportingText = { Text("e.g. gpt-4o-mini, qwen2.5, llama3.2") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Temperature: ${"%.1f".format(temperature)}")
            Slider(
                value = temperature,
                onValueChange = { temperature = it },
                valueRange = 0f..1.5f,
                steps = 14
            )

            OutlinedTextField(
                value = maxTokens.toString(),
                onValueChange = { maxTokens = it.toIntOrNull() ?: 2048 },
                label = { Text("Max tokens") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = systemPrompt,
                onValueChange = { systemPrompt = it },
                label = { Text("System prompt") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                maxLines = 6
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enable agent tools", modifier = Modifier.weight(1f))
                Switch(
                    checked = enableTools,
                    onCheckedChange = { enableTools = it }
                )
            }

            Button(
                onClick = {
                    viewModel.updateSettings(
                        AppSettings(
                            apiBaseUrl = baseUrl.trim(),
                            apiKey = apiKey.trim(),
                            defaultModel = model.trim(),
                            temperature = temperature,
                            maxTokens = maxTokens,
                            systemPrompt = systemPrompt,
                            enableTools = enableTools
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save settings")
            }

            HorizontalDivider()

            Text("Quick presets", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {
                        baseUrl = "https://api.openai.com/v1"
                        model = "gpt-4o-mini"
                    },
                    label = { Text("OpenAI") }
                )
                AssistChip(
                    onClick = {
                        baseUrl = "https://openrouter.ai/api/v1"
                        model = "openai/gpt-4o-mini"
                    },
                    label = { Text("OpenRouter") }
                )
                AssistChip(
                    onClick = {
                        baseUrl = "http://127.0.0.1:8080/v1"
                        apiKey = ""
                        model = "local"
                    },
                    label = { Text("Local 8080") }
                )
            }
            AssistChip(
                onClick = {
                    baseUrl = "http://127.0.0.1:11434/v1"
                    apiKey = ""
                    model = "llama3.2"
                },
                label = { Text("Ollama (11434)") }
            )
        }
    }
}
