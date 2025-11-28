# Mark VII

<div align="center">

![Version](https://img.shields.io/badge/version-3.0.0-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-orange.svg)

**Multi-provider AI chat platform with 45+ models, cloud configuration, and modern Material 3 design.**

[Features](#features) • [Quick Start](#quick-start) • [Download](#download)

</div>

---

## Overview

Mark VII provides unified access to 45+ AI models from Anthropic, OpenAI, Meta, Deepseek, Mistral, Google, and more through a single Android app powered by OpenRouter and Firebase.

**Key Highlights:**
- 45+ AI models from multiple providers
- Cloud-based configuration via Firebase
- Dual API support (OpenRouter + Gemini)
- Dynamic theming (Light, Dark, System)
- Google Sign-In with chat history sync
- PDF export and session management
- Real-time streaming responses
- Voice input and text-to-speech

---

## Features

### AI Models
Access to Claude 3.5, GPT-4, Llama 3.3/4, Deepseek R1, Mistral, Gemini 2.0, Qwen3, and 40+ more models from leading AI providers.

### Cloud Configuration
Manage models and API keys remotely through Firebase. Updates reflect instantly on app restart without requiring new builds.

### Authentication & Storage
Google Sign-In with Firebase Authentication. Chat sessions sync across devices with Firestore integration.

### User Experience
- Material 3 design with dynamic theming
- Streaming responses with animated cursor
- Voice input and text-to-speech
- Markdown rendering with syntax highlighting
- PDF export with professional formatting
- Session management (create, rename, delete)
- Real-time model switching
- Stop generation anytime

### Performance
- Startup: <100ms (24x faster than v1.x)
- Connection pooling for faster API responses
- Memoized rendering for smooth UI
- Optimized lazy loading

---

## Quick Start

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+
- Firebase account (free)
- OpenRouter API key (free tier available)

### Setup Steps

**1. Clone Repository**
```bash
git clone https://github.com/daemon-001/Mark-VII.git
cd Mark-VII
```

**2. Firebase Configuration**

Create project at [Firebase Console](https://console.firebase.google.com/):
1. Add project: `Mark-VII`
2. Add Android app: `com.daemon.markvii`
3. Download `google-services.json` → place in `app/` folder
4. Enable Firestore Database (test mode)

**3. Firestore Data Structure**

Collection: `app_config`
- Document: `models`
  - Field: `list` (array of model objects)
- Document: `api_keys`
  - Field: `openrouterApiKey` (string)
  - Field: `geminiApiKey` (string, optional)

**Quick setup with Python:**
```bash
cd update_models
pip install firebase-admin
python update_firebase_models.py --csv models.csv
```

**4. API Keys**

Get OpenRouter key from [openrouter.ai/keys](https://openrouter.ai/keys) and add to Firebase `app_config/api_keys/openrouterApiKey`.

**5. Build**
```bash
./gradlew assembleDebug
```

---

## Installation

### Users (APK)
Download from [Releases](https://github.com/daemon-001/Mark-VII/releases/latest), enable "Unknown Sources", and install.

### Developers (Source)
Follow Quick Start steps above.

---

## Usage

### Basic Chat
1. Select model from dropdown
2. Type message or use voice input
3. Get streaming AI response
4. Tap stop button to cancel anytime

### Theme Management
Open drawer → Settings → Theme → Select Light/Dark/System Default

### Chat Sessions
Create, rename, delete sessions from navigation drawer. All sessions sync with Google account.

### PDF Export
Tap menu → Export/Share PDF to save conversations with formatting.

---

## Management Tools

### CSV Model Management
```bash
cd update_models

# Import models
python update_firebase_models.py --csv models.csv

# List current models
python update_firebase_models.py --list
```

**CSV Format:**
```csv
apiModel,displayName,isAvailable,order
google/gemini-2.0-flash-exp,Gemini 2.0 Flash,TRUE,1
deepseek/deepseek-r1,Deepseek R1,TRUE,2
```

Edit 50+ models in Excel/Sheets, bulk enable/disable, easy reordering.

---

## Troubleshooting

**Firebase not configured**
- Verify Firestore: `app_config/models` has `list` field
- Check `app_config/api_keys` has `openrouterApiKey`
- Ensure `google-services.json` in `app/` folder

**HTTP 401 (Unauthorized)**
- Get new key from [openrouter.ai/keys](https://openrouter.ai/keys)
- Update Firebase: `app_config/api_keys/openrouterApiKey`

**HTTP 404 (Model Not Found)**
- Model may need `:free` suffix
- App auto-adds to `exp_models` and retries
- Check model exists on [OpenRouter](https://openrouter.ai/models)

**HTTP 429 (Rate Limit)**
- Wait and retry
- Consider upgrading OpenRouter plan

**Build Errors**
```bash
./gradlew clean
./gradlew assembleDebug
```

---

## Architecture

**Stack:**
- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Pattern: MVVM
- Backend: Firebase (Firestore, Auth, Analytics)
- Networking: Retrofit + OkHttp with SSE streaming
- Async: Coroutines + StateFlow
- Rendering: Markdown with syntax highlighting

**Project Structure:**
```
app/src/main/java/com/daemon/markvii/
├── MainActivity.kt               # Main UI
├── ChatViewModel.kt              # State management
├── SettingsScreen.kt             # Settings UI
├── DrawerContent.kt              # Navigation
├── data/
│   ├── ChatData.kt               # API logic
│   ├── OpenRouterApi.kt          # API client
│   ├── FirebaseConfigManager.kt  # Config management
│   ├── AuthManager.kt            # Authentication
│   ├── ChatHistoryManager.kt     # Session storage
│   └── ThemePreferences.kt       # Theme state
├── ui/theme/
│   ├── Theme.kt                  # Theme system
│   └── Color.kt                  # Color definitions
└── utils/
    └── PdfGenerator.kt           # PDF export

update_models/
├── update_firebase_models.py     # Model management
└── models.csv                    # 49 pre-configured models
```

---

## Performance

**v3.0 vs v1.x:**

| Metric | v1.x | v3.0 | Improvement |
|--------|------|------|-------------|
| Startup | 2.65s | <100ms | 24x faster |
| Models | 5-10 | 45+ | 9x more |
| Cost/Launch | $0.001 | $0.00 | 100% savings |
| Configuration | Hardcoded | Cloud | Instant updates |
| Streaming | No | Yes | Real-time |

---

## Dependencies

**Core:**
```gradle
// Firebase
firebase-bom:33.7.0
firebase-firestore-ktx
firebase-auth-ktx

// Networking
retrofit:2.9.0
okhttp:4.12.0

// PDF
itext7-core:7.2.5
html2pdf:4.0.5

// UI
compose-markdown:0.5.4
coil-compose:2.4.0
lottie-compose:6.6.0
```

---

## Security

- API keys stored in Firebase (not code)
- HTTPS encryption for all calls
- Google Sign-In for authentication
- No third-party data sharing
- Local theme preferences
- Optional cloud sync for chat history

---

## Supported Models

50+ models including:
- Google: Gemini 2.0 Flash, Gemma 3 variants
- Deepseek: Chat V3.1, R1, R1 Distill
- Meta: Llama 3.3, 4 Maverick/Scout
- Qwen: Qwen3 Coder, Qwen 2.5 variants
- Mistral: Large, Small, Medium
- Anthropic: Claude 3.5 Sonnet
- OpenAI: GPT-4 Turbo, GPT-4

See `update_models/models.csv` for complete list.

---

## Download

**Latest Release:** [v3.0.0](https://github.com/daemon-001/Mark-VII/releases/latest)

**Requirements:**
- Android 7.0+ (API 24)
- Target: Android 14 (API 34)

---

## Contributing

**Report Issues:** [GitHub Issues](https://github.com/daemon-001/Mark-VII/issues)  
**Pull Requests:** Fork → Feature branch → Test → PR with description  
**Code Style:** Follow Kotlin conventions, meaningful names, focused functions

---

## Support

**Developer:** Nitesh (@daemon-001)  
**Email:** nitesh.kumar4work@gmail.com  
**GitHub:** [daemon-001](https://github.com/daemon-001)  
**LinkedIn:** [daemon001](https://www.linkedin.com/in/daemon001)

---

## License

MIT License - see [LICENSE](LICENSE) file.

---

<div align="center">

**Made by Nitesh**

*Advanced multi-provider AI chat for Android*

</div>
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-orange.svg)

**A professional Android AI chatbot with access to 45+ AI models from multiple providers through cloud-based configuration.**

[Features](#features) • [Installation](#installation) • [Quick Start](#quick-start) • [Documentation](#documentation) • [Download](#download)

</div>

---

## Overview

**Mark VII** is a next-generation AI-powered Android chatbot that provides access to multiple state-of-the-art AI models from various providers including **Anthropic (Claude)**, **OpenAI (GPT)**, **Meta (Llama)**, **Deepseek**, **Mistral**, and many more - all through a single, unified interface powered by **OpenRouter API**.

### What Makes Mark VII Unique?

- 🤖 **45+ AI Models** - Access to Claude, GPT, Llama, Deepseek, Mistral, and more
- 🔥 **Cloud Configuration** - Manage models and API keys remotely via Firebase
- ⚡ **Lightning Fast** - Instant startup with no API calls (24x faster than v1.x)
- 🎨 **Brand Attribution** - See which AI provider answered each question
- 📱 **Modern UI** - Built with Jetpack Compose and Material 3
- 🛡️ **Robust Error Handling** - Clear messages for all error scenarios
- 🐍 **Easy Management** - Python tools for bulk model updates via CSV

---

## Features

### Multi-Provider AI Access
- **Anthropic:** Claude 3.5 Sonnet, Claude 3 Opus, Claude 3 Haiku
- **OpenAI:** GPT-4 Turbo, GPT-4, GPT-3.5 Turbo
- **Meta:** Llama 3.3, Llama 3.1 70B/8B, Llama 4 Maverick/Scout
- **Deepseek:** Deepseek Chat V3.1, Deepseek R1, R1 Distill
- **Mistral:** Mistral Large, Small, Medium, Nemo
- **Google:** Gemini 2.0 Flash, Gemma 3 variants (via OpenRouter and direct Gemini API)
- **Qwen:** Qwen3 Coder, Qwen3 235B, Qwen 2.5 variants
- **And 40+ more models!**

### Firebase Cloud Integration
- **Remote Model Management** - Add/remove models without app updates
- **Dynamic API Keys** - Update credentials remotely
- **Instant Updates** - Changes reflect immediately on app restart
- **Secure Storage** - API keys stored in Firebase, not in code
- **Automatic Fallback** - Works offline with cached configuration
- **Exception Model Handling** - Automatically manages models requiring ":free" suffix
- **404 Auto-Recovery** - Smart error handling with retry suggestions

### Chat Features
- **Dual API Support** - Switch between OpenRouter and direct Gemini API
- **Text Conversations** - Chat with any AI model
- **Image Understanding** - Upload images and ask questions (vision models)
- **Model Switching** - Switch between models and API providers mid-conversation
- **Chat History** - Review and manage previous conversations with Google Sign-In
- **Session Management** - Create, rename, and delete chat sessions
- **Brand Display** - See which AI (e.g., "Mark VII x Deepseek") answered
- **Voice Input** - Speak your prompts using voice recognition
- **Text-to-Speech** - Listen to AI responses
- **Copy & Share** - Easy text copying and sharing of responses
- **PDF Export** - Save and share conversations as formatted PDFs
- **Retry with Different Models** - Re-run prompts with alternate AI models or providers

### User Experience
- **Instant Startup** - Welcome guide loads in <10ms
- **Dynamic Theme Support** - Light, Dark, and System Default themes
- **Material 3 Design** - Modern, beautiful interface with theme-aware colors
- **Smooth Animations** - Lottie-powered animations and streaming cursor effects
- **Text Selection** - Easy copy/paste support for all messages
- **Syntax Highlighting** - Beautiful code rendering with language-specific colors
- **Markdown Support** - Rich text formatting with inline code and code blocks
- **Image Attachment UI** - Clean preview of attached images with pin indicator
- **Streaming Responses** - Real-time text streaming with animated cursor
- **Haptic Feedback** - Tactile feedback during streaming responses
- **Stop Generation** - Red stop button to cancel streaming responses anytime
- **Smart Model Selection** - Dropdown with model switching mid-conversation
- **Auto-Scroll** - Automatic scrolling to latest message
- **No White Flash** - Smooth transitions with theme-appropriate backgrounds
- **PDF Export** - Save and share conversations as professionally formatted PDFs
- **Google Sign-In** - Secure authentication with Firebase
- **Chat Session Management** - Create, rename, and delete chat sessions
- **Settings Screen** - Easy access to theme selection and account management

### Management Tools
- **CSV Import/Export** - Manage models in spreadsheets
- **Bulk Updates** - Update 50+ models at once
- **Interactive CLI** - Menu-driven Python script
- **Validation** - Automatic format checking

---

## Quick Start

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24 (Android 7.0) or higher
- Firebase account (free)
- OpenRouter API key (free tier available)

### 1. Clone Repository
```bash
git clone https://github.com/daemon-001/Mark-VII.git
cd Mark-VII
```

### 2. Firebase Setup (5 minutes)

#### A. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name: `Mark-VII`
4. Disable Google Analytics (optional)
5. Click "Create project"

#### B. Add Android App
1. Click Android icon
2. Package name: `com.daemon.markvii`
3. Download `google-services.json`
4. Place in `Mark-VII/app/` folder

#### C. Enable Firestore
1. In Firebase Console, go to **Firestore Database**
2. Click "Create database"
3. Select "Start in test mode"
4. Click "Enable"

#### D. Add Data to Firestore
1. In Firestore, create collection: `app_config`
2. Create document: `models`
3. Add field: `list` (type: array)
4. Use the provided CSV or Python script to populate models

**Quick Option - Use Python Script:**
```bash
cd update_models
pip install firebase-admin
# Download Firebase service account key from Firebase Console
# Save as: mark-vii-firebase-service-account-key.json
python update_firebase_models.py --csv models.csv
```

5. Create document: `api_keys`
6. Add field: `openrouterApiKey` (type: string)
7. Get your API key from [OpenRouter](https://openrouter.ai/keys)
8. Paste the key value

### 3. Build & Run
```bash
# In Android Studio
1. File → Sync Project with Gradle Files
2. Build → Build Bundle(s) / APK(s) → Build APK
3. Run → Run 'app'
```

---

## Installation

### For Users (APK)

#### Android Smartphone:
1. Download APK from [Releases](https://github.com/daemon-001/Mark-VII/releases/latest)
2. Enable "Install from Unknown Sources" in Settings
3. Install the APK
4. Open Mark VII
5. Select a model and start chatting!

#### PC Android Emulator:
1. Download APK from [Releases](https://github.com/daemon-001/Mark-VII/releases/latest)
2. Open emulator's APK installer
3. Select the downloaded APK
4. Launch Mark VII from emulator

### For Developers (Source)
See [Quick Start](#quick-start) above.

---

## Python Management Tools

### Bulk Model Management with CSV

Update multiple models easily using CSV files:

```bash
cd update_models

# Import models from CSV
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

### Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material 3
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend:** Firebase Firestore
- **API Client:** Retrofit + OkHttp (with SSE streaming support)
- **AI Provider:** OpenRouter (unified API)
- **Async:** Kotlin Coroutines + StateFlow
- **Animations:** Lottie, Compose Animations
- **Text Rendering:** Markdown with syntax highlighting
- **Voice:** Android Speech Recognition & Text-to-Speech

### Project Structure
```
Mark-VII/
├── app/
│   ├── google-services.json              # Firebase config (download)
│   └── src/main/java/com/daemon/markvii/
│       ├── MainActivity.kt                # Main UI + Firebase init
│       ├── ChatViewModel.kt               # Chat logic + state
│       ├── data/
│       │   ├── Chat.kt                    # Data models
│       │   ├── ChatData.kt                # API calls + logic
│       │   ├── OpenRouterApi.kt           # API client
│       │   ├── FirebaseConfig.kt          # Firebase models
│       │   ├── FirebaseConfigManager.kt   # Firebase operations
│       │   ├── AuthManager.kt             # Google Sign-In
│       │   ├── ChatHistoryManager.kt      # Chat session storage
│       │   ├── ThemePreferences.kt        # Theme management
│       │   ├── GeminiClient.kt            # Gemini API integration
│       │   └── Keys.kt                    # App metadata
│       ├── ui/theme/
│       │   ├── Theme.kt                   # Material 3 theme + AppColors
│       │   └── Color.kt                   # Theme color definitions
│       ├── SettingsScreen.kt              # Settings UI
│       ├── DrawerContent.kt               # Navigation drawer
│       └── utils/
│           └── PdfGenerator.kt            # PDF export
├── update_models/
│   ├── update_firebase_models.py          # Model management
│   ├── models.csv                         # 49 pre-configured models
│   └── mark-vii-firebase-service-account-key.json  # Firebase admin key
├── CHANGELOG.md                           # Version history
└── README.md                              # This file
```

---

## Usage Examples

### Basic Chat
1. Open Mark VII
2. Sign in with Google (optional, for chat history sync)
3. Select a model from dropdown (e.g., "Deepseek Chat V3.1")
4. Type your message or use voice input (microphone icon)
5. Tap send (▲) to get instant streaming AI response
6. See response with "Mark VII x Deepseek" header
7. Tap the red stop button (■) to cancel streaming anytime

### Using Themes
1. Tap the profile icon or hamburger menu to open drawer
2. Tap the Settings icon (gear)
3. Under "Appearance", tap the Theme selector
4. Choose from:
   - **System Default** - Follows device theme
   - **Light** - Clean, bright interface
   - **Dark** - Eye-friendly dark mode
5. Theme changes apply instantly without restarting

### Managing Chat Sessions
1. Open the navigation drawer (swipe right or tap menu)
2. View all your chat sessions
3. Tap a session to switch to it
4. Long-press a session to rename or delete
5. Tap "New Chat" to start a fresh conversation
6. All sessions sync with your Google account

### Image Understanding
1. Tap camera icon (📷)
2. Select an image
3. Type a question about the image
4. Get AI analysis (works with vision-capable models)

### Comparing Models
1. Ask Claude 3.5 Sonnet a question
2. See streaming response with animated cursor
3. See response with "Mark VII x Anthropic" brand header
4. Switch to GPT-4 Turbo from the model dropdown
5. Ask the same question
6. See response with "Mark VII x Openai"
7. Compare the different approaches!
8. Use retry button to re-run prompts with different models

### Exporting Conversations
1. Tap the three-dot menu (⋮) in the top right
2. Select "Export as PDF" to save
3. Or select "Share PDF" to send via email/messaging
4. PDFs include proper formatting, syntax highlighting, and page margins
5. Great for archiving important conversations or sharing insights

---

## Contributing

We welcome contributions! Here's how:

### Reporting Issues
- Use [GitHub Issues](https://github.com/daemon-001/Mark-VII/issues)
- Include Android version, app version, and steps to reproduce
- Screenshots/logs are helpful

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
6. Open Pull Request with description

### Code Style
- Follow Kotlin conventions
- Use meaningful variable names
- Add comments for complex logic
- Keep functions small and focused

---

## Performance Metrics

### Version 2.0 vs 1.x

| Metric | v1.x (Gemini) | v2.0 (OpenRouter) | Improvement |
|--------|---------------|-------------------|-------------|
| **Startup Time** | ~2.65 seconds | ~110ms | **24x faster** |
| **Models Available** | 5-10 (Gemini only) | 100+ (Multi-provider) | **10x more** |
| **API Cost/Launch** | $0.001 | $0.00 | **100% savings** |
| **Configuration** | Hardcoded | Cloud-based | **Instant updates** |
| **Dependency** | Only Gemini  | Multiple Companies | **More reliable** |
| **Streaming** | No | Yes (SSE) | **Real-time responses** |
| **Error Handling** | Basic | Comprehensive (400-503) | **Better UX** |
| **UI Performance** | Good | Optimized (memoization) | **Smoother** |
| **Stop Generation** | No | Yes (cancel anytime) | **User control** |


---

## Dependencies

### Core
```gradle
// Firebase
firebase-bom:33.7.0
firebase-firestore-ktx
firebase-analytics-ktx
firebase-auth-ktx

// Google Sign-In
play-services-auth:21.3.0

// Networking
retrofit:2.9.0
okhttp:4.12.0
gson:2.10.1

// PDF Generation
itext7-core:7.2.5
html2pdf:4.0.5

// Markdown Rendering
compose-markdown:0.5.4

// UI
androidx.compose:*
androidx.material3:*
lottie-compose:6.0.0
coil-compose:2.4.0
```

### Python Tools
```bash
pip install firebase-admin
```

---

## Security & Privacy

### Data Protection
- API keys stored in Firebase (not in code)
- `google-services.json` excluded from Git
- HTTPS encryption for all API calls
- Google Sign-In for secure authentication
- Chat history stored locally and synced with Firebase
- No user data shared with third parties

### User Control
- Sign out anytime from Settings
- Delete chat sessions individually
- Theme preferences stored locally
- Optional Google account sync for chat history


---

## Supported Models

### Current (50 models)
See `update_models/models.csv` for complete list including:
- Google (Gemini, Gemma variants)
- Deepseek (Chat, R1 variants)
- Meta (Llama 3.3, 3.1, 4 variants)
- Qwen (Qwen3, Qwen 2.5 variants)
- Mistral (Large, Small, Medium variants)
- xAI (Grok variants)
- Anthropic (Claude 3.5, 3 variants)
- OpenAI (GPT-4, GPT-3.5 variants)
- And 30+ more providers!

### Exception Models (":free" suffix)
Some models require the ":free" suffix to work. The app automatically:
- Detects 404 errors for models without ":free"
- Adds the model to Firebase `exp_models` collection
- Retries with correct format
- Shows user-friendly error dialog with retry option

### Add More
Simply edit CSV and run:
```bash
python update_firebase_models.py --csv models.csv
```

---

## Screenshots

### Main Chat Interface
![Mark VII Chat](https://github.com/user-attachments/assets/5ef5e209-fb29-47e3-b8b1-0bd9999a3ea9)


---

## Download

### Latest Release
- **Stable:** [GitHub Releases](https://github.com/daemon-001/Mark-VII/releases)
- **Latest:** [Latest Build](https://github.com/daemon-001/Mark-VII/releases/latest)

### Version Information
- **Current Version:** 2.0.0
- **Min Android:** 7.0 (API 24)
- **Target Android:** 14 (API 34)
---

## License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) file for details.

---

## Support

### Get Help
- Email: nitesh.kumar4work@gmail.com
- GitHub Issues: [Report Bug](https://github.com/daemon-001/Mark-VII/issues/new)
- Feature Request: [Request Feature](https://github.com/daemon-001/Mark-VII/issues/new)

### Developer
- **Author:** Nitesh (@daemon-001)
- **Project:** [Mark-VII](https://github.com/daemon-001/Mark-VII)
- **GitHub:** [daemon-001](https://github.com/daemon-001)
- **LinkedIn:** [daemon001](https://www.linkedin.com/in/daemon001)

---

## Show Your Support

If you find Mark VII helpful, please:
- Star this repository
- Report bugs
- Suggest features
- Contribute code
- Share with others

---

<div align="center">

**Made by Nitesh**

[Home](https://github.com/daemon-001/Mark-VII) • [Download](https://github.com/daemon-001/Mark-VII/releases) • [Report Bug](https://github.com/daemon-001/Mark-VII/issues) • [Request Feature](https://github.com/daemon-001/Mark-VII/issues)

*Enjoy your advanced multi-provider AI chatbot experience!*

</div>
