# Betty AI — Folder Structure

This document is a complete, annotated map of the repository. Every source file is listed so
you can find where a responsibility lives before adding code.

---

## Repository root

```
betty-ai/
├── app/                         # The single Android application module
├── docs/                        # Architecture & folder-structure documentation
├── gradle/
│   └── libs.versions.toml       # Version catalog — single source of dependency truth
├── build.gradle.kts             # Root build script
├── settings.gradle.kts          # Module includes & repositories
├── gradle.properties            # JVM/build flags (tuned for low-memory machines)
├── gradlew / gradlew.bat        # Gradle wrapper
├── config.example.properties    # Example non-secret configuration
├── keystore.properties.example  # Example signing config (real file is git-ignored)
└── README.md                    # Project overview, build & extension guide
```

---

## `app/` — the application module

```
app/
├── build.gradle.kts             # SDK levels, signing, build types, dependencies
├── proguard-rules.pro           # R8/ProGuard keep rules (serialization, Tink, Room…)
└── src/
    ├── main/                    # Production sources
    ├── test/                    # JVM unit tests
    └── androidTest/             # Instrumentation (device) tests
```

### `app/src/main/`

```
main/
├── AndroidManifest.xml          # Permissions, launcher activity, foreground service
├── java/com/bettyai/assistant/  # Kotlin sources (see below)
└── res/                         # Resources
    ├── drawable/                # Vector icons (launcher fg/bg, status icon)
    ├── mipmap-anydpi-v26/       # Adaptive launcher icons
    ├── values/                  # colors.xml, strings.xml, themes.xml
    └── xml/                     # backup_rules.xml, data_extraction_rules.xml
```

---

## Kotlin source tree (`app/src/main/java/com/bettyai/assistant/`)

### Entry points

| File | Responsibility |
|------|----------------|
| `BettyApplication.kt` | `@HiltAndroidApp` application; creates notification channels. |
| `MainActivity.kt` | Single-activity Compose host; splash screen, edge-to-edge, permission prompt, `BettyNavHost`. |

### `ai/` — AI layer

| File | Responsibility |
|------|----------------|
| `AIEngine.kt` | Selects the active AI provider, resolves runtime config + secret key, assembles context, returns offline/unconfigured fallbacks. |
| `OpenAiCompatibleProvider.kt` | HTTP implementation for any OpenAI-compatible chat-completions endpoint. |
| `PromptBuilder.kt` | Builds the system prompt using the real user name and relevant memories. |
| `dto/ChatDtos.kt` | Request/response DTOs for the chat-completions API. |
| `automation/N8nAutomationProvider.kt` | Reference `AutomationProvider` (n8n webhook) extension point. |
| `agent/FlowiseAgentProvider.kt` | Reference `AgentProvider` (Flowise) extension point. |

### `core/` — orchestration, state, events, results, DI

| File | Responsibility |
|------|----------------|
| `BettyCore.kt` | The orchestrator: state machine, turn pipeline, event publishing. |
| `state/BettyState.kt` | `BettyState` enum + `BettyStatus` (state, detail, connectivity, last error). |
| `event/BettyEvent.kt` | Sealed event types. |
| `event/EventBus.kt` | `SharedFlow`-based event bus. |
| `logger/BettyLogger.kt` | Logging interface (DEBUG/INFO/WARNING/ERROR). |
| `logger/AndroidBettyLogger.kt` | Android `Log`-backed implementation. |
| `result/BettyResult.kt` | Typed success/failure result with `map`/`onSuccess`/`onFailure`. |
| `result/BettyError.kt` | Sealed error hierarchy with codes + Persian user messages. |
| `di/AppModule.kt` | Provides the Room database and DAOs. |
| `di/RepositoryModule.kt` | Binds repository implementations. |
| `di/ProviderModule.kt` | Registers all providers into their sets (`@IntoSet`). |
| `di/SkillModule.kt` | Registers all skills into the skill set (`@IntoSet`). |

### `domain/` — pure models & interfaces

