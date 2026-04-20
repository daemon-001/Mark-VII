# Changelog

All notable changes to the Mark VII project are documented in this file.

---
## v3.3.1 (30310)
April 20, 2026 20:26:19 +0530

### Retry UX & Traceability
- **Retry Source Linking**: Retry actions now preserve metadata about the original user prompt (`retryOfPrompt`) so regenerated assistant replies remain traceable.
- **"Retried From" Tag**: Assistant retry bubbles show a compact source-prompt tag; tapping it scrolls directly to the originating user message.
- **Prompt Highlight Blink**: When jumping to a source prompt, the target user bubble gets a temporary blinking accent border for quick visual confirmation.
- **Cleaner Retry Context**: Retry generation trims context at the retried response point so stale assistant output and later turns do not pollute regenerated responses.

### Streaming Performance & Rendering
- **Throttled Stream Updates**: Streaming chunk UI updates are rate-limited to roughly 60 FPS (16ms cadence) to reduce Compose churn and improve smoothness.
- **Pure Compose Streaming Renderer**: Added `StreamingMarkdown` for low-flicker, in-progress markdown rendering during live generation.
- **Full Markdown Post-Stream**: After streaming completes, rendering switches to `MarkdownWithCodeCopy` for full markdown display and code copy actions.
- **Localized Haptics**: Haptic feedback now triggers from streaming chunk growth inside message UI (debounced) instead of global state-based triggers.
- **Voice Waveform Optimization**: Mic waveform rendering moved to a single `Canvas` path, replacing many per-bar composables and reducing recompositions.
- **Auto-Scroll Refinement**: Chat list auto-scroll now reacts to new message arrival instead of generation-state toggles for more stable scrolling behavior.

### Session Persistence & Drawer Stability
- **Per-User Session Cache**: Added local file-based caching of chat sessions (`chat_sessions_<userId>.json`) for faster drawer/session loading.
- **Cache-First Session Load**: Session list now hydrates from local cache first, then refreshes from Firestore and rewrites cache.
- **Broader Session Persistence**: Session cache is updated during create/save/rename/delete/session-sync flows to keep local and remote state aligned.
- **IO Threading Improvements**: Session/network and migration workflows were moved to IO dispatchers to reduce UI-thread blocking risk.
- **Drawer Recomposition Hardening**: Drawer callbacks were stabilized (`remember`/`rememberUpdatedState`), date formatter reuse was added, and primitive props are passed to session rows for more stable recomposition.
- **Scaffold Cleanup**: Removed the one-time welcome-guide trigger hack from `MainActivity` bottom bar scaffolding.

### Provider & Request Handling Fixes
- **Groq in Retry Selector**: Groq models are now fully available in the retry-time model selector path alongside Gemini and OpenRouter.
- **Provider Capability Guard**: Selecting Groq for image-based retries now surfaces a clear warning because image input is unsupported for Groq in-app.
- **Duplicate Request Cleanup**: Removed redundant prompt insertion in OpenRouter request message assembly to avoid duplicate user-message payloads.

---
## v3.3.0 (30300)
March 20, 2026 15:20:00 +0530

### Groq — Third AI Provider
- **Groq Integration**: Added Groq as a third AI provider alongside OpenRouter and Gemini, giving access to ultra-fast inference models (Llama 3, Mixtral 8x7B, Gemma 2, and more)
- **New `GroqApi.kt`**: OpenAI-compatible Retrofit client (`api.groq.com/openai/v1/`) with streaming SSE support, model listing, and `verifyKey()` helper
- **Dynamic Model Fetching**: All active Groq models fetched live from the Groq `/models` endpoint and cached — no hardcoded list required
- **Firestore Key Management**: Groq API key read from `app_config/api_keys` document (field: `groqApiKey`) following the same Firebase-first pattern as OpenRouter and Gemini
- **User Key Override**: Personal Groq API key can be entered in Settings → Groq API Key; takes priority over the Firebase key when enabled
- **Streaming Responses**: Full SSE streaming for Groq chat completions with conversation history context (last 6 messages)
- **`ApiProvider.GROQ`**: Added to the `ApiProvider` enum; `ChatViewModel` routes requests to the Groq client; image uploads correctly return an unsupported-provider error
- **`UserApiPreferences`**: Added `groqApiKey` and `isGroqKeyEnabled` fields with SharedPreferences persistence and `StateFlow` reactivity

