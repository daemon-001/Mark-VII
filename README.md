# 

<div align="center">

<H1>Mark VII - Multi-Provider AI Chat Platform</H1>

![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)
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

### 🤖 Multi-Provider AI Access
- **Anthropic:** Claude 3.5 Sonnet, Claude 3 Opus, Claude 3 Haiku
- **OpenAI:** GPT-4 Turbo, GPT-4, GPT-3.5 Turbo
- **Meta:** Llama 3.3, Llama 3.1 70B/8B, Llama 4 Maverick/Scout
- **Deepseek:** Deepseek Chat V3.1, Deepseek R1, R1 Distill
- **Mistral:** Mistral Large, Small, Medium, Nemo
- **Google:** Gemini 2.0 Flash, Gemma 3 variants (via OpenRouter)
- **Qwen:** Qwen3 Coder, Qwen3 235B, Qwen 2.5 variants
- **And 40+ more models!**

### 🔥 Firebase Cloud Integration
- ✅ **Remote Model Management** - Add/remove models without app updates
- ✅ **Dynamic API Keys** - Update credentials remotely
- ✅ **Instant Updates** - Changes reflect immediately on app restart
- ✅ **Secure Storage** - API keys stored in Firebase, not in code
- ✅ **Automatic Fallback** - Works offline with cached configuration

### 💬 Chat Features
- 🗨️ **Text Conversations** - Chat with any AI model
- 🖼️ **Image Understanding** - Upload images and ask questions (vision models)
- 🔄 **Model Switching** - Switch between models mid-conversation
- 💾 **Chat History** - Review previous conversations
- 🏷️ **Brand Display** - See which AI (e.g., "Mark VII x Deepseek") answered

### 🎨 User Experience
- ⚡ **Instant Startup** - Welcome guide loads in <10ms
- 📱 **Material 3 Design** - Modern, beautiful interface
- 🌓 **Dynamic Themes** - Light/Dark theme support
- 🎭 **Smooth Animations** - Lottie-powered animations
- 📋 **Copy/Paste Support** - Easy text selection and sharing

### 🐍 Management Tools
- 📊 **CSV Import/Export** - Manage models in spreadsheets
- 🔄 **Bulk Updates** - Update 50+ models at once
- 🖥️ **Interactive CLI** - Menu-driven Python script
- ✅ **Validation** - Automatic format checking

---

## 🚀 Quick Start

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

## 📱 Installation

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

## 🐍 Python Management Tools

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
- ✅ Edit 50+ models in Excel/Google Sheets
- ✅ Bulk enable/disable models
- ✅ Easy reordering
- ✅ Version control friendly

---

## 📚 Documentation

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