| File | Responsibility |
|------|----------------|
| `model/Conversation.kt` | `Conversation`, `Message`, `MessageRole`. |
| `model/UserProfile.kt` | `UserProfile`. |
| `model/Memory.kt` | `Memory`, `MemoryType`. |
| `model/SkillInfo.kt` | `SkillInfo` (settings-facing skill metadata). |
| `model/AppSettings.kt` | `AppSettings` snapshot. |
| `model/AiModels.kt` | `AiRequest`, `AiResponse`, `AiMessage`, `AiRole`, `AiRuntimeConfig`. |
| `model/VoiceModels.kt` | `SpeechResult`, `TtsResult`, `VoiceInfo`. |
| `provider/AIProvider.kt` | AI provider interface. |
| `provider/SpeechProvider.kt` | Speech-to-text provider interface. |
| `provider/TtsProvider.kt` | Text-to-speech provider interface. |
| `provider/AutomationProvider.kt` | Automation (n8n) interface. |
| `provider/AgentProvider.kt` | Agent (Flowise) interface. |
| `provider/NetworkMonitor.kt` | Connectivity observation interface. |
| `repository/ConversationRepository.kt` | Conversation/message persistence interface. |
| `repository/MemoryRepository.kt` | Memory persistence interface. |
| `repository/UserRepository.kt` | User profile interface. |
| `repository/SettingsRepository.kt` | Settings interface. |

### `data/` — implementations

| File | Responsibility |
|------|----------------|
| `local/BettyDatabase.kt` | Room database (6 entities, version 1, schema exported). |
| `local/entity/Entities.kt` | All Room entities + the `ConversationWithCount` projection. |
| `local/dao/UserDao.kt` | User profile queries. |
| `local/dao/ConversationDao.kt` | Conversation queries (incl. message-count projection). |
| `local/dao/MessageDao.kt` | Message queries. |
| `local/dao/MemoryDao.kt` | Memory queries (recall/search/forget). |
| `local/dao/SkillDao.kt` | Skill metadata + enabled state. |
| `local/dao/AppSettingsDao.kt` | Settings snapshot key/value. |
| `mapper/Mappers.kt` | Entity ↔ domain model mapping. |
| `repository/ConversationRepositoryImpl.kt` | Conversation repository implementation. |
| `repository/MemoryRepositoryImpl.kt` | Memory repository implementation. |
| `repository/UserRepositoryImpl.kt` | User repository implementation. |
| `settings/SettingsRepositoryImpl.kt` | DataStore-backed settings repository. |
| `security/SecureConfigStore.kt` | Encrypted secret storage (API key, future secrets). |
| `network/HttpClientProvider.kt` | Shared OkHttp client. |
| `network/NetworkMonitorImpl.kt` | ConnectivityManager-based network monitor. |

### `voice/` — voice pipeline

| File | Responsibility |
|------|----------------|
| `VoiceEngine.kt` | Drives STT via the active `SpeechProvider`. |
| `TtsManager.kt` | Drives TTS via the active `TtsProvider`. |
| `speech/AndroidSpeechRecognizerProvider.kt` | `SpeechProvider` backed by Android `SpeechRecognizer`. |
| `tts/AndroidTtsProvider.kt` | `TtsProvider` backed by Android `TextToSpeech`. |
| `wakeword/WakeWordEngine.kt` | Wake-word detection interface (extension seam). |
| `wakeword/DefaultWakeWordEngine.kt` | Default implementation + wake-phrase handling. |

### `router/` — command routing

| File | Responsibility |
|------|----------------|
| `CommandRouter.kt` | Decides skill vs AI using keyword/regex scoring. |
| `TextNormalizer.kt` | Normalizes Persian text, converts Arabic letters, strips the wake phrase. |

### `skills/` — skill framework & built-ins

| File | Responsibility |
|------|----------------|
| `Skill.kt` | `Skill` contract, `SkillIntent`, `SkillInput`, `SkillResult`. |
| `SkillContext.kt` | The seam skills use to reach name, language, clock and memory. |
| `SkillManager.kt` | Registers, enables/disables, persists and executes skills. |
| `builtin/TimeSkill.kt` | Time & Jalali date. |
| `builtin/GreetingSkill.kt` | Greets the user by their real name. |
| `builtin/HelpSkill.kt` | Explains what Betty can do. |
| `builtin/AboutSkill.kt` | About Betty / version. |
| `builtin/RememberSkill.kt` | Stores a fact in long-term memory. |

