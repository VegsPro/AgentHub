package com.vegspro.agenthub.network

import com.vegspro.agenthub.data.ChatMessage
import com.vegspro.agenthub.data.ToolCall
import com.vegspro.agenthub.tools.AgentTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenAIClient(
    private var baseUrl: String,
    private var apiKey: String
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun updateCredentials(baseUrl: String, apiKey: String) {
        this.baseUrl = baseUrl.trimEnd('/')
        this.apiKey = apiKey
    }

    suspend fun chat(
        messages: List<ChatMessage>,
        model: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 2048,
        enableTools: Boolean = true,
        onPartial: ((String) -> Unit)? = null
    ): ChatMessage = withContext(Dispatchers.IO) {
        val url = if (baseUrl.endsWith("/v1")) "$baseUrl/chat/completions" else "$baseUrl/v1/chat/completions"
        
        val body = JSONObject().apply {
            put("model", model)
            put("temperature", temperature.toDouble())
            put("max_tokens", maxTokens)
            put("stream", false)
            put("messages", JSONArray().apply {
                messages.forEach { msg ->
                    put(JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                        if (msg.toolCalls != null) {
                            put("tool_calls", JSONArray().apply {
                                msg.toolCalls.forEach { tc ->
                                    put(JSONObject().apply {
                                        put("id", tc.id)
                                        put("type", "function")
                                        put("function", JSONObject().apply {
                                            put("name", tc.name)
                                            put("arguments", tc.arguments)
                                        })
                                    })
                                }
                            })
                        }
                        if (msg.toolCallId != null) {
                            put("tool_call_id", msg.toolCallId)
                        }
                    })
                }
            })
            if (enableTools) {
                put("tools", JSONArray().apply {
                    AgentTools.toolDefinitions().forEach { def ->
                        put(JSONObject(def as Map<*, *>))
                    }
                })
            }
        }

        val requestBuilder = Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
        
        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }

        val response = client.newCall(requestBuilder.build()).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        
        if (!response.isSuccessful) {
            throw Exception("API error ${response.code}: $responseBody")
        }

        val json = JSONObject(responseBody)
        val choice = json.getJSONArray("choices").getJSONObject(0)
        val message = choice.getJSONObject("message")
        val content = message.optString("content", "")
        
        val toolCalls = mutableListOf<ToolCall>()
        if (message.has("tool_calls")) {
            val tcs = message.getJSONArray("tool_calls")
            for (i in 0 until tcs.length()) {
                val tc = tcs.getJSONObject(i)
                val fn = tc.getJSONObject("function")
                toolCalls.add(
                    ToolCall(
                        id = tc.getString("id"),
                        name = fn.getString("name"),
                        arguments = fn.optString("arguments", "{}")
                    )
                )
            }
        }

        ChatMessage(
            role = "assistant",
            content = content,
            toolCalls = if (toolCalls.isEmpty()) null else toolCalls
        )
    }

    /**
     * Full agent loop: call model → execute tools → feed results → repeat until done
     */
    suspend fun agentLoop(
        initialMessages: MutableList<ChatMessage>,
        model: String,
        temperature: Float = 0.7f,
        maxTokens: Int = 2048,
        maxIterations: Int = 8,
        onUpdate: (List<ChatMessage>) -> Unit
    ): List<ChatMessage> {
        var messages = initialMessages.toMutableList()
        var iterations = 0

        while (iterations < maxIterations) {
            iterations++
            val response = chat(messages, model, temperature, maxTokens, enableTools = true)
            messages.add(response)
            onUpdate(messages.toList())

            val toolCalls = response.toolCalls
            if (toolCalls.isNullOrEmpty()) {
                break // done
            }

            // Execute tools
            for (tc in toolCalls) {
                val result = AgentTools.execute(tc.name, tc.arguments)
                val toolMsg = ChatMessage(
                    role = "tool",
                    content = result,
                    toolCallId = tc.id
                )
                messages.add(toolMsg)
                onUpdate(messages.toList())
            }
        }
        return messages
    }
}
