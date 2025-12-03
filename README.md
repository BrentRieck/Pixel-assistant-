# Pixel Assistant (offline LiteRT)

An on-device, offline-first AI chatbot designed for the Google Pixel 10 Pro XL (Tensor G5, 16GB RAM) running Android 16+. The app mirrors the feel of Google's on-device AI gallery while focusing on persistent, privacy-preserving assistance using LiteRT models from [Hugging Face](https://huggingface.co/litert-community/models).

## Key capabilities
- **Model picker** for curated LiteRT builds (Gemma 2B, Phi-3 Mini, Llama 3 8B) with per-model context limits.
- **Offline inference** using downloaded LiteRT weights stored under app-private storage.
- **Persistent memory** with Android DataStore so the assistant remembers context between sessions (optional toggle).
- **Composable UI** optimized for single-column chat on Pixel 10 Pro XL, with quick clear and model switching.

## Architecture overview
- **UI layer**: Jetpack Compose (`MainActivity`, `PixelAssistantScreen`) renders chat history, model selector, memory toggle, and input row.
- **State & domain**: `ChatViewModel` orchestrates user intents, `ChatRepository` coordinates persistence and LiteRT inference, `MemoryStore` handles DataStore snapshots, and `ModelCatalog` surfaces recommended LiteRT builds.
- **Model runtime**: `LiteRtModelManager` prepares models (download + warm-up) and stubs inference. Replace `generateReply` with LiteRT session execution when integrating the runtime binary.

```
app/
├─ src/main/java/com/example/pixelassistant/
│  ├─ MainActivity.kt          # Compose UI + top bar + input
│  ├─ ChatViewModel.kt         # UI state, send/toggle/change model
│  ├─ ChatRepository.kt        # Memory persistence + LiteRT orchestration
│  ├─ LiteRtModelManager.kt    # Download/warmup + inference stub
│  ├─ ModelCatalog.kt          # Featured LiteRT models
│  ├─ ChatModels.kt            # Message + snapshot serialization
│  ├─ MemoryStore.kt           # DataStore-backed memory
│  └─ PixelAssistantApp.kt     # Application entry point
```

## Model recommendations
The curated list in `ModelCatalog` targets the Pixel 10 Pro XL balance of latency and quality:
- `litert-community/gemma-2b-it-int4` – Fast multilingual chats; ~1.6 GB.
- `litert-community/phi-3-mini-4k-instruct-int4` – Reasoning + tool-style prompts; ~2.4 GB.
- `litert-community/llama-3-8b-instruct-int4` – Highest quality; ~5.4 GB, still fits in 16 GB RAM with room for context.

> The download helper builds a Hugging Face `model.litert` URL. Swap in a signed local file or preloaded asset for fully offline distribution.

## Building & running
1. Open the project in Android Studio Ladybug (AGP 8.5+, Kotlin 1.9.23).
2. Sync Gradle and run on a Pixel 10 Pro XL (Android 16). Compose tooling previews are available via `ui-tooling`.
3. First run downloads the selected LiteRT model into `filesDir/models/<model-id>/model.litert`. Subsequent launches reuse the cached file and warm it up.

## Quick setup (works on device or emulator)
1. **Install prerequisites**
   - Android Studio Ladybug+ with Android SDK Platform 35 and Google Play system images.
   - JDK 17 (bundled with Studio is fine).
   - A Pixel 10 Pro XL running Android 16 (or an x86_64/arm64 emulator with >=6 GB RAM for smoke tests).
2. **Clone & open**
   ```bash
   git clone <this-repo>
   cd Pixel-assistant-
   ./gradlew tasks  # first sync + dependency download
   ```
   Then open the folder in Android Studio and let it finish Gradle sync.
3. **Prepare a target**
   - For hardware: enable Developer options + USB/Wi‑Fi debugging; verify with `adb devices`.
   - For emulator: create an Android 16 (API 35) image with at least 6 GB RAM and 1280x720+ resolution.
4. **Build & install**
   - From Studio: Select a device > Run.
   - From CLI: `./gradlew installDebug` (or `assembleDebug` + `adb install app/build/outputs/apk/debug/app-debug.apk`).
5. **First launch**
   - Choose a LiteRT model from the picker; the app downloads it to app-private storage and warms it up.
   - Toggle "Persistent memory" if you want chat context to survive relaunches (DataStore-backed).
   - Start chatting; switch models anytime. Subsequent sessions reuse the cached weights.

## Next integration steps
- Wire `LiteRtModelManager.generateReply` to the LiteRT Java/Kotlin API with a streaming text decoder.
- Add on-device evaluations (latency, token/s, memory) per model and surface them in the picker.
- Ship a local FAQ and red-team prompts so the assistant stays offline for safety review.