### `memory/`, `settings/`, `permissions/`, `notifications/`, `service/`

| File | Responsibility |
|------|----------------|
| `memory/MemoryManager.kt` | Long-term memory facade (remember/recall/search/forget). |
| `settings/SettingsManager.kt` | Settings facade over the repository + secure store. |
| `permissions/PermissionManager.kt` | Checks/requests permissions lazily; exposes statuses. |
| `permissions/BettyPermission.kt` | The set of permissions Betty cares about. |
| `notifications/BettyNotificationManager.kt` | Notification channels and the foreground notification. |
| `service/BettyService.kt` | Foreground service that keeps Betty warm with a visible notification. |

### `ui/` — presentation

| File | Responsibility |
|------|----------------|
| `navigation/BettyNavHost.kt` | Routes + `NavHost` wiring. |
| `home/HomeScreen.kt` | Home: greeting, logo, status, talk button, recent conversations. |
| `home/HomeViewModel.kt` | Home `UiState` + intents. |
| `conversation/ConversationScreen.kt` | Message bubbles, input bar, clear history. |
| `conversation/ConversationViewModel.kt` | Conversation `UiState` + intents. |
| `settings/SettingsScreen.kt` | All settings sections. |
| `settings/SettingsViewModel.kt` | Settings `UiState` + setters. |
| `permissions/PermissionsScreen.kt` | Permission status list with request buttons. |
| `permissions/PermissionsViewModel.kt` | Permission statuses flow. |
| `components/BettyLogo.kt` | Animated equalizer-bars logo. |
| `components/StatusIndicator.kt` | Pulsing status pill. |
| `components/TalkButton.kt` | Large central push-to-talk button. |
| `components/BettyStateVisuals.kt` | `BettyState` → label/colour mapping. |
| `theme/Color.kt` | Brand palette + semantic state colours. |
| `theme/Type.kt` | Typography. |
| `theme/Shape.kt` | Shapes. |
| `theme/Theme.kt` | `BettyTheme` (dark/light, edge-to-edge). |

### `util/`

| File | Responsibility |
|------|----------------|
| `PersianText.kt` | Persian digit conversion, weekday names, Jalali date conversion/formatting. |

---

## Tests

### `app/src/test/` — JVM unit tests (54 tests, all passing)

```
test/java/com/bettyai/assistant/
├── ai/AIEngineTest.kt                 # offline/unconfigured/configured/failure paths
├── core/result/BettyResultTest.kt     # result mapping + every error has a user message
├── memory/MemoryManagerTest.kt        # remember/recall/search/forget
├── router/CommandRouterTest.kt        # skill vs AI routing, regex, wake-phrase stripping
├── router/TextNormalizerTest.kt       # normalization rules
├── skills/BuiltinSkillsTest.kt        # built-in skill behaviour
├── skills/SkillManagerTest.kt         # registry, enable/disable, execute, error handling
└── util/PersianTextTest.kt            # digits, weekdays, Jalali conversion
```

### `app/src/androidTest/` — instrumentation tests

```
androidTest/java/com/bettyai/assistant/
├── data/BettyDatabaseInstrumentedTest.kt  # Room integration (cascade delete, unique key)
└── ui/BettyComponentsUiTest.kt            # Compose UI (status indicator, talk button)
```

---

## Where do I add…?

- **A new skill** → `skills/builtin/` + one line in `core/di/SkillModule.kt`.
- **A new AI/STT/TTS/automation/agent provider** → the matching package under `ai/` or
  `voice/` + one line in `core/di/ProviderModule.kt`.
- **A new screen** → `ui/<feature>/` (screen + ViewModel) + a route in
  `ui/navigation/BettyNavHost.kt`.
- **A new persisted entity** → `data/local/entity/Entities.kt` + a DAO + register it in
  `BettyDatabase` and `core/di/AppModule.kt`.
- **A new setting** → `domain/model/AppSettings.kt` + `data/settings/SettingsRepositoryImpl.kt`
  + the settings screen.
