package com.vegspro.agenthub.data

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String,
    val content: String,
    val toolCalls: List<ToolCall>? = null,
    val toolCallId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ToolCall(
    val id: String,
    val name: String,
    val arguments: String
)

@Serializable
data class Agent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val systemPrompt: String,
    val enabledTools: List<String> = listOf("file_write", "file_read", "file_list", "http_get", "http_post", "calculator"),
    val model: String? = null
)

@Serializable
data class ModelInfo(
    val id: String,
    val name: String,
    val description: String = "",
    val size: String = "",
    val quant: String = "",
    val downloadUrl: String? = null,
    val hfRepo: String? = null,
    val filename: String? = null,
    val isLocal: Boolean = false,
    val path: String? = null
)

@Serializable
data class AppSettings(
    val apiBaseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val defaultModel: String = "gpt-4o-mini",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val systemPrompt: String = "You are a helpful AI assistant running on the user's Android phone. You have tools to work with files and make HTTP requests. Be concise and useful.",
    val enableTools: Boolean = true,
    val appliedLocalModelPath: String? = null
)

val PRESET_MODELS = listOf(
    ModelInfo(
        id = "qwen2.5-0.5b",
        name = "Qwen2.5 0.5B Instruct",
        description = "Tiny fast model, good for phones with 4–6 GB RAM",
        size = "~398 MB",
        quant = "Q4_K_M",
        hfRepo = "Qwen/Qwen2.5-0.5B-Instruct-GGUF",
        filename = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf"
    ),
    ModelInfo(
        id = "qwen2.5-1.5b",
        name = "Qwen2.5 1.5B Instruct",
        description = "Balanced small model",
        size = "~986 MB",
        quant = "Q4_K_M",
        hfRepo = "Qwen/Qwen2.5-1.5B-Instruct-GGUF",
        filename = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf"
    ),
    ModelInfo(
        id = "smollm2-1.7b",
        name = "SmolLM2 1.7B Instruct",
        description = "Excellent small model from HuggingFace",
        size = "~1.05 GB",
        quant = "Q4_K_M",
        hfRepo = "HuggingFaceTB/SmolLM2-1.7B-Instruct-GGUF",
        filename = "smollm2-1.7b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/HuggingFaceTB/SmolLM2-1.7B-Instruct-GGUF/resolve/main/smollm2-1.7b-instruct-q4_k_m.gguf"
    ),
    ModelInfo(
        id = "gemma-2-2b",
        name = "Gemma 2 2B IT",
        description = "Google Gemma 2, good quality",
        size = "~1.6 GB",
        quant = "Q4_K_M",
        hfRepo = "bartowski/gemma-2-2b-it-GGUF",
        filename = "gemma-2-2b-it-Q4_K_M.gguf",
        downloadUrl = "https://huggingface.co/bartowski/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf"
    ),
    ModelInfo(
        id = "phi-3.5-mini",
        name = "Phi-3.5 Mini Instruct",
        description = "Microsoft Phi-3.5, strong reasoning for size",
        size = "~2.2 GB",
        quant = "Q4_K_M",
        hfRepo = "bartowski/Phi-3.5-mini-instruct-GGUF",
        filename = "Phi-3.5-mini-instruct-Q4_K_M.gguf",
        downloadUrl = "https://huggingface.co/bartowski/Phi-3.5-mini-instruct-GGUF/resolve/main/Phi-3.5-mini-instruct-Q4_K_M.gguf"
    ),
    ModelInfo(
        id = "qwen2.5-3b",
        name = "Qwen2.5 3B Instruct",
        description = "Strong 3B model, needs ~6+ GB free RAM",
        size = "~1.9 GB",
        quant = "Q4_K_M",
        hfRepo = "Qwen/Qwen2.5-3B-Instruct-GGUF",
        filename = "qwen2.5-3b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf"
    ),
    ModelInfo(
        id = "qwen3-4b",
        name = "Qwen3 4B Instruct",
        description = "Newer Qwen3 4B — good quality, needs 8+ GB RAM",
        size = "~2.5 GB",
        quant = "Q4_K_M",
        hfRepo = "Qwen/Qwen3-4B-GGUF",
        filename = "Qwen3-4B-Q4_K_M.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen3-4B-GGUF/resolve/main/Qwen3-4B-Q4_K_M.gguf"
    ),
    ModelInfo(
        id = "llama-3.2-3b",
        name = "Llama 3.2 3B Instruct",
        description = "Meta Llama 3.2 3B, solid all-rounder",
        size = "~2.0 GB",
        quant = "Q4_K_M",
        hfRepo = "bartowski/Llama-3.2-3B-Instruct-GGUF",
        filename = "Llama-3.2-3B-Instruct-Q4_K_M.gguf",
        downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf"
    ),
    ModelInfo(
        id = "qwen2.5-7b",
        name = "Qwen2.5 7B Instruct",
        description = "Larger model — only for high-end phones (12+ GB RAM)",
        size = "~4.4 GB",
        quant = "Q4_K_M",
        hfRepo = "Qwen/Qwen2.5-7B-Instruct-GGUF",
        filename = "qwen2.5-7b-instruct-q4_k_m.gguf",
        downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-7B-Instruct-GGUF/resolve/main/qwen2.5-7b-instruct-q4_k_m.gguf"
    )
)

val PRESET_AGENTS = listOf(
    Agent(
        name = "File Helper",
        description = "Specialist in creating and managing files on the device",
        systemPrompt = "You are a file management agent on an Android phone. You can create, read, list and write files. Always confirm destructive actions. Prefer working inside the app sandbox."
    ),
    Agent(
        name = "Web Researcher",
        description = "Can make HTTP requests and fetch data",
        systemPrompt = "You are a research agent. You can perform GET and POST requests to gather information. Summarize results clearly. Do not make requests to malicious sites."
    ),
    Agent(
        name = "General Assistant",
        description = "All-purpose agent with all tools enabled",
        systemPrompt = "You are a powerful AI assistant running locally on the user's phone. You have tools for files and HTTP. Use tools when they help solve the user's request. Be helpful, concise and accurate."
    )
)
