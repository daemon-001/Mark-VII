# Changelog

All notable changes to the Mark VII project are documented in this file.

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