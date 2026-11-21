package com.vegspro.agenthub.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vegspro.agenthub.data.ModelInfo
import com.vegspro.agenthub.data.PRESET_MODELS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val modelsDir = remember { File(context.filesDir, "models").also { if (!it.exists()) it.mkdirs() } }

    var downloadProgress by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
    var installed by remember { mutableStateOf(scanInstalled(modelsDir)) }
    var appliedId by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun refreshInstalled() {
        installed = scanInstalled(modelsDir)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Model Catalog") })

        if (statusMessage != null) {
            Text(
                text = statusMessage!!,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "Скачай GGUF прямо в приложение. После скачивания нажми «Применить». Для инференса укажи в Settings Base URL локального llama-server / Ollama.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(PRESET_MODELS, key = { it.id }) { model ->
                val isInstalled = installed.contains(model.id) ||
                    (model.filename != null && File(modelsDir, model.filename).exists())
                val progress = downloadProgress[model.id]
                val isDownloading = progress != null && progress in 0f..0.999f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(model.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(model.description, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = { Text(model.size) })
                            AssistChip(onClick = {}, label = { Text(model.quant) })
                            if (isInstalled) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Installed") },
                                    leadingIcon = { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                )
                            }
                            if (appliedId == model.id) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Applied") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }

                        if (isDownloading) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress ?: 0f },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                "${((progress ?: 0f) * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!isInstalled && !isDownloading && model.downloadUrl != null) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            downloadModel(
                                                model = model,
                                                modelsDir = modelsDir,
                                                onProgress = { p ->
                                                    downloadProgress = downloadProgress + (model.id to p)
                                                },
                                                onDone = { success, msg ->
                                                    downloadProgress = downloadProgress - model.id
                                                    if (success) {
                                                        refreshInstalled()
                                                        statusMessage = "Скачано: ${model.name}"
                                                    } else {
                                                        statusMessage = "Ошибка: $msg"
                                                    }
                                                }
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CloudDownload, null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Скачать")
                                }
                            }

                            if (isInstalled) {
                                Button(
                                    onClick = {
                                        val file = File(modelsDir, model.filename ?: "${model.id}.gguf")
                                        appliedId = model.id
                                        context.getSharedPreferences("agenthub", Context.MODE_PRIVATE)
                                            .edit()
                                            .putString("applied_model_path", file.absolutePath)
                                            .putString("applied_model_id", model.id)
                                            .apply()
                                        statusMessage = "Применено: ${file.absolutePath}"
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Применить")
                                }

                                OutlinedButton(
                                    onClick = {
                                        val file = File(modelsDir, model.filename ?: "${model.id}.gguf")
                                        if (file.exists()) file.delete()
                                        refreshInstalled()
                                        if (appliedId == model.id) appliedId = null
                                        statusMessage = "Удалено: ${model.name}"
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, null)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Как использовать локально", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "1. Скачай модель кнопкой «Скачать»\n" +
                            "2. Нажми «Применить»\n" +
                            "3. В Termux: llama-server -m <путь> --host 127.0.0.1 --port 8080\n" +
                            "4. Settings → Base URL = http://127.0.0.1:8080/v1",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

private fun scanInstalled(dir: File): Set<String> {
    val files = dir.listFiles()?.map { it.name.lowercase() }?.toSet() ?: emptySet()
    return PRESET_MODELS
        .filter { m -> m.filename != null && files.contains(m.filename.lowercase()) }
        .map { it.id }
        .toSet()
}

private suspend fun downloadModel(
    model: ModelInfo,
    modelsDir: File,
    onProgress: (Float) -> Unit,
    onDone: (Boolean, String?) -> Unit
) {
    val url = model.downloadUrl ?: return onDone(false, "No URL")
    val filename = model.filename ?: "${model.id}.gguf"
    val target = File(modelsDir, filename)
    val tmp = File(modelsDir, "$filename.part")

    withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) { onDone(false, "HTTP ${response.code}") }
                    return@withContext
                }
                val body = response.body ?: run {
                    withContext(Dispatchers.Main) { onDone(false, "Empty body") }
                    return@withContext
                }
                val total = body.contentLength().coerceAtLeast(1L)
                body.byteStream().use { input ->
                    FileOutputStream(tmp).use { output ->
                        val buffer = ByteArray(8192)
                        var downloaded = 0L
                        var lastEmit = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                            downloaded += read
                            if (downloaded - lastEmit > 256 * 1024 || downloaded == total) {
                                lastEmit = downloaded
                                val p = (downloaded.toFloat() / total).coerceIn(0f, 1f)
                                withContext(Dispatchers.Main) { onProgress(p) }
                            }
                        }
                    }
                }
                tmp.renameTo(target)
                withContext(Dispatchers.Main) {
                    onProgress(1f)
                    onDone(true, null)
                }
            }
        } catch (e: Exception) {
            tmp.delete()
            withContext(Dispatchers.Main) { onDone(false, e.message) }
        }
    }
}
