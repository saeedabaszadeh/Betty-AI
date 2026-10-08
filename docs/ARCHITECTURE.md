# Betty AI — Architecture

This document describes the internal architecture of the Betty core engine in depth: the
layers, the responsibilities of each subsystem, the data and control flows, the extension
seams, and the design decisions that make Betty a *modular voice-AI operating system* rather
than a single chatbot.

---

## 1. Design goals

Betty is built around four non-negotiable goals:

1. **Modular and extensible from day one.** New capabilities must be addable as
   skills/modules without editing the core. This is achieved with dependency injection of
   *sets* (`Set<Skill>`, `Set<AIProvider>`, …) and single-line registrations.
2. **Clean separation of concerns.** Presentation, domain and data are distinct; the UI
   holds no business logic; the domain has no Android dependencies.
3. **Never crash, always degrade gracefully.** Every subsystem returns typed results; the
   AI layer has offline and unconfigured fallbacks; skills cannot throw out of the manager.
4. **Honest about the platform.** No hidden always-on microphone behaviour; wake word is a
   ready seam, not a fake feature.

---

## 2. Layered view

```
Presentation ──► Core (BettyCore) ──► Domain ◄── Data
   (Compose)        (orchestration)    (interfaces)  (Room/DataStore/HTTP)
```

### 2.1 Presentation layer (`ui/`)

- **Screens** are stateless Compose functions that render an immutable `UiState`.
- **ViewModels** expose `StateFlow<UiState>`, translate user intents into calls on
  `BettyCore`/managers, and never touch Android framework APIs directly.
- **Navigation** is a single-activity `NavHost` (`ui/navigation/BettyNavHost.kt`) with routes
  for home, conversation, settings and permissions.
- **Design system** (`ui/theme`, `ui/components`) centralises colours, typography, shapes
  and reusable widgets (logo, status indicator, talk button) so every screen is consistent.

The ViewModels are the only place that combine flows from the core (status, conversations,
profile, settings) into a screen model. Because the state is a `StateFlow`, the UI is fully
reactive: when `BettyCore` transitions to `LISTENING`, the home screen updates automatically.

### 2.2 Core layer (`core/`)

`BettyCore` is the heart of the system. It:

- owns the **state machine** (`BettyState`) and the richer `BettyStatus` (state + detail +
  connectivity + last error);
- exposes `initialize()`, `startVoiceTurn()`, `submitText()`, `stop()` and `shutdown()`;
- runs a **turn pipeline**: `LISTENING → PROCESSING → SPEAKING → IDLE`, persisting every
  user and Betty message to the conversation repository;
- publishes lifecycle **events** (`BettyEvent`) on the `EventBus` for future modules;
- never throws — a failed turn is caught, logged and surfaced as a Persian error message.

Supporting core pieces:

- **`BettyState` / `BettyStatus`** — the state machine.
- **`EventBus`** — a `SharedFlow`-based bus (`StateChanged`, `UserSpoke`, `BettyReplied`,
  `SkillExecuted`, `ErrorOccurred`, `NetworkChanged`) so features can react without coupling.
- **`BettyLogger`** — levelled logging (DEBUG/INFO/WARNING/ERROR) that never logs secrets.
- **`BettyResult` / `BettyError`** — a typed result and a sealed error hierarchy where every
  error carries a machine-readable `code` and a Persian `userMessage`.
- **DI modules** (`core/di`) — `AppModule` (database/DAOs), `RepositoryModule` (repositories),
  `ProviderModule` (provider sets), `SkillModule` (skill set).

### 2.3 Domain layer (`domain/`)

Pure Kotlin. Contains:

- **Models** — `Conversation`, `Message`, `UserProfile`, `Memory`, `SkillInfo`, `AppSettings`,
  `AiModels`, `VoiceModels`.
- **Repository interfaces** — `ConversationRepository`, `MemoryRepository`, `UserRepository`,
  `SettingsRepository`.
- **Provider interfaces** — `AIProvider`, `SpeechProvider`, `TtsProvider`,
  `AutomationProvider`, `AgentProvider`, `NetworkMonitor`.

Because this layer depends on nothing platform-specific, it is fast to unit-test and stable
against infrastructure changes.

### 2.4 Data layer (`data/`)

Implements the domain interfaces:

- **Room** — `BettyDatabase` with six entities and their DAOs; repositories wrap DAOs and
  map entities ↔ domain models (`data/mapper/Mappers.kt`).
- **DataStore** — `SettingsRepositoryImpl` stores non-secret settings reactively.
- **EncryptedSharedPreferences** — `SecureConfigStore` stores the AI API key (and any future
  provider secrets) using the Android Keystore.
- **Network** — `HttpClientProvider` (OkHttp) and `NetworkMonitorImpl` (ConnectivityManager).

---

## 3. The voice/AI turn pipeline

