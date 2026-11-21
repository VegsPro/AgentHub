# AgentHub

**Native Android AI Agent Hub** — better agent capabilities than Maid.

Built with **Jetpack Compose + Material 3**.

## Features

- **Chat** with any OpenAI-compatible endpoint (OpenAI, OpenRouter, local `llama-server`, Ollama, LM Studio, etc.)
- **Agent tools** (the main advantage over Maid):
  - `file_write` / `file_read` / `file_list` / `file_delete` (sandbox)
  - `http_get` / `http_post`
  - `calculator`
- Automatic **tool-calling loop**
- Model catalog with recommended small GGUF models
- Preset agents
- Settings for base URL, API key, model, temperature, system prompt
- Dark Material 3 theme

## Local models (recommended)

1. Install **Termux**
2. `pkg install llama-cpp`
3. Download a GGUF (e.g. Qwen2.5-0.5B or 1.5B)
4. `llama-server -m your-model.gguf --host 127.0.0.1 --port 8080`
5. In AgentHub → Settings → Base URL = `http://127.0.0.1:8080/v1`

## Build

```bash
./gradlew assembleRelease
```

APK: `app/build/outputs/apk/release/app-release.apk`

## Signing

- SHA-1: `B8:58:F5:64:DC:FF:A3:A3:0B:32:9E:AA:9C:84:0F:D1:5F:4F:35:4A`
- SHA-256: `00:10:45:A3:7C:3A:F4:00:F1:DB:BC:2C:37:3D:F4:9C:18:1E:9F:08:39:10:AD:24:E3:D0:DD:8D:C3:4D:B3:71`

## License

MIT