### Model Selector UI
- **Groq Tab**: Added "Groq" provider button to the model-selector dropdown alongside existing Gemini and OpenRouter tabs
- **Wider Dropdown**: Increased model selector dropdown width from 280dp to 320dp to comfortably fit all three provider tabs
- **Compact Tab Labels**: Reduced provider tab font size from 13sp to 11sp to prevent "OpenRouter" text from appearing cramped

### Settings
- **Groq API Key Card**: New `UserApiConfigItem` for Groq in the Settings screen with verify button, toggle, and link to [console.groq.com/keys](https://console.groq.com/keys)

---
## v3.2.0 (30200)
February 9, 2026 14:30:00 +0530

### User API Key Management
- **Personal API Keys**: Introduced `UserApiPreferences` system allowing users to add and manage their own Gemini and OpenRouter API keys
- **API Key Verification**: Added real-time API key verification with loading states, error handling, and visual feedback in Settings
- **Key Priority System**: User-provided API keys now take priority over app default keys when enabled, with seamless fallback support
- **Settings UI Enhancement**: New `UserApiConfigItem` component for API key input, toggle enable/disable, and verification
- **Persistent Storage**: API keys and enabled states are stored securely using SharedPreferences with reactive StateFlow updates

### Documentation & Visuals
- **README Enhancement**: Added OpenRouter model visuals and comprehensive usage examples
- **Documentation Assets**: Added 10 PNG visual assets for v3.1.0 feature documentation

---
## v3.1.0 (30100) 
December 7, 2025 05:20:00 +0530

### Explore UI & Guest Experience
- **New Explore UI**: Replaced the empty state "Getting Started" guide with a dynamic "Explore" screen featuring a "Bot Hello" Lottie animation and quick-access feature chips (Gemini 1.5 Pro, Image Analysis, PDF Export, etc.).
- **Guest "New Chat"**: Enabled the "New Chat" button in the navigation drawer for guest users, allowing them to easily reset sessions without signing in.

### UI/UX Refinements
- **Immersive Chat List**: Added smooth gradient fade effects at both the top and bottom of the chat list.
- **Top Bar Redesign**: Increased top bar height and added a vertical gradient background for a seamless "fade-under" scrolling effect.
- **Keyboard Optimization**: Fixed issue where chat messages were hidden behind the keyboard by properly handling IME insets.
- **Compact Action Buttons**: Reduced the size of chat action buttons (Copy, Speak, Retry, Share, Export) for a cleaner, less intrusive look.
- **Onboarding Animation**: Smoothed out the transition animation for the "Get Started" button on the onboarding screen.

---

## [3.0.0]

---


## December 5, 2025 04:22:40 +0530

### Custom Speech Recognition with Moving Waveform Timeline
- Implemented custom SpeechRecognizer to eliminate system toast notifications
- Added real-time moving waveform visualization (60 bars) that scrolls right-to-left during voice input
- Added dynamic text-to-speech speakericon at below of the response
- Waveform replaces model selector and plus icon with smooth scale transitions (200ms enter, 150ms exit)
- Mic icon displays circular background highlight when listening
- Constant timeline speed (30ms intervals) for smooth, consistent waveform movement
- Voice pitch detection using RMS dB values for real-time amplitude visualization
- Waveform bars with curved edges (2dp radius) and fade effect for older bars
- Center line reference and red position indicator for professional audio editor aesthetic
- Comprehensive error handling for microphone permissions and recognition failures
- Proper resource cleanup to prevent memory leaks

---


## November 29, 2025 05:15:00 +0530

### UI/UX Improvements & Animations
- Fixed InfoTab card vertical alignment and app version positioning
- Added clickable GitHub repository link in About section
- Enhanced app-wide animations: smooth navigation transitions, staggered card entrance effects, dynamic button scaling, and optimized 60fps performance
- Fixed model reload issue when navigating between screens

---

## November 28, 2025 17:32:54 +0530

### Multilingual Text-to-Speech
Integrated MLKit Language Identification for automatic language detection. Text-to-Speech now dynamically switches to the appropriate language module (Chinese, Japanese, Korean, Spanish, French, German, Italian, Portuguese, Russian, Arabic, Hindi, English) based on content, significantly improving pronunciation accuracy for multilingual responses.

---

## November 28, 2025 15:33:21 +0530

### Theme System, Authentication & Chat Sessions
- Dynamic theme support with seamless light/dark mode switching and system default option
- Custom AppColors system with iOS-style palettes and persistent preferences
- Firebase Authentication integration with Google Sign-In
- User profile display in navigation drawer with session management
- Persistent chat sessions stored in Firestore
- Create, switch, rename, and delete conversations with timestamp organization

---

## November 22, 2025 13:58:48 +0530

### PDF Export
Export chat conversations to professionally formatted PDF files with branding, timestamps, and model information. Supports sharing and local storage.

---

## November 22, 2025 04:18:17 +0530

### Gemini API & Chat History
- Dual API provider support: Toggle between OpenRouter (40+ models) and Gemini API
- Provider-specific error handling and separate model lists
- Visual provider indicator in UI with automatic model list filtering
- Chat history management with conversation persistence

---

## November 20, 2025 21:00:08 +0530

### Streaming AI Responses
Real-time streaming support with live updates, haptic feedback, and stop streaming functionality for better user interaction.

---

## November 20, 2025 14:24:41 +0530

### UI Refactor & Code Block Copy
Enhanced code block copy functionality in chat interface with improved UI layout and better user experience.

---

## November 20, 2025 13:33:16 +0530

### UI & Markdown Enhancements
Removed Firebase model dependencies, updated UI for better usability, and added comprehensive markdown support to chat interface.

---

## November 20, 2025 09:13:16 +0530

### Error Handling & Performance
- Robust error handling system for chat failures
- Model ID auto-correction with automatic `:free` postfix addition
- API response time optimizations:
  - Connection timeout: 60s → 30s
  - Connection pooling: 5 persistent connections
  - Context window: 10 → 6 messages
  - Token limit: 2000 → 3000
  - Call timeout: 120s
- UI performance improvements with memoized operations

---

## November 7, 2025 08:22:02 +0530

### Voice Input & UI Improvements
Added voice input support with improved chat UI, larger touch targets (40dp), toast notifications, and better visual feedback throughout the app.

---

## [2.0.0] - October 22, 2025

---

## October 22, 2025 01:28:14 +0530

### Major Platform Transformation
Complete overhaul from single-provider Gemini app to multi-provider AI platform with cloud configuration.

**Firebase Integration:**
- Firebase Firestore for remote configuration management
- Dynamic model loading from Firebase at startup
- Remote API key management via Firebase
- FirebaseConfigManager.kt and FirebaseConfig.kt
- Automatic fallback to default configuration
- Real-time updates without app updates

**OpenRouter API Integration:**
- Switched from Gemini API to OpenRouter API
- Access to 100+ AI models from multiple providers
- Support for Claude (Anthropic), GPT (OpenAI), Llama (Meta), Mistral, Deepseek, and more
- Unified API interface for all providers
- Better error handling with specific HTTP status codes

**Models Available:**
- Anthropic: Claude 3.5 Sonnet, Claude 3 Opus, Claude 3 Haiku
- OpenAI: GPT-4 Turbo, GPT-4, GPT-3.5 Turbo
- Meta: Llama 3.1 70B, Llama 3.1 8B, Llama 3.3
- Mistral: Mistral Large, Mistral Medium, Mistral Small
- Deepseek: Deepseek Chat, Deepseek R1
- Google: Gemini (via OpenRouter)
- And 40+ more models

**Static Welcome Guide:**
- Replaced AI-generated greeting with instant static guide
- No API call needed on app startup (275x faster)
- Quick start instructions for new users
- Zero cost per app launch (was $0.001)

**Model Brand Display:**
- Dynamic "Mark VII x Brand" header in AI responses
- Shows which AI provider generated each response
- Automatic brand extraction from model identifier

**Configuration Architecture:**
- Removed ALL hardcoded credentials
- 100% Firebase-driven model configuration
- 100% Firebase-driven API key management
- Clean separation of concerns (MVVM architecture)

**Error Handling System:**
- Comprehensive HTTP error handling (401, 403, 404, 429)
- Clear user instructions for API key issues
- Actionable solutions and troubleshooting documentation

**Theme Rebranding:**
- Renamed from GeminiChatBotTheme to MarkVIITheme
- Better brand alignment

**Code Cleanup:**
- Removed all Gemini API references
- Removed hardcoded Gemini models
- Clean codebase with zero legacy code

**Python Management Tools:**
- Firebase model updater script (update_firebase_models.py)
- CSV import/export support
- Interactive management mode

**Documentation Suite:**
- 26 comprehensive documentation files
- Setup guides, troubleshooting resources, technical documentation

**Security Hardening:**
- Updated .gitignore to exclude sensitive files
- API keys stored in Firebase (not in code)
- Secure HTTPS communication

**Build System Updates:**
- Updated Gradle dependencies
- Added Firebase BOM, Retrofit, OkHttp
- Removed deprecated Gemini dependencies

**Performance Impact:**
- Startup speed: 2.65s → 110ms (24x faster)
- API cost per launch: $0.001 → $0.00
- Network dependency: Eliminated for startup

**Dependencies Added:**
```gradle
// Firebase
firebase-bom:33.7.0
firebase-database-ktx
firebase-firestore-ktx
firebase-analytics-ktx

// Networking
retrofit:2.9.0
converter-gson:2.9.0
okhttp:4.12.0
logging-interceptor:4.12.0
gson:2.10.1
```

**Dependencies Removed:**
```gradle
generativeai // Gemini API
```

---

## October 21, 2025 11:45:34 +0530

### Build System Update
Updated Android Gradle plugin to 8.13.0 for better performance and compatibility.

---

## October 17, 2025 21:20:44 +0530

### Gradle & Android Plugin Updates
Updated Gradle and Android plugin versions for improved build performance and latest feature support.

---

## August 30, 2025 12:39:26 +0530

### Model Configuration Refactor
Refactored model selection system and centralized model configuration for better maintainability and easier updates.

---

## [1.0.0] - December 10, 2024

---

## December 10, 2024 15:08:15 +0530

### Major UI and Features Upgrade
Comprehensive UI overhaul with enhanced features, improved user experience, and modern design patterns.

---

## December 10, 2024 00:10:07 +0530

### Build v1.0 Source Code Release
Initial production release with complete source code, build configurations, and documentation.

---

## December 8, 2024 13:24:30 +0530

### Function Updates
Updated core functions for improved performance and stability.

---

## December 7, 2024 23:17:36 +0530

### Bug Fix: Model Overload Error
Fixed "Model is overloaded" error with better error handling and retry logic.

---

## December 7, 2024 19:52:06 +0530

### Multiple NLP Model Support
Added support for multiple NLP models with example implementations in ChatData.kt.

---

## December 7, 2024 19:40:12 +0530

### Version 1.0 Initial Release
First stable release of Mark VII with core AI chat functionality, Gemini integration, and basic UI.