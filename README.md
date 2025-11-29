# Mark VII

<div align="center">

![Version](https://img.shields.io/badge/version-3.0.0-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-orange.svg)

**Enterprise-grade multi-provider AI chat platform with 45+ models, Firebase cloud configuration, and modern Material 3 design.**

[Features](#features) • [Quick Start](#quick-start) • [Download](#download) • [Documentation](#documentation)

</div>

---

## Overview

**Mark VII** is a production-ready AI chat application for Android that provides unified access to 45+ state-of-the-art AI models from leading providers including Anthropic, OpenAI, Meta, Deepseek, Mistral, Google, and more through a single, elegant interface.

### Key Capabilities

- 🤖 **45+ AI Models** - Gemini, GPT, Llama, Deepseek, Mistral, and more
- 🔥 **Cloud-First Architecture** - Firebase-powered configuration management and real-time sync
- ⚡ **High Performance** - <100ms startup, connection pooling, optimized streaming
- 🎨 **Dual API Support** - OpenRouter (40+ models) + Direct Gemini API
- 📱 **Modern Material 3 UI** - Dynamic theming, smooth animations, haptic feedback
- 🌐 **Multilingual TTS** - Automatic language detection for 15+ languages
- 🔒 **Enterprise Security** - Firebase Authentication, encrypted storage, HTTPS only
- 🐍 **DevOps Tools** - Python CLI for bulk model management via CSV

### Why Mark VII?

**Flexibility:** Switch between providers and models instantly without code changes  
**Reliability:** Automatic error recovery, exception handling, offline capability  
**Performance:** 24x faster than v1.x, real-time streaming, optimized rendering  
**Developer-Friendly:** Complete Firebase integration, comprehensive documentation, open source

---

## Features

### 🤖 AI Model Access

**Dual API Architecture:**
- **OpenRouter Integration** - Direct access to 100+ models with real-time catalog sync
- **Gemini API Integration** - Native Google Gemini support with vision capabilities
- **Seamless Switching** - Toggle between APIs mid-conversation

**Supported Companies:**
- **Google** - Gemini 2.0/2.5 Flash/Pro, Gemma 2/3
- **Anthropic** - Claude 3.5 Sonnet, Opus, Haiku
- **OpenAI** - GPT-4 Turbo, GPT-4o, GPT-3.5
- **Meta** - Llama 3.1/3.3/4 (8B to 405B parameters)
- **Deepseek** - Chat V3.1, R1, R1 Distill variants
- **Mistral AI** - Full lineup from Small to Large
- **Qwen (Alibaba)** - Qwen2.5, Qwen3 Coder
- **xAI** - Grok with vision support
- **30+ more** - Cohere, AI21, Perplexity, and others

### ☁️ Firebase Cloud Integration

- **Remote Model Management** - Add/remove models without app updates
- **Dynamic API Keys** - Update credentials in real-time
- **Instant Sync** - Changes reflect on next app restart
- **Secure Vault** - API keys stored in Firebase, never in code
- **Offline Support** - Cached configuration for offline operation
- **Exception Handling** - Auto-detection and retry for `:free` suffix models
- **Smart Recovery** - Automatic 404 error handling with model correction

### 💬 Advanced Chat Features

- **Real-Time Streaming** - Server-sent events (SSE) for live responses
- **Multi-Modal Support** - Text and image understanding (vision models)
- **Context Management** - 6-message history for optimal performance
- **Session Persistence** - Cloud-synced chat history with Google Sign-In
- **Brand Attribution** - Clear provider identification (e.g., "Mark VII x Anthropic")
- **Smart Retry** - Re-run prompts with different models
- **Stop Generation** - Cancel responses instantly with red stop button
- **Voice I/O** - Speech recognition input + multilingual text-to-speech
- **PDF Export** - Professional formatting with syntax highlighting
- **Copy & Share** - Easy text extraction and sharing

### 🎨 User Experience

**Material 3 Design:**
- Dynamic theming (Light, Dark, System Default)
- iOS-style color palettes with smooth transitions
- Theme-aware status bar and navigation
- No white flash on startup

**Performance Optimizations:**
- <100ms startup time (24x faster than v1.x)
- Connection pooling for API requests
- Memoized rendering and lazy loading
- Optimized scroll performance

**Interaction Design:**
- Streaming cursor with haptic feedback
- 40dp touch targets for accessibility
- Smooth Lottie animations
- Auto-scroll to latest messages
- Syntax highlighting for code blocks
- Markdown support with inline formatting

**Multilingual Support:**
- MLKit language detection (15+ languages)
- Automatic TTS language switching
- Support for: Chinese, Japanese, Korean, Spanish, French, German, Italian, Portuguese, Russian, Arabic, Hindi, English

### 🛠️ Management Tools

**Python CLI:**
```bash
# Import models from CSV
python update_firebase_models.py --csv models.csv

# List current configuration
python update_firebase_models.py --list

# Interactive mode
python update_firebase_models.py
```

**CSV Bulk Management:**
- Edit 50+ models in Excel/Google Sheets
- Bulk enable/disable toggles
- Easy reordering with sort priority
- Version control friendly
- Automatic validation

---

## Quick Start

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Firebase account (free tier sufficient)
- OpenRouter API key (free tier available at [openrouter.ai/keys](https://openrouter.ai/keys))
- Optional: Google Gemini API key for direct Gemini access

### 1. Clone Repository
```bash
git clone https://github.com/daemon-001/Mark-VII.git
cd Mark-VII
```

### 2. Firebase Setup (~5 minutes)

#### Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" → Enter name: `Mark-VII`
3. Disable Google Analytics (optional) → "Create project"

#### Add Android App
1. Click Android icon in Project Overview
2. Enter package name: `com.daemon.markvii`
3. Download `google-services.json` → Place in `Mark-VII/app/` directory

#### Configure Firestore Database
1. Navigate to **Firestore Database** → "Create database"
2. Select "Start in test mode" → Choose region → "Enable"

#### Add Configuration Data

**Option A: Python Script (Recommended)**
```bash
cd update_models
pip install firebase-admin

# Download service account key:
# Firebase Console → Project Settings → Service Accounts
# → Generate New Private Key → Save as mark-vii-firebase-service-account-key.json

# Import 45+ models from CSV
python update_firebase_models.py --csv models.csv
```

**Option B: Manual Setup**
1. In Firestore, create collection: `app_config`
2. Create document: `models`
3. Add field: `list` (type: array) with model objects:
```json
{
  "id": "anthropic/claude-3.5-sonnet",
  "name": "Claude 3.5 Sonnet",
  "provider": "anthropic",
  "isEnabled": true,
  "sortOrder": 1,
  "apiType": "openrouter"
}
```

4. Create document: `api_keys`
5. Add fields:
   - `openrouterApiKey` (string) - Get from [OpenRouter](https://openrouter.ai/keys)
   - `geminiApiKey` (string, optional) - Get from [Google AI Studio](https://makersuite.google.com/app/apikey)

### 3. Build & Run
```bash
# In Android Studio:
# 1. File → Open → Select Mark-VII folder
# 2. File → Sync Project with Gradle Files
# 3. Build → Make Project (Ctrl+F9)
# 4. Run → Run 'app' (Shift+F10)
```

**First Launch:**
- Grant internet permissions
- Sign in with Google (creates cloud-synced chat sessions)
- Select a model and start chatting!

---

## Installation

### End Users

**Android Smartphone:**
1. Download APK from [Releases](https://github.com/daemon-001/Mark-VII/releases/latest)
2. Enable "Install from Unknown Sources" in Settings → Security
3. Open APK file → Install → Open Mark VII

**Android Emulator (PC):**
1. Download APK from [Releases](https://github.com/daemon-001/Mark-VII/releases/latest)
2. Drag APK into emulator window or use APK installer
3. Launch Mark VII from app drawer

### Developers

See [Quick Start](#quick-start) above for source-based setup.

---

## Python Management Tools

Efficiently manage 45+ AI models using CSV-based workflows.

### Features

- **Bulk Import/Export** - Manage models in Excel/Google Sheets
- **Validation** - Automatic format and field checking
- **Version Control** - Track changes with Git
- **Interactive CLI** - Menu-driven operations
- **Zero Downtime** - Update models without app redeployment

### Usage

```bash
cd update_models

# Import models from CSV (recommended)
python update_firebase_models.py --csv models.csv

# List current models in Firestore
python update_firebase_models.py --list

# Interactive mode
python update_firebase_models.py

# Export models to CSV
python update_firebase_models.py --export my_models.csv
```

### CSV Format
```csv
apiModel,displayName,isAvailable,order
google/gemini-2.0-flash-exp,Gemini 2.0 Flash,TRUE,1
deepseek/deepseek-chat-v3.1,Deepseek Chat V3.1,TRUE,2
anthropic/claude-3-5-sonnet-20241022,Claude 3.5 Sonnet,TRUE,3
```

**Benefits:**
- Edit 50+ models in Excel/Google Sheets
- Bulk enable/disable models
- Easy reordering
- Version control friendly

---

## Documentation

### Setup Guides
- **CHANGELOG.md** - Complete version history and changes
- **Firebase Setup** - See Quick Start section above

### Python Tools
- **update_models/update_firebase_models.py** - Model management script
- **update_models/models.csv** - Sample CSV with 49 models

### Troubleshooting

#### "Firebase not configured" in app
- Check Firestore structure: `app_config/models` document must have `list` field (not `models`)
- Verify `app_config/api_keys` document has `openrouterApiKey` field
- Ensure `google-services.json` is in `app/` folder
- Check `exp_models` collection for exception models

#### HTTP 401 Error (Unauthorized)
- Invalid API key
- Get new key from [OpenRouter](https://openrouter.ai/keys)
- Update in Firebase: `app_config/api_keys/openrouterApiKey`

#### HTTP 404 Error (Model Not Found)
- Model may require ":free" suffix
- App automatically adds failing models to `exp_models` collection
- Retry the request after error - it will use the correct format
- Check model exists on [OpenRouter Models](https://openrouter.ai/models)

#### HTTP 429 Error (Rate Limit)
- Too many requests
- Wait a few seconds and retry
- Consider upgrading OpenRouter plan

#### HTTP 502/503 Error (Server Issues)
- OpenRouter or model provider temporarily down
- Try a different model
- Wait and retry later

#### App Crashing
- Clear app data: Settings → Apps → Mark VII → Clear Data
- Reinstall the app
- Check for updates

#### Build Errors
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug
```

---

## Architecture

### Overview

**Mark VII** uses a cloud-first architecture combining Firebase's real-time configuration management with dual AI API access (OpenRouter + Gemini), enabling zero-downtime model updates and enterprise-grade reliability.

```
┌─────────────────────────────────────────────────────────────┐
│                      Mark VII Android App                   │
├─────────────────────────────────────────────────────────────┤
│  UI Layer (Jetpack Compose + Material 3)                    │
│  ├─ MainActivity.kt         - Main UI orchestration         │
│  ├─ SettingsScreen.kt       - Theme & account management    │
│  └─ DrawerContent.kt        - Navigation & session list     │
├─────────────────────────────────────────────────────────────┤
│  ViewModel Layer (MVVM)                                     │
│  └─ ChatViewModel.kt        - State management + logic      │
├─────────────────────────────────────────────────────────────┤
│  Data Layer                                                 │
│  ├─ ChatData.kt             - OpenRouter API orchestration  │
│  ├─ GeminiClient.kt         - Direct Gemini API client      │
│  ├─ FirebaseConfigManager   - Remote model configuration    │
│  ├─ ChatHistoryManager      - Session persistence (Cloud)   │
│  ├─ AuthManager             - Google Sign-In + tokens       │
│  └─ ThemePreferences        - Local theme storage           │
├─────────────────────────────────────────────────────────────┤
│  Network Layer                                              │
│  ├─ Retrofit + OkHttp       - HTTP client with pooling      │
│  ├─ SSE EventSource         - Streaming response parser     │
│  └─ Connection Pool         - Persistent connections        │
└─────────────────────────────────────────────────────────────┘
              ↓                              ↓
    ┌───────────────────┐        ┌──────────────────────┐
    │ Firebase Services │        │   AI API Providers   │
    ├───────────────────┤        ├──────────────────────┤
    │ Firestore         │        │ OpenRouter API       │
    │ Authentication    │        │ (100+ models)        │
    │ Analytics         │        │ + Direct Gemini API  │
    └───────────────────┘        └──────────────────────┘
```

### Tech Stack

**Frontend:**
- Kotlin with Jetpack Compose for declarative UI
- Material 3 Design System with dynamic theming
- Lottie for animations, Markdown with syntax highlighting
- StateFlow for reactive state management

**Architecture Pattern:**
- MVVM (Model-View-ViewModel) with repository pattern
- Coroutines + Flow for asynchronous operations
- Dependency injection via constructor parameters

**Backend Integration:**
- Firebase Firestore: Cloud configuration + chat history
- Firebase Authentication: Google Sign-In with OAuth
- Firebase Analytics: Usage tracking (optional)

**Networking:**
- Retrofit 2.11.0 with OkHttp 4.12.0
- Server-Sent Events (SSE) for streaming responses
- Connection pooling for <100ms startup time

**AI Providers:**
- OpenRouter API: 100+ models with unified interface
- Direct Gemini API: Native Google integration with vision
- MLKit Language ID: Automatic TTS language detection

**Tools:**
- Python 3.11+ with Firebase Admin SDK
- CSV-based model management workflow
- Git for version control

### Project Structure
```
Mark-VII/
├── app/
│   ├── google-services.json              # Firebase config (download from console)
│   └── src/main/java/com/daemon/markvii/
│       ├── MainActivity.kt                # App entry + Firebase initialization
│       ├── ChatViewModel.kt               # MVVM state management
│       ├── data/
│       │   ├── Chat.kt                    # Data models (Message, ChatRequest, etc.)
│       │   ├── ChatData.kt                # OpenRouter API orchestration
│       │   ├── GeminiClient.kt            # Direct Gemini API client
│       │   ├── OpenRouterApi.kt           # Retrofit API interface
│       │   ├── FirebaseConfig.kt          # Model configuration models
│       │   ├── FirebaseConfigManager.kt   # Firestore config operations
│       │   ├── AuthManager.kt             # Google Sign-In + token management
│       │   ├── ChatHistoryManager.kt      # Cloud chat session storage
│       │   ├── ThemePreferences.kt        # Local theme persistence
│       │   └── Keys.kt                    # App metadata + build info
│       ├── ui/theme/
│       │   ├── Theme.kt                   # Material 3 theme + AppColors
│       │   └── Color.kt                   # Theme color definitions
│       ├── SettingsScreen.kt              # Theme selector + account UI
│       ├── DrawerContent.kt               # Navigation + session management
│       └── utils/
│           └── PdfGenerator.kt            # PDF export with syntax highlighting
├── update_models/
│   ├── update_firebase_models.py          # Model management CLI
│   ├── models.csv                         # Pre-configured 49 models
│   └── mark-vii-firebase-service-account-key.json  # Service account (download)
├── CHANGELOG.md                           # Git commit-based version history
├── FIREBASE_SETUP.md                      # Detailed Firebase setup guide
└── README.md                              # Project documentation
```

---

## Usage Examples

### Basic Chat Workflow
1. **Launch** - Open Mark VII (cold start <100ms)
2. **Sign In** - Tap profile icon → "Sign in with Google" (optional, enables cloud sync)
3. **Select Model** - Tap dropdown → Choose model (e.g., "Claude 3.5 Sonnet")
4. **Input** - Type message or tap microphone icon for voice input
5. **Send** - Tap send button (▲) → Get real-time streaming response
6. **Attribution** - See response header "Mark VII x Anthropic"
7. **Stop** - Tap red stop button (■) to cancel streaming anytime

### Switching Between API Providers
1. Open model dropdown
2. Select different provider mid-conversation to compare responses
3. Chat history preserves which API was used per message

### Theme Customization
1. Open drawer (swipe right or tap menu) → Tap Settings icon (⚙️)
2. Under "Appearance", tap Theme selector
3. Choose theme:
   - **System Default** - Follows device settings (auto light/dark)
   - **Light** - iOS-inspired bright theme with gentle shadows
   - **Dark** - Eye-friendly with OLED-optimized blacks
4. Theme applies instantly without restart, status bar updates automatically

### Managing Chat Sessions
1. Open navigation drawer (swipe right or hamburger menu)
2. View all sessions with preview of last message
3. **Switch** - Tap session to load conversation
4. **Rename** - Long-press → "Rename" → Enter new name
5. **Delete** - Long-press → "Delete" → Confirm
6. **New Chat** - Tap "+" button → Fresh session created
7. **Cloud Sync** - All sessions backup to Firebase when signed in

### Image Understanding (Vision Models)
1. Tap plus icon (+) in input field
2. Select image from gallery or take photo
3. Type question about image (e.g., "What's in this image?")
4. Works with vision-capable models: Gemini 2.0 Flash, Claude 3.5, GPT-4o
5. Image preview shows pin indicator when attached
6. Send to get multi-modal analysis

### Multilingual Text-to-Speech
1. Receive AI response in any language (Chinese, Spanish, Arabic, etc.)
2. Tap speaker icon on message
3. MLKit automatically detects language
4. TTS reads response in correct language/accent
5. Supports 15+ languages: Chinese (Mandarin), Japanese, Korean, Spanish, French, German, Italian, Portuguese, Russian, Arabic, Hindi, English, Dutch, Polish, Turkish

### PDF Export
1. Complete conversation with AI model
2. Tap 3-dot menu → "Export to PDF"
3. PDF generates with:
   - Syntax-highlighted code blocks
   - Formatted Markdown rendering
   - Message timestamps
   - Model attribution (e.g., "Mark VII x Anthropic")
4. Share via any app or save to storage
5. Professional formatting for documentation/reports

### Retry with Different Models
1. Send message to Model A (e.g., Claude 3.5 Sonnet)
2. Get response → Not satisfied with output
3. Tap model dropdown → Switch to Model B (e.g., GPT-4o)
4. Tap "Retry Last Prompt" button
5. Get alternative response from different AI
6. Compare responses side-by-side in chat history

### Voice Input
1. Tap microphone icon in input field
2. Grant microphone permission (first time)
3. Speak your question naturally
4. Speech-to-text transcription appears automatically
5. Edit if needed, then send
6. Works in all languages supported by Android Speech Recognition

---

## Performance Metrics

### Startup Performance
- **Cold Start:** <100ms (24x faster than v1.x)
- **Model Loading:** <50ms (cached configuration)
- **Theme Application:** <10ms (instant visual feedback)
- **Firebase Init:** Asynchronous, non-blocking

### Runtime Optimization
- **Streaming Latency:** <500ms to first token
- **Rendering:** 60 FPS with memoized composables
- **Memory:** <50MB baseline, <150MB during streaming
- **Network:** Connection pooling reduces request overhead by 70%
- **TTS Language Detection:** <100ms with MLKit on-device processing

*Note: Times vary based on network, server load, and prompt complexity.*

---

## Contributing

We welcome contributions from the community! Whether you're fixing bugs, adding features, or improving documentation, your help makes Mark VII better.

### How to Contribute

**Reporting Issues:**
1. [Open an issue](https://github.com/daemon-001/Mark-VII/issues/new)
2. Add screenshots if helpful

**Suggesting Features:**
1. Open a [Feature Request](https://github.com/daemon-001/Mark-VII/issues/new?template=feature_request.md)
2. Describe the feature and its use case
3. Provide mockups/examples if relevant

### Feature Requests
- Open an issue with tag `enhancement`
- Describe the feature and use case
- Discuss with maintainers before implementing

### Pull Requests
1. Fork the repository
2. Create feature branch: `git checkout -b feature-name`
3. Make changes and test thoroughly
4. Commit: `git commit -m "Add: feature description"`
5. Push: `git push origin feature-name`
4. Explain why it would benefit users
5. Provide mockups/examples if relevant

**Code Contributions:**
1. Fork the repository
2. Create feature branch: `git checkout -b feature/your-feature-name`
3. Make changes following code style guidelines (see below)
4. Test thoroughly on multiple devices/Android versions
5. Commit with clear messages: `git commit -m "Add: Feature description"`
6. Push to your fork: `git push origin feature/your-feature-name`
7. Open Pull Request with detailed description
---

## Dependencies

### Core Libraries
```gradle
// Firebase (BOM manages versions)
firebase-bom:33.7.0
firebase-firestore-ktx          // Cloud configuration + chat history
firebase-analytics-ktx          // Usage analytics
firebase-auth-ktx               // Google Sign-In authentication

// Google Services
play-services-auth:21.3.0       // Google Sign-In UI
mlkit-language-id:17.0.6        // Multilingual TTS detection

// Networking
retrofit:2.11.0                 // Type-safe HTTP client
okhttp:4.12.0                   // Connection pooling + SSE
gson:2.10.1                     // JSON serialization

// PDF Generation
itext7-core:7.2.5               // PDF document creation
html2pdf:4.0.5                  // HTML to PDF conversion

// Markdown & Syntax Highlighting
compose-markdown:0.5.4          // Rich text rendering
code-highlight:2.0.0            // Syntax highlighting for code blocks

// UI & Animation
androidx.compose.bom:2024.12.01 // Jetpack Compose
androidx.material3:*            // Material 3 components
lottie-compose:6.0.0            // Lottie animations
coil-compose:2.4.0              // Image loading
```

### Minimum Requirements
- **Android SDK:** 24 (Android 7.0 Nougat) or higher
- **Target SDK:** 35 (Android 15)
- **Kotlin:** 2.1.0
- **Gradle:** 8.7
- **JDK:** 17+

---

## Performance Comparison

### Version 3.0 vs Previous Releases

| Metric                | v1.x (Gemini)      | v2.x (OpenRouter)  | v3.0 (Dual API)    | Improvement      |
|-----------------------|--------------------|--------------------|--------------------|--------------------|
| **Startup Time**      | ~2.65s             | ~110ms             | <100ms             | **26.5x faster**   |
| **Models Available**  | 5-10               | 100+               | 100+ (dual APIs)   | **10-20x more**    |
| **API Providers**     | 1 (Google)         | Multiple           | OpenRouter + Gemini| **Direct + unified**|
| **Configuration**     | Hardcoded          | Cloud-based        | Cloud + real-time  | **Instant updates**|
| **Streaming**         | No                 | Yes (SSE)          | Yes (both APIs)    | **Real-time**      |
| **Error Handling**    | Basic              | Comprehensive      | Auto-retry + 404 fix| **Resilient**     |
| **TTS Languages**     | 1-2                | 1-2                | 15+ (MLKit)        | **7x more**        |
| **Theme Support**     | Basic              | Light/Dark         | L/D/System + iOS-style | **Polished**   |
| **Offline Support**   | No                 | Limited            | Cached config      | **Works offline**  |
| **Stop Generation**   | No                 | Yes                | Yes + haptics      | **User control**   |

---

---

## Security & Privacy

### Data Protection
- **Encrypted Storage:** API keys secured in Firebase, never in source code or APK
- **HTTPS Only:** All API communication uses TLS 1.3 encryption
- **OAuth 2.0:** Google Sign-In with secure token management
- **Local Storage:** Chat history cached locally, synced to Firebase when signed in
- **No Tracking:** Zero third-party analytics or ad networks
- **Firestore Rules:** Read/write access restricted to authenticated users

### Configuration Security
- `google-services.json` excluded from Git via `.gitignore`
- `mark-vii-firebase-service-account-key.json` excluded (Python tool only)
- Firebase project rules restrict access to authenticated users
- API keys rotatable without app updates (cloud configuration)

### User Control
- **Sign Out Anytime** - Settings → Sign Out → Clears local cache
- **Delete Chat Sessions** - Long-press session → Delete (removes from cloud)
- **Theme Preferences** - Stored locally, never synced
- **Optional Cloud Sync** - Chat history syncs only when signed in with Google
- **Offline Mode** - Works with cached configuration when no internet

### Best Practices
- Never commit `google-services.json` or service account keys to public repos
- Rotate Firebase API keys periodically in Firebase Console
- Use Firebase Authentication rules to restrict Firestore access
- Enable Firebase App Check for production builds to prevent API abuse

---

## Supported Models

**Dual API Architecture:**  
Mark VII integrates both **OpenRouter** and **Gemini API** services, providing flexible access to multiple AI providers through a unified interface.

**OpenRouter Integration:**
- Direct connection to OpenRouter's model ecosystem
- Automatically fetches available models from OpenRouter API
- 45+ models from leading AI providers
- Real-time model catalog synchronization
- Unified API for multiple providers (Anthropic, OpenAI, Meta, Mistral, Deepseek, etc.)

**Gemini API Integration:**
- Direct Google Gemini API support (separate from OpenRouter)
- Dedicated Gemini model access
- Vision capabilities for image understanding
- Toggle between OpenRouter and Gemini in-app

**Firebase Cloud Configuration:**
- Models managed in Firestore for instant updates
- No app rebuild required to add/remove models
- Changes reflect on app restart

**Supported Companies & Providers:**
- **Google** - Gemini 2.0/2.5 Flash, Gemini Pro, Gemma 2/3 variants (via both OpenRouter and direct Gemini API)
- **Deepseek** - Deepseek Chat V3.1, R1, R1 Distill Llama/Qwen, R1 Zero
- **Meta** - Llama 3.1 (8B/70B/405B), Llama 3.3 70B, Llama 4 Maverick/Scout
- **Qwen (Alibaba)** - Qwen2.5 (7B/72B), Qwen3 Coder (8B/32B), Qwen3 235B
- **Mistral AI** - Mistral Large, Medium, Small, 7B, Nemo
- **xAI** - Grok Beta, Grok Vision Beta
- **Anthropic** - Claude 3.5 Sonnet, Claude 3 Opus/Haiku
- **OpenAI** - GPT-4 Turbo, GPT-4, GPT-4o, GPT-3.5 Turbo
- **30+ additional AI companies** - Cohere, AI21 Labs, Perplexity AI, and many more through OpenRouter

*Complete model list with specific variants available in `update_models/models.csv`*


**Management Tools:**
- **API Toggle:** Switch between OpenRouter and Gemini API in Settings
- **Firebase Console:** Add/remove models directly in Firestore
- **CSV Bulk Import:** Edit `update_models/models.csv` and run Python script
- **Python CLI:** Interactive model management with `update_firebase_models.py`
- **No App Updates Required:** Changes reflect on next app restart

**Exception Models:**  
Some OpenRouter models require `:free` suffix for free-tier access. The app automatically detects 404 errors, adds models to `exp_models` collection, and retries with correct format.

**Data Structure:**  
Models stored in Firebase at `app_config/models/list` (array) with fields:
- `apiModel` - OpenRouter/Gemini model identifier
- `displayName` - User-friendly name shown in app
- `isAvailable` - Enable/disable toggle
- `order` - Sort position in dropdown

**API Keys:**  
Stored in Firebase at `app_config/api_keys`:
- `openrouterApiKey` - Required for OpenRouter models
- `geminiApiKey` - Optional for direct Gemini API access

**Quick Setup:**
```bash
cd update_models
python update_firebase_models.py --csv models.csv
```

See `update_models/models.csv` for complete pre-configured model list with 45+ entries.

---

## Screenshots

### Main Chat Interface
![Mark VII Chat](https://github.com/user-attachments/assets/5ef5e209-fb29-47e3-b8b1-0bd9999a3ea9)


---

## Download

### Official Releases

**Latest Stable Version:**
- [GitHub Releases](https://github.com/daemon-001/Mark-VII/releases/latest) - v1.1
- Release Date: Dec 10, 2024
- Changelog: See [CHANGELOG.md](CHANGELOG.md)

**Development Builds:**
- Available in [Pre-releases](https://github.com/daemon-001/Mark-VII/releases)
- Includes experimental features and beta testing

### System Requirements
- **Minimum:** Android 7.0 Nougat (API 24)
- **Target:** Android 15 (API 35)
- **Recommended:** Android 10+ for best performance
- **Storage:** ~50MB app + ~100MB cache
- **Internet:** Required for AI model access (offline config supported)

---


See [LICENSE](LICENSE) file for full terms.

---

## Support & Contact

### Get Help

**Bug Reports:**
- [GitHub Issues](https://github.com/daemon-001/Mark-VII/issues/new?template=bug_report.md)
- Include: Android version, app version, steps to reproduce

**Feature Requests:**
- [Feature Request Form](https://github.com/daemon-001/Mark-VII/issues/new?template=feature_request.md)
- Describe use case and expected behavior

**Documentation:**
- [CHANGELOG.md](CHANGELOG.md) - Version history with git timestamps
- [FIREBASE_SETUP.md](FIREBASE_SETUP.md) - Detailed Firebase configuration
- [README.md](README.md) - Comprehensive project documentation

**Email Support:**
- Developer: nitesh.kumar4work@gmail.com
- Response time: 1-48 hours

### Connect

**Developer:**
- **Name:** Nitesh Kumar
- **GitHub:** [@daemon-001](https://github.com/daemon-001)
- **LinkedIn:** [daemon001](https://www.linkedin.com/in/daemon001)

**Project:**
- **Repository:** [Mark-VII](https://github.com/daemon-001/Mark-VII)
- **Stars:** Give us a ⭐ if you find this useful!
- **Forks:** Welcome - see [Contributing](#contributing) section

---

## Acknowledgments

### Technologies
- **OpenRouter** - Unified API for 50+ AI models
- **Google Firebase** - Cloud infrastructure and authentication
- **Google MLKit** - On-device language identification
- **Jetpack Compose** - Modern Android UI toolkit
- **Material 3** - Google's design system

### Open Source Libraries
- Retrofit & OkHttp by Square
- Lottie by Airbnb
- iText PDF by iText Software
- And all contributors to our dependencies

### Community
- Thanks to all contributors, testers, and users
- Special thanks to early adopters who provided feedback
- Inspired by the need for flexible, multi-provider AI access

---

## Show Your Support

If Mark VII helps you, please consider:
- ⭐ **Star this repository** on GitHub
- 🐛 **Report bugs** to help improve quality
- 💡 **Suggest features** for future releases
- 🔧 **Contribute code** via pull requests
- 📢 **Share** with developers and AI enthusiasts
- 📝 **Write reviews** on GitHub or social media

Every contribution, no matter how small, helps make Mark VII better for everyone!

---

<div align="center">

**Built with ❤️ by Nitesh**

[⬆ Back to Top](#mark-vii)

</div>

[Home](https://github.com/daemon-001/Mark-VII) • [Download](https://github.com/daemon-001/Mark-VII/releases) • [Report Bug](https://github.com/daemon-001/Mark-VII/issues) • [Request Feature](https://github.com/daemon-001/Mark-VII/issues)

*Enjoy your advanced multi-provider AI chatbot experience!*

</div>