#### HTTP 401 Error
- Invalid API key
- Get new key from [OpenRouter](https://openrouter.ai/keys)
- Update in Firebase: `app_config/api_keys/openrouterApiKey`

#### HTTP 404 Error
- Invalid model name
- Check model exists on [OpenRouter Models](https://openrouter.ai/models)
- Update model name in Firestore

#### Build Errors
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug
```

---

## 🏗️ Architecture

### Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material 3
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend:** Firebase Firestore
- **API Client:** Retrofit + OkHttp
- **AI Provider:** OpenRouter (unified API)
- **Async:** Kotlin Coroutines + StateFlow

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
│       │   └── Keys.kt                    # App metadata
│       └── ui/theme/
│           └── Theme.kt                   # Material 3 theme
├── update_models/
│   ├── update_firebase_models.py          # Model management
│   ├── models.csv                         # 49 pre-configured models
│   └── mark-vii-firebase-service-account-key.json  # Firebase admin key
├── CHANGELOG.md                           # Version history
└── README.md                              # This file
```

---

## 🎯 Usage Examples

### Basic Chat
1. Open Mark VII
2. Select a model from dropdown (e.g., "Deepseek Chat V3.1")
3. Type your message
4. Tap send (✈️)
5. Get instant AI response with "Mark VII x Deepseek" header

### Image Understanding
1. Tap camera icon (📷)
2. Select an image
3. Type a question about the image
4. Get AI analysis (works with vision-capable models)

### Comparing Models
1. Ask Claude 3.5 Sonnet a question
2. See response with "Mark VII x Anthropic"
3. Switch to GPT-4 Turbo
4. Ask the same question
5. See response with "Mark VII x Openai"
6. Compare the different approaches!

---

## 🤝 Contributing

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

## 📊 Performance Metrics

### Version 2.0 vs 1.x

| Metric | v1.x (Gemini) | v2.0 (OpenRouter) | Improvement |
|--------|---------------|-------------------|-------------|
| **Startup Time** | ~2.65 seconds | ~110ms | **24x faster** |
| **Models Available** | 5-10 (Gemini only) | 100+ (Multi-provider) | **10x more** |
| **API Cost/Launch** | $0.001 | $0.00 | **100% savings** |
| **Configuration** | Hardcoded | Cloud-based | **Instant updates** |
| **Dependency** | Only Gemini  | Multiple Companies | **More reliable** |


---

## 📦 Dependencies

### Core
```gradle
// Firebase
firebase-bom:33.7.0
firebase-firestore-ktx
firebase-analytics-ktx

// Networking
retrofit:2.9.0
okhttp:4.12.0
gson:2.10.1

// UI
androidx.compose:*
androidx.material3:*
```

### Python Tools
```bash
pip install firebase-admin
```

---

## 🔒 Security & Privacy

### Data Protection
- ✅ API keys stored in Firebase (not in code)
- ✅ `google-services.json` excluded from Git
- ✅ HTTPS encryption for all API calls
- ✅ No user data stored without consent


---

## 🌍 Supported Models

### Current (50 models)
See `update_models/models.csv` for complete list including:
- Google (Gemini, Gemma variants)
- Deepseek (Chat, R1 variants)
- Meta (Llama 3.3, 3.1, 4 variants)
- Qwen (Qwen3, Qwen 2.5 variants)
- Mistral (Large, Small, Medium variants)
- And 30+ more providers!

### Add More
Simply edit CSV and run:
```bash
python update_firebase_models.py --csv models.csv
```

---

## 📱 Screenshots

### Main Chat Interface
![Mark VII Chat](https://github.com/user-attachments/assets/5ef5e209-fb29-47e3-b8b1-0bd9999a3ea9)


---

## 📥 Download

### Latest Release
- **Stable:** [GitHub Releases](https://github.com/daemon-001/Mark-VII/releases)
- **Latest:** [Latest Build](https://github.com/daemon-001/Mark-VII/releases/latest)

### Version Information
- **Current Version:** 2.0.0
- **Min Android:** 7.0 (API 24)
- **Target Android:** 14 (API 34)
---

## 📄 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) file for details.

---

## 📞 Support

### Get Help
- 📧 Email: nitesh.kumar4work@gmail.com
- 💬 GitHub Issues: [Report Bug](https://github.com/daemon-001/Mark-VII/issues/new)
- 💡 Feature Request: [Request Feature](https://github.com/daemon-001/Mark-VII/issues/new)

### Developer
- **Author:** Nitesh (@daemon-001)
- **Project:** [Mark-VII](https://github.com/daemon-001/Mark-VII)
- **GitHub:** [daemon-001](https://github.com/daemon-001)
- **LinkedIn:** [daemon001](https://www.linkedin.com/in/daemon001)

---

## ⭐ Show Your Support

If you find Mark VII helpful, please:
- ⭐ Star this repository
- 🐛 Report bugs
- 💡 Suggest features
- 🤝 Contribute code
- 📢 Share with others

---

<div align="center">

**Made with ❤️ by Nitesh**

[🏠 Home](https://github.com/daemon-001/Mark-VII) • [📥 Download](https://github.com/daemon-001/Mark-VII/releases) • [🐛 Report Bug](https://github.com/daemon-001/Mark-VII/issues) • [💡 Request Feature](https://github.com/daemon-001/Mark-VII/issues)

*Enjoy your advanced multi-provider AI chatbot experience!*

</div>
