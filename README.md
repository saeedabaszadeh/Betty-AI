# Betty AI — Voice Assistant (Core Engine)

Betty is a Persian-first, voice-driven AI assistant for Android. She is activated by the
wake phrase **«سلام بتی»** (Hello Betty) and is designed from the ground up as a small
*personal voice-AI operating system* rather than a single chat screen. This repository
contains the **stable core engine**: the orchestration, voice pipeline, AI layer, command
router, skill framework, memory, settings, permissions, notifications, local database and
UI shell — all built so that new capabilities can be added as **skills/modules without
rewriting the core**.

The project is a real, buildable, installable Android application (package
`com.bettyai.assistant`) written in Kotlin with Jetpack Compose, following Clean
Architecture + MVVM + a modular, dependency-injected design.

---

## Table of contents

1. [What is implemented](#what-is-implemented)
2. [Architecture](#architecture)
3. [Folder structure](#folder-structure)
4. [The state machine](#the-state-machine)
5. [Requirements](#requirements)
6. [Building & running](#building--running)
7. [Configuration & secrets](#configuration--secrets)
8. [How to add a new Skill (complete example)](#how-to-add-a-new-skill-complete-example)
9. [How to add a new AI / Speech / TTS / Automation / Agent provider](#how-to-add-a-new-provider)
10. [Database structure](#database-structure)
11. [Wake word & Android limitations (honest notes)](#wake-word--android-limitations)
12. [Security](#security)
13. [Testing](#testing)
14. [Roadmap](#roadmap)

---

## What is implemented

The core engine is complete and functional end-to-end:

- **Voice pipeline** — push-to-talk capture via Android `SpeechRecognizer` (STT) and speech
  output via Android `TextToSpeech` (TTS), both behind provider interfaces.
- **BettyCore orchestrator** — owns the state machine and wires together the voice engine,
  wake-word engine, AI engine, TTS manager, command router, skill manager, memory manager,
  settings manager, permission manager, notification manager and event bus.
- **AI layer** — an OpenAI-compatible HTTP provider (works with OpenAI, Azure-compatible
  gateways, OpenRouter, local servers, etc.). The API key is never hard-coded and is stored
  encrypted. Betty degrades gracefully when offline or unconfigured.
- **Command router** — normalizes the utterance (including stripping the wake phrase) and
  routes it to a matching skill or to the AI engine, using keyword/regex scoring.
- **Skill framework** — a `Skill` contract (id, name, description, permissions, intents,
  `execute`, response) plus a `SkillManager` that registers, enables/disables and executes
  skills. Five built-in skills ship with the core: time/date, greeting, help, about and
  "remember".
- **Memory manager** — long-term memory facade over a repository (DB layer kept separate).
- **Settings** — enable/disable Betty, voice on/off, language, TTS voice, AI provider,
  base URL, model, API key, wake word toggle and display name, all reactive via DataStore.
- **Permissions** — a permission manager that requests permissions only when needed and a
  status screen showing every permission's state.
- **Local database** — Room with `UserProfile`, `Conversation`, `Message`, `AppSettings`,
  `Skill` and `Memory` tables, exposed through repositories.
- **Notifications & foreground service** — a transparent, always-visible foreground
  notification (within official Android limits) so Betty can stay warm.
- **UI** — a home screen with an animated logo, a live status indicator, a large central
  push-to-talk button and recent conversations; a conversation screen; a settings screen;
  and a permissions screen. The user's name is read from the real profile and is never
  hard-coded.
- **Tests** — 54 JVM unit tests (all passing) plus Room integration and Compose UI
  instrumentation tests.

What is intentionally **not** implemented yet (kept as ready extension points): future
skills such as calling, WhatsApp, reminders or trading; always-on background wake-word
detection (see the honest note below); and cloud sync.

---

## Architecture

Betty follows **Clean Architecture + MVVM**, split into a **modular** set of layers, glued
together with **Hilt** dependency injection. The guiding rule is that the UI contains no
business logic: screens observe immutable state from ViewModels, and ViewModels delegate to
the core engine and repositories.

```
┌──────────────────────────────────────────────────────────────┐
│                         Presentation                          │
│  Compose screens + ViewModels (Home, Conversation, Settings,  │
│  Permissions) + Navigation + Theme/Design system              │
└───────────────▲───────────────────────────────┬──────────────┘
                │ StateFlow (immutable UI state) │ user intents
┌───────────────┴───────────────────────────────▼──────────────┐
│                          BettyCore                            │
│  Orchestrator + State machine + EventBus                      │
│  VoiceEngine · WakeWordEngine · AIEngine · TtsManager         │
│  CommandRouter · SkillManager · MemoryManager                 │
│  SettingsManager · PermissionManager · NotificationManager    │
└───────────────▲───────────────────────────────┬──────────────┘
                │ domain interfaces             │ implementations
┌───────────────┴───────────────────────────────▼──────────────┐
│                    Domain  ──►  Data                          │
│  Models · Repository interfaces · Provider interfaces         │
│  Room database · DataStore · EncryptedSharedPreferences       │
│  HTTP client · NetworkMonitor · Repository implementations    │
└──────────────────────────────────────────────────────────────┘
```

**Layers**

- **Presentation** (`ui/`) — Jetpack Compose screens, ViewModels, navigation and the design
  system. Stateless composables render `UiState`; ViewModels expose `StateFlow`.
- **Core** (`core/`) — `BettyCore`, the state machine, the event bus, the typed result/error
  hierarchy and the Hilt modules that wire everything.
- **Domain** (`domain/`) — pure Kotlin models, repository interfaces and provider
  interfaces. No Android dependencies, so it is trivially unit-testable.
- **Data** (`data/`) — Room entities/DAOs, repository implementations, DataStore settings,
  the encrypted secret store, the HTTP client and the connectivity monitor.
- **Engines & services** (`voice/`, `ai/`, `router/`, `skills/`, `memory/`, `settings/`,
  `permissions/`, `notifications/`, `service/`) — the concrete subsystems the core composes.

**Dependency injection & extensibility.** Providers (AI, speech, TTS, automation, agent)
are injected as `Set<Provider>` and skills as `Set<Skill>`. Adding a new implementation is a
single `@Binds @IntoSet` line in a Hilt module; the engines that consume the sets never
change. This is the mechanism that makes Betty modular and future-proof.

---

## Folder structure

```
betty-ai/
├── app/
│   ├── build.gradle.kts                 # App module build config (SDK, signing, deps)
│   ├── proguard-rules.pro               # R8/ProGuard keep rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml       # Permissions, activity, foreground service
│       │   ├── java/com/bettyai/assistant/
│       │   │   ├── BettyApplication.kt    # @HiltAndroidApp entry point
│       │   │   ├── MainActivity.kt        # Single-activity Compose host
│       │   │   ├── ai/                    # AI engine, OpenAI-compatible provider,
│       │   │   │                          #   prompt builder, DTOs, n8n/Flowise providers
│       │   │   ├── core/                  # BettyCore, state, events, logger, results, DI
│       │   │   ├── data/                  # Room DB + DAOs + entities, mappers, repos,
│       │   │   │                          #   network, secure store, settings
│       │   │   ├── domain/                # Models, repository & provider interfaces
│       │   │   ├── memory/                # MemoryManager
│       │   │   ├── notifications/         # Notification manager
│       │   │   ├── permissions/           # Permission manager + model
│       │   │   ├── router/                # CommandRouter + TextNormalizer
│       │   │   ├── service/               # Foreground service
│       │   │   ├── settings/              # SettingsManager
│       │   │   ├── skills/                # Skill contract, manager, context, builtin/
│       │   │   ├── ui/                    # Compose screens, ViewModels, nav, theme, components
│       │   │   ├── util/                  # Persian text helpers (digits, Jalali date)
│       │   │   └── voice/                 # VoiceEngine, TtsManager, speech & wake-word
│       │   └── res/                       # Strings, themes, icons, backup rules
│       ├── test/                          # JVM unit tests
│       └── androidTest/                   # Instrumentation (Room + Compose UI) tests
├── docs/                                  # Architecture & folder-structure docs
├── gradle/libs.versions.toml              # Version catalog (single source of dependency truth)
├── config.example.properties              # Example non-secret configuration
├── keystore.properties.example            # Example signing config (real file is git-ignored)
├── build.gradle.kts                       # Root build config
├── settings.gradle.kts                    # Module includes
└── gradle.properties                      # JVM/build flags
```

---

## The state machine

Betty's lifecycle is a small, explicit state machine surfaced in the UI:

| State | Persian label | Meaning |
|-------|---------------|---------|
| `IDLE` | آماده | Ready and waiting. |
| `LISTENING` | در حال شنیدن… | Capturing speech (STT). |
| `PROCESSING` | در حال فکر کردن… | Routing / calling a skill or the AI. |
| `SPEAKING` | در حال صحبت… | Playing the response (TTS). |
| `ERROR` | خطا | A recoverable error occurred; Betty announces it and returns to idle. |
| `DISABLED` | غیرفعال | The user turned Betty off in settings. |

`BettyCore` owns the transitions and also tracks connectivity (`isOnline`) and the last
error, all exposed as a single `StateFlow<BettyStatus>` that the UI observes.

---

## Requirements

- **JDK 17**
- **Android SDK** with platform **android-36 (Android 16)** and **build-tools 36.x**
- **Gradle 8.13** (via the included wrapper — no separate install needed)
- An Android device or emulator running **API 26 (Android 8.0) or newer**

Key versions (see `gradle/libs.versions.toml`): Kotlin 1.9.24, AGP 8.11.1, Compose BOM
2024.06.00, Hilt 2.51.1, Room 2.6.1, OkHttp 4.12.0.

> **Android 16 (API 36) notes.** The app targets `compileSdk`/`targetSdk` **36** and is built
> with **AGP 8.11.1 + Gradle 8.13**. Edge-to-edge is handled explicitly via
> `enableEdgeToEdge()` in `MainActivity` (Android 15+ enforces edge-to-edge for apps
> targeting SDK 35+). All native libraries are **16 KB page-size aligned** (ELF `0x4000`
> LOAD segments + `zipalign -P 16`), satisfying the Android 15/16 requirement for 16 KB
> devices. The release APK is signed with the **v2 + v3** signature schemes.

---

## Building & running

```bash
# 1. Point the build at your JDK and Android SDK (adjust paths as needed).
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/opt/android-sdk

# 2. Build the debug APK.
./gradlew :app:assembleDebug
#    → app/build/outputs/apk/debug/app-debug.apk

# 3. Build the (minified, signed) release APK.
./gradlew :app:assembleRelease
#    → app/build/outputs/apk/release/app-release.apk

# 4. Install on a connected device/emulator.
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 5. Run the JVM unit tests.
./gradlew :app:testDebugUnitTest

# 6. Run the instrumentation tests (requires a device/emulator).
./gradlew :app:connectedDebugAndroidTest
```

> **Low-memory machines.** `gradle.properties` is tuned for a machine with ~4 GB RAM
> (`-Xmx1536m`, single worker, release lint disabled during assemble). If you have more
> memory you can raise these values and re-enable `lint.checkReleaseBuilds`.

### Signing the release build

Create `keystore.properties` in the project root (it is git-ignored) using
`keystore.properties.example` as a template:

```properties
storeFile=betty-release.keystore
storePassword=********
keyAlias=betty
keyPassword=********
```

Then generate a keystore if you do not have one:

```bash
keytool -genkeypair -v -keystore betty-release.keystore \
  -alias betty -keyalg RSA -keysize 2048 -validity 10000
```

If `keystore.properties` is absent, the release build simply produces an **unsigned** APK.

---

## Configuration & secrets

Betty never hard-codes secrets. All user-facing configuration lives in the app's **Settings
screen** and is persisted reactively (DataStore for non-secret values, an
`EncryptedSharedPreferences` file backed by the Android Keystore for the API key).

| Setting | Default | Where it is stored |
|---------|---------|--------------------|
| Betty enabled | `true` | DataStore |
| Voice replies | `true` | DataStore |
| Language | `fa-IR` | DataStore |
| TTS voice | system default | DataStore |
| AI provider id | `openai_compatible` | DataStore |
| AI base URL | *(empty)* | DataStore |
| AI model | `gpt-4o-mini` | DataStore |
| AI API key | *(none)* | **EncryptedSharedPreferences** |
| Wake word | `سلام بتی` | DataStore |
| Display name | *(from profile)* | Room `user_profile` |

`config.example.properties` documents the non-secret defaults you may want to pre-seed for
a build. There is **no** `.env` file with secrets committed to the repository; secrets are
entered by the user at runtime and stored encrypted.

To point Betty at an OpenAI-compatible endpoint, open **Settings → هوش مصنوعی** and set the
base URL (e.g. `https://api.openai.com/v1`), the model and the API key.

---

## How to add a new Skill (complete example)

A skill is a self-contained capability. Adding one touches **two files** and never modifies
the core engine.

### Step 1 — Implement the `Skill` interface

Create `app/src/main/java/com/bettyai/assistant/skills/builtin/BatterySkill.kt`:

```kotlin
package com.bettyai.assistant.skills.builtin

import com.bettyai.assistant.skills.Skill
import com.bettyai.assistant.skills.SkillContext
import com.bettyai.assistant.skills.SkillInput
import com.bettyai.assistant.skills.SkillIntent
import com.bettyai.assistant.skills.SkillResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A tiny example skill: it reports the device battery level.
 * Intents are matched by the CommandRouter using keywords and/or regex.
 */
@Singleton
class BatterySkill @Inject constructor(
    private val batteryReader: BatteryReader   // any dependency you need
) : Skill {

    override val id: String = "battery"
    override val name: String = "باتری"
    override val description: String = "گفتن درصد شارژ باتری"

    override val intents: List<SkillIntent> = listOf(
        SkillIntent(
            id = "ask_battery",
            description = "پرسیدن شارژ باتری",
            keywords = listOf("باتری", "شارژ"),
            examples = listOf("شارژ باتری چنده", "باتری چند درصد است")
        )
    )

    override suspend fun execute(input: SkillInput, context: SkillContext): SkillResult {
        val level = batteryReader.levelPercent()
        return SkillResult(
            spokenText = "شارژ باتری ${level} درصد است.",
            data = mapOf("level" to level.toString())
        )
    }
}
```

Key points:

- **`id`** is a stable unique string used by the router and the settings screen.
- **`intents`** declare how the utterance matches this skill. `keywords` are matched by
  ratio (≥ 0.6 of the keywords present) and any `regex` match yields full confidence.
- **`permissions`** (optional) declares Android permissions the skill may need; they are
  requested lazily by the permission manager, never up front.
- **`execute`** must not throw — return a `SkillResult`. If you cannot handle the intent,
  return `SkillResult(spokenText = "...", handled = false)`.
- Use **`context`** to reach the user's name (`context.userDisplayName`), the language, the
  clock (`context.now()`) and memory (`context.remember(...)`, `context.recall(...)`).

### Step 2 — Register it in the Hilt skill module

Open `app/src/main/java/com/bettyai/assistant/core/di/SkillModule.kt` and add one line:

```kotlin
@Binds
@IntoSet
abstract fun bindBatterySkill(impl: BatterySkill): Skill
```

That's it. `SkillManager` discovers the skill automatically, persists its metadata, exposes
it as a toggle in the settings screen, and the `CommandRouter` will start routing matching
utterances to it. **No core code changes are required.**

### Step 3 (optional) — Test it

Add a JVM unit test under `app/src/test/.../skills/` following the pattern in
`BuiltinSkillsTest.kt`, verifying that `execute` returns the expected `spokenText`.

---

## How to add a new provider

Providers are injected as sets, so adding one is again a single `@Binds @IntoSet` line in
`core/di/ProviderModule.kt` plus the implementation.

### A new AI provider

Implement `domain/provider/AIProvider.kt`:

```kotlin
class MyAiProvider @Inject constructor(private val http: OkHttpClient) : AIProvider {
    override val id = "my_ai"
    override val displayName = "My AI"
    override val requiresApiKey = true
    override fun isConfigured(config: AiRuntimeConfig) = config.apiKey != null
    override suspend fun complete(request: AiRequest, config: AiRuntimeConfig): AiResponse {
        // call your endpoint, map to AiResponse
    }
}
```

Then register it:

```kotlin
@Binds @IntoSet abstract fun bindMyAi(impl: MyAiProvider): AIProvider
```

The user can now select it by its `id` in settings; `AIEngine` picks it up automatically.

### A new Speech (STT) or TTS provider

Implement `domain/provider/SpeechProvider.kt` or `domain/provider/TtsProvider.kt` and bind
it with `@IntoSet`. `VoiceEngine` and `TtsManager` select the active provider from settings
and fall back gracefully if none is available.

### Automation (n8n) and Agent (Flowise) providers

The `AutomationProvider` and `AgentProvider` interfaces are already defined and have
reference implementations (`N8nAutomationProvider`, `FlowiseAgentProvider`). They are wired
as extension points so future skills can trigger workflows or delegate to an agent without
the core depending on any specific service.

---

## Database structure

Betty uses Room (`data/local/BettyDatabase.kt`, version 1, schema exported to
`app/schemas`). The tables are:

| Table | Entity | Purpose |
|-------|--------|---------|
| `user_profile` | `UserProfileEntity` | Single row: display name, preferred language, timestamps. |
| `conversations` | `ConversationEntity` | Conversation threads (title, timestamps). |
| `messages` | `MessageEntity` | Messages in a conversation (role, content, timestamp, optional `skillId`). FK → `conversations` with `ON DELETE CASCADE`. |
| `memories` | `MemoryEntity` | Long-term memory key/value pairs with type and importance. Unique index on `key`. |
| `skills` | `SkillEntity` | Registered skills and their enabled/disabled state. |
| `app_settings` | `AppSettingsEntity` | Key/value settings snapshot (DataStore remains the reactive source of truth). |

All access goes through repositories (`ConversationRepository`, `MemoryRepository`,
`UserRepository`, `SettingsRepository`), so the storage engine can be swapped without
touching the domain or UI.

---

## Wake word & Android limitations

Betty's architecture includes a `WakeWordEngine` abstraction with the wake phrase
**«سلام بتی»** and a `DefaultWakeWordEngine` implementation. However, this project is
**honest about Android's real constraints** and does **not** ship hidden, unauthorized
always-on microphone behaviour:

- Android does not allow an app to keep the microphone open indefinitely in the background
  without a **visible foreground service** and the user's explicit consent. There is no
  official, battery-safe "always listening" API for third-party apps.
- The core therefore supports **push-to-talk** out of the box, and the wake-word engine is
  a ready seam. To enable a real wake-word experience you would plug in an on-device
  wake-word engine (e.g. Porcupine, Snowboy-style, or a custom TFLite model) behind the
  `WakeWordEngine` interface, backed by the transparent foreground service and its
  always-visible notification.
- The settings screen shows the wake-word toggle **disabled with an explanatory note** when
  the platform/engine cannot support it, rather than pretending it works.

This keeps the architecture ready for a proper wake-word implementation while never faking
a capability the platform does not permit.

---

## Security

- **No secrets in source.** The AI API key is entered at runtime and stored in an
  `EncryptedSharedPreferences` file backed by the Android Keystore. There is no key in the
  repository, in `BuildConfig`, or in logs.
- **Minimal permissions.** Only `RECORD_AUDIO`, `INTERNET`, `ACCESS_NETWORK_STATE`,
  `POST_NOTIFICATIONS` and the foreground-service permissions are declared, and runtime
  permissions are requested only when a feature needs them.
- **No sensitive logging in release.** The logger has DEBUG/INFO/WARNING/ERROR levels and
  never logs secrets; verbose logs are for debug builds.
- **HTTPS.** The AI provider talks to the configured endpoint over HTTPS.
- **Safe errors.** Every subsystem reports failures through the typed `BettyError`
  hierarchy, each carrying a user-friendly Persian message — the app never crashes on a
  failed turn.

---

## Testing

The project ships three layers of tests:

- **Unit tests (JVM)** — 54 tests across the text normalizer, Persian date/digit helpers,
  command router, skill manager, built-in skills, AI engine, memory manager and the result
  type. Run with `./gradlew :app:testDebugUnitTest`.
- **Integration tests (instrumentation)** — `BettyDatabaseInstrumentedTest` exercises the
  real Room/SQLite engine in-memory, including the conversation→message cascade delete and
  the unique memory key. Run with `./gradlew :app:connectedDebugAndroidTest`.
- **UI tests (instrumentation)** — `BettyComponentsUiTest` verifies the status indicator and
  the push-to-talk button reflect the state machine and invoke their callbacks only when
  enabled.

The intended end-to-end manual path is: **Open app → grant permission → Home → tap Talk →
speak → STT → AI/skill → response → TTS → conversation history.**

---

## Roadmap

The core is deliberately built so the following can be added purely additively:

- Real on-device wake-word detection behind the existing `WakeWordEngine` seam.
- New skills (calls, WhatsApp, reminders, smart-home, trading, …).
- Additional AI/STT/TTS providers and the n8n/Flowise automation and agent flows.
- Memory consolidation, richer personalization and cloud sync.

---

*Betty is a small personal voice-AI operating system — built to grow without being
rewritten.*