A single turn (voice or typed) flows through the system as follows:

```
User taps Talk ─► BettyCore.startVoiceTurn()
   │
   ├─ state = LISTENING
   ├─ VoiceEngine.listen()  ──► SpeechProvider (Android SpeechRecognizer)
   │        └─ SpeechResult.Success / NoMatch / Cancelled / Failure
   │
   ├─ state = PROCESSING
   ├─ persist USER message
   ├─ CommandRouter.route(text)
   │        ├─ RouteDecision.SkillMatch ─► SkillManager.execute(...) ─► SkillResult
   │        └─ RouteDecision.AiFallback ─► AIEngine.respond(text, history)
   │
   ├─ persist BETTY message
   ├─ state = SPEAKING
   ├─ TtsManager.speak(response) ──► TtsProvider (Android TextToSpeech)
   │
   └─ state = IDLE
```

Every stage is wrapped so that a failure at any point produces a friendly error and returns
Betty to `IDLE` instead of crashing.

---

## 4. Extension seams

The architecture is designed so that growth is **additive**. The table below lists each seam
and what you do to extend it.

| Seam | Interface | Register in | Effect |
|------|-----------|-------------|--------|
| New skill | `skills/Skill` | `core/di/SkillModule` (`@Binds @IntoSet`) | Router can match it; settings shows a toggle |
| New AI provider | `domain/provider/AIProvider` | `core/di/ProviderModule` | Selectable in settings; `AIEngine` uses it |
| New STT provider | `domain/provider/SpeechProvider` | `core/di/ProviderModule` | `VoiceEngine` uses it |
| New TTS provider | `domain/provider/TtsProvider` | `core/di/ProviderModule` | `TtsManager` uses it |
| Automation (n8n) | `domain/provider/AutomationProvider` | `core/di/ProviderModule` | Skills can trigger workflows |
| Agent (Flowise) | `domain/provider/AgentProvider` | `core/di/ProviderModule` | Skills can delegate to an agent |
| Wake word engine | `voice/wakeword/WakeWordEngine` | `core/di` | Enables real wake-word detection |

Because consumers depend on the **set**, not on concrete classes, no engine code changes
when you add an implementation.

---

## 5. State machine

`BettyState` is a closed enum with an `isBusy` helper:

```
IDLE ⇄ LISTENING ⇄ PROCESSING ⇄ SPEAKING
  │
  ├─ ERROR      (transient; auto-returns to IDLE)
  └─ DISABLED   (user turned Betty off)
```

Transitions are owned exclusively by `BettyCore`; the UI only observes. The settings flow is
also observed so that toggling "Betty enabled" immediately moves the machine between
`DISABLED` and `IDLE`.

---

## 6. Persistence & data model

- **Conversations** group **messages**; deleting a conversation cascades to its messages
  (foreign key with `ON DELETE CASCADE`).
- **Messages** record the role (USER/BETTY/SYSTEM), content, timestamp and the optional
  `skillId` that produced a Betty reply — useful for analytics and for future "which skill
  answered this?" features.
- **Memories** are key/value pairs with a type and an importance score; the `key` is unique
  so upserts replace. `PromptBuilder` injects the most important memories into the AI system
  prompt, giving Betty lightweight long-term memory.
- **Skills** persist their enabled/disabled state so user preferences survive restarts.
- **AppSettings** provides a database-level snapshot of settings for future modules that
  need to query them at the storage layer; DataStore remains the reactive source of truth.

---

## 7. Error handling model

All failures are modelled as `BettyError` subclasses (`MicrophoneUnavailable`,
`SpeechRecognitionFailed`, `NetworkUnavailable`, `AiProviderUnavailable`, `AiNotConfigured`,
`TtsUnavailable`, `PermissionDenied`, `StorageError`, `Disabled`, `Unknown`). Each carries a
`code` and a `userMessage`. Subsystems catch their own exceptions and either return a
`BettyResult.Failure` or a graceful fallback, so the user always gets a coherent Persian
message and the app keeps running.

---

## 8. Threading & concurrency

- `BettyCore` runs turns on a `SupervisorJob` + `Dispatchers.Default` scope; a single
  `turnJob` guards against overlapping turns.
- Room and DataStore expose `Flow`/`suspend` APIs consumed from coroutines.
- The state is a `StateFlow`, so observers are conflated and lifecycle-safe.

---

## 9. Why this is not "just a chatbot"

A chatbot is a text box in front of an LLM. Betty is an *operating system* for voice tasks:
she owns a state machine, a voice pipeline, a command router that decides between local
skills and the LLM, a skill registry that is extensible without core changes, a memory
layer, a settings/permissions surface, a local database and a foreground service. The LLM is
just one provider behind an interface — the same way a future call skill, smart-home skill
or automation flow would be. The core stays small and stable while capabilities accumulate
around it.
