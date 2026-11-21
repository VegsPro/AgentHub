package com.vegspro.agenthub.tools

import android.content.Context
import com.vegspro.agenthub.AgentHubApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object AgentTools {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun sandboxDir(ctx: Context): File {
        val dir = File(ctx.filesDir, "agent_files")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun execute(name: String, argumentsJson: String): String = withContext(Dispatchers.IO) {
        try {
            val args = JSONObject(argumentsJson.ifBlank { "{}" })
            when (name) {
                "file_write" -> fileWrite(args)
                "file_read" -> fileRead(args)
                "file_list" -> fileList(args)
                "file_delete" -> fileDelete(args)
                "http_get" -> httpGet(args)
                "http_post" -> httpPost(args)
                "calculator" -> calculator(args)
                else -> "Unknown tool: $name"
            }
        } catch (e: Exception) {
            "Tool error ($name): ${e.message}"
        }
    }

    private fun fileWrite(args: JSONObject): String {
        val path = args.optString("path", "note.txt")
        val content = args.optString("content", "")
        val ctx = AgentHubApp.instance
        val file = File(sandboxDir(ctx), path.trimStart('/'))
        file.parentFile?.mkdirs()
        file.writeText(content)
        return "File written successfully: ${file.absolutePath} (${content.length} chars)"
    }

    private fun fileRead(args: JSONObject): String {
        val path = args.optString("path", "")
        val ctx = AgentHubApp.instance
        val file = File(sandboxDir(ctx), path.trimStart('/'))
        return if (file.exists() && file.isFile) {
            val text = file.readText()
            if (text.length > 8000) text.take(8000) + "\n...[truncated]" else text
        } else {
            "File not found: $path"
        }
    }

    private fun fileList(args: JSONObject): String {
        val path = args.optString("path", "")
        val ctx = AgentHubApp.instance
        val dir = if (path.isBlank()) sandboxDir(ctx) else File(sandboxDir(ctx), path.trimStart('/'))
        if (!dir.exists() || !dir.isDirectory) return "Directory not found: $path"
        val files = dir.listFiles()?.sortedBy { it.name } ?: emptyList()
        if (files.isEmpty()) return "Directory is empty"
        return files.joinToString("\n") {
            val type = if (it.isDirectory) "[DIR]" else "[FILE]"
            "$type ${it.name} (${it.length()} bytes)"
        }
    }

    private fun fileDelete(args: JSONObject): String {
        val path = args.optString("path", "")
        val ctx = AgentHubApp.instance
        val file = File(sandboxDir(ctx), path.trimStart('/'))
        return if (file.exists()) {
            val ok = file.delete()
            if (ok) "Deleted: $path" else "Failed to delete: $path"
        } else "File not found: $path"
    }

    private fun httpGet(args: JSONObject): String {
        val url = args.optString("url", "")
        if (url.isBlank()) return "url is required"
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            val truncated = if (body.length > 6000) body.take(6000) + "\n...[truncated]" else body
            return "HTTP ${response.code}\n$truncated"
        }
    }

    private fun httpPost(args: JSONObject): String {
        val url = args.optString("url", "")
        val bodyStr = args.optString("body", "{}")
        val contentType = args.optString("content_type", "application/json")
        if (url.isBlank()) return "url is required"
        val mediaType = contentType.toMediaType()
        val body = bodyStr.toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(body).build()
        client.newCall(request).execute().use { response ->
            val respBody = response.body?.string() ?: ""
            val truncated = if (respBody.length > 6000) respBody.take(6000) + "\n...[truncated]" else respBody
            return "HTTP ${response.code}\n$truncated"
        }
    }

    private fun calculator(args: JSONObject): String {
        val expression = args.optString("expression", "")
        if (expression.isBlank()) return "expression is required"
        return try {
            // Simple safe eval for basic arithmetic
            val result = evaluateSimple(expression)
            "Result: $result"
        } catch (e: Exception) {
            "Calculation error: ${e.message}"
        }
    }

    private fun evaluateSimple(expr: String): Double {
        val cleaned = expr.replace(" ", "")
        // Very basic: support + - * / and numbers
        // For production use a proper expression parser
        return when {
            "+" in cleaned -> {
                val parts = cleaned.split("+")
                parts.sumOf { it.toDoubleOrNull() ?: 0.0 }
            }
            "-" in cleaned && cleaned.indexOf("-") > 0 -> {
                val parts = cleaned.split("-")
                parts.map { it.toDoubleOrNull() ?: 0.0 }.reduce { a, b -> a - b }
            }
            "*" in cleaned -> {
                val parts = cleaned.split("*")
                parts.map { it.toDoubleOrNull() ?: 1.0 }.reduce { a, b -> a * b }
            }
            "/" in cleaned -> {
                val parts = cleaned.split("/")
                parts.map { it.toDoubleOrNull() ?: 1.0 }.reduce { a, b -> a / b }
            }
            else -> cleaned.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid expression")
        }
    }

    fun toolDefinitions(): List<Map<String, Any>> = listOf(
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "file_write",
                "description" to "Write text content to a file in the agent sandbox",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf("type" to "string", "description" to "Relative file path"),
                        "content" to mapOf("type" to "string", "description" to "Text content to write")
                    ),
                    "required" to listOf("path", "content")
                )
            )
        ),
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "file_read",
                "description" to "Read content of a file from the agent sandbox",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf("type" to "string", "description" to "Relative file path")
                    ),
                    "required" to listOf("path")
                )
            )
        ),
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "file_list",
                "description" to "List files and directories in the agent sandbox",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf("type" to "string", "description" to "Relative directory path (empty for root)")
                    )
                )
            )
        ),
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "file_delete",
                "description" to "Delete a file from the agent sandbox",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf("type" to "string", "description" to "Relative file path")
                    ),
                    "required" to listOf("path")
                )
            )
        ),
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "http_get",
                "description" to "Perform an HTTP GET request and return the response body",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "url" to mapOf("type" to "string", "description" to "Full URL")
                    ),
                    "required" to listOf("url")
                )
            )
        ),
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "http_post",
                "description" to "Perform an HTTP POST request",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "url" to mapOf("type" to "string", "description" to "Full URL"),
                        "body" to mapOf("type" to "string", "description" to "Request body (usually JSON)"),
                        "content_type" to mapOf("type" to "string", "description" to "Content-Type header")
                    ),
                    "required" to listOf("url")
                )
            )
        ),
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "calculator",
                "description" to "Evaluate a simple arithmetic expression",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "expression" to mapOf("type" to "string", "description" to "Math expression e.g. 12*34+5")
                    ),
                    "required" to listOf("expression")
                )
            )
        )
    )
}
