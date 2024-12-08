# Mark VII: AI-Powered Android Chatbot

**Mark VII** is an AI-powered Android chatbot application that combines the capabilities of multiple Natural Language Processing (NLP) models from different companies. It provides intelligent, context-aware responses, delivering a seamless and advanced conversational experience to users.

## Features

- **Multi-NLP Model Integration**: Combines state-of-the-art NLP technologies from various providers.
- **Real-time Messaging:** Instant communication between the app and the user.
- **Multi-lingual Support:** Can support multiple languages, depending on the NLP models integrated.
- **Context-Aware Conversations**: Understands and responds intelligently based on conversation history.
- **Customizable User Interactions:** Adaptable to different use cases such as customer support, personal assistance, and more.
- **Extensible Framework:** Easily extendable to add more NLP models or integrate new features.
- **Cross-Platform Support**: Easily adaptable to other platforms with minimal modifications.

## Requirements

- **Android Version**: 7.0 (Nougat) or higher
- **Development Tools**:
  - Android Studio
  - Java/Kotlin
- **APIs and SDKs**:
  - Jetpack Compose UI toolkit for building app layout. It simplifies the development process by allowing developers to design UIs using Kotlin code rather than XML.
  - Lottie to use high-quality jason-based animations.
  - Access to APIs from NLP providers (e.g., OpenAI, Google Gemini, Microsoft Azure)

## Installation (Android Studio)

### Clone the Repository
```bash
git clone https://github.com/daemon-001/Mark-VII.git
cd Mark-VII
```

### Open in Android Studio
1. Launch Android Studio.
2. Open the `Mark-VII` project directory.

### Configure API Keys
1. Go to the `com.daemon.markvii/data` folder and locate the `ChatData.kt` file.
2. Add your API keys for the integrated NLP services:
   ```
   val gemini_api_key = "YOUR API KEY"
   val openai_api_key = "YOUR API KEY"
   ```

### Build and Run
1. Sync the Gradle files.
2. Build the project.
3. Install the app on your Android device or emulator.

## APK Installation (Android Smartphones)

- Download APK file from [Download Page](#download)
- Open the downloaded app.
- Allow the **Unknown Sources** permission.
- Now install the app.
- Ready to use. (This app is fully dependent on Internet, make sure you are connected to internet).


## APK Installation (PC Android Emulators)

- Download APK file from [Download Page](#download)
- Now open APK installer of your emulator and select download **.apk** file.
- Make sure your PC is connected to internet.
- Lounch **Mark VII** app from emulator's louncher.
- Ready to use.

## Usage

- **Initiate Chat**: Start a conversation by typing commands.
- **Interact**: The chatbot will provide intelligent and context-aware responses.
- **Productivity Assistance:** The chatbot can assist users with managing tasks, setting reminders, or organizing schedules. Leveraging NLP models that understand natural language commands, it helps users stay productive and organized with minimal effort.
- **Education & Learning Assistance:** The app can provide educational content, answer academic queries, and offer language learning or training support. By using NLP models trained in different subjects, it can offer specialized knowledge across various topics, making it a valuable tool for students and professionals.
- **Entertainment & Virtual Companionship:** The AI chatbot can engage users in casual conversation, recommend movies, music, or books, and provide entertainment. It can even simulate deeper interactions by understanding humor, emotions, and user interests, making it feel like a more interactive and personal companion.
- **AI-driven Content Creation:** The app can assist content creators with brainstorming ideas, generating content, and even providing style suggestions. With NLP models that understand different writing tones and formats, it can help users create blog posts, articles, social media updates, and more.


## File Structure

```plaintext
Mark-VII/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/daemon/markvii/                  # Application source code
│   │   │   ├── java/com/daemon/markvii/data/ChatData.kt  # API keys configuration
│   │   │   └── res/                                      # App resources (layouts, strings, etc.)
├── build.gradle                                          # Project build configuration
├── README.md                                             # Project documentation
└── ...
```

## Technologies Used

- **NLP Models**:
  - Google Gemini
  - OpenAI GPT
  - More comming soon...
- **Languages**:
  - Kotlin/Java for Android development
- **Libraries**:
  - Lottie for animations
  - Jetpack Compose for ui design

## Contributing

Contributions are welcome! Here's how you can help:

1. Fork the repository.
2. Create a new branch for your feature or bug fix:
   ```bash
   git checkout -b feature-name
   ```
3. Commit your changes:
   ```bash
   git commit -m "Add your feature or fix"
   ```
4. Push the branch:
   ```bash
   git push origin feature-name
   ```
5. Submit a pull request.

## Download

* For stable releases, please go to [Github Releases page](https://github.com/daemon-001/Mark-VII/releases)
* For latest build, please check [Latest Release](https://github.com/daemon-001/Mark-VII/releases/latest)

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE.md) file for more details.

## Acknowledgments

- Thanks to OpenAI, Google, and Microsoft for their cutting-edge NLP technologies.
- Inspired by the potential of AI to revolutionize human-computer interaction.

---

Feel free to contact us with suggestions or feedback. Enjoy your advanced AI-powered chatbot experience with **Mark VII**! 

