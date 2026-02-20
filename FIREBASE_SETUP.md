# Firebase Integration Setup Guide

This guide will help you set up Firebase for your Mark-VII Android app to manage Gemini AI models and API keys remotely.

## Prerequisites

- Android Studio installed
- A Google account for Firebase Console access
- Your Mark-VII project open in Android Studio

## Step 1: Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click on "Add project" or "Create a project"
3. Enter project name: `Mark-VII` (or your preferred name)
4. Optionally enable Google Analytics (recommended)
5. Click "Create project"

## Step 2: Add Android App to Firebase Project

1. In Firebase Console, click on the Android icon to add an Android app
2. Enter the following details:
   - **Android package name**: `com.daemon.markvii`
   - **App nickname** (optional): `Mark VII`
   - **Debug signing certificate SHA-1** (optional but recommended for development)

### To get SHA-1 certificate:
```bash
# Windows (PowerShell)
cd D:\Workspace\Project\Android-Studio\Mark-VII
.\gradlew signingReport

# Look for SHA-1 under "Variant: debug" in the output
```

3. Click "Register app"

## Step 3: Download google-services.json

1. Download the `google-services.json` file from Firebase Console
2. Place it in your app module directory:
   ```
   Mark-VII/
   └── app/
       └── google-services.json  <-- Place it here
   ```

**IMPORTANT**: Never commit `google-services.json` to public repositories!

## Step 4: Enable Firestore Database

1. In Firebase Console, go to **Build** > **Firestore Database**
2. Click "Create database"
3. Choose **Start in test mode** (for development)
4. Select your preferred location
5. Click "Enable"

### Security Rules (Important for Production)

For development (test mode):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.time < timestamp.date(2025, 12, 31);
    }
  }
}
```

For production (more secure):
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /app_config/{document} {
      allow read: if true;  // Allow anyone to read config
      allow write: if false; // Only admin can write via console
    }
  }
}
```

## Step 5: Set Up Firestore Data Structure

### Create the following collections and documents in Firestore:

#### Collection: `app_config`

##### Document: `models`
```json
{
  "list": [
    {
      "displayName": "Gemini 1.5 8b ⚡",
      "apiModel": "gemini-1.5-flash-8b",
      "isAvailable": true,
      "order": 0
    },
    {
      "displayName": "Gemini 1.5 ⚡⚡",
      "apiModel": "gemini-1.5-flash",
      "isAvailable": true,
      "order": 1
    },
    {
      "displayName": "Gemini 2.0 lite 🤖",
      "apiModel": "gemini-2.0-flash-lite",
      "isAvailable": true,
      "order": 2
    },
    {
      "displayName": "Gemini 2.0 🤖🤖",
      "apiModel": "gemini-2.0-flash",
      "isAvailable": true,
      "order": 3
    },
    {
      "displayName": "Gemini 2.5 lite 🚀",
      "apiModel": "gemini-2.5-flash-lite",
      "isAvailable": true,
      "order": 4
    },
    {
      "displayName": "Gemini 2.5 🚀🚀",
      "apiModel": "gemini-2.5-flash",
      "isAvailable": true,
      "order": 5
    }
  ]
}
```

##### Document: `api_keys`
```json
{
  "geminiApiKey": "YOUR_GEMINI_API_KEY_HERE"
}
```

**IMPORTANT**: Replace `YOUR_GEMINI_API_KEY_HERE` with your actual Google Gemini API key.

### How to Create Documents in Firestore:

1. Go to Firebase Console > Firestore Database
2. Click "Start collection"
3. Collection ID: `app_config`
4. Click "Next"
5. Document ID: `models`
6. Add field:
   - Field: `list`
   - Type: `array`
   - Click "Add item" for each model entry
7. For each array item, add the following fields:
   - `displayName` (string)
   - `apiModel` (string)
   - `isAvailable` (boolean)
   - `order` (number)
8. Click "Save"

Repeat for the `api_keys` document:
1. Click "Add document"
2. Document ID: `api_keys`
3. Add field:
   - Field: `geminiApiKey`
   - Type: `string`
   - Value: Your Gemini API key
4. Click "Save"

## Step 6: Sync Gradle Files

1. In Android Studio, open `build.gradle.kts` files
2. Click "Sync Now" at the top of the editor
3. Wait for the build to complete

## Step 7: Test the Integration

1. Run your app on an emulator or physical device
2. The app should now:
   - Fetch models from Firebase at startup
   - Display "Loading..." briefly while fetching
   - Show models in the dropdown from Firebase
   - Use the API key from Firebase for Gemini calls

## Updating Models and API Keys

### To Add/Update Models:
1. Go to Firebase Console > Firestore Database
2. Navigate to `app_config/models`
3. Edit the `list` array
4. Add new model objects or modify existing ones
5. Changes will be reflected in the app on next startup or refresh

### To Update API Key:
1. Go to Firebase Console > Firestore Database
2. Navigate to `app_config/api_keys`
3. Update the `geminiApiKey` field
4. Changes will be reflected immediately on next app startup

## Fallback Mechanism

The app includes a fallback mechanism:
- If Firebase is unreachable, it uses default models from `ModelConfiguration`
- If Firebase API key is not available, it uses the key from `Keys.kt`
- This ensures the app continues to work even if Firebase is down

## Troubleshooting

### Issue: "google-services.json not found"
**Solution**: Make sure the file is in `app/` directory, not in the root

### Issue: "FirebaseApp initialization unsuccessful"
**Solution**: 
- Check that package name in Firebase matches `com.daemon.markvii`
- Verify `google-services.json` is properly placed
- Sync Gradle files again

### Issue: "Permission denied" when reading Firestore
**Solution**: 
- Check Firestore security rules
- Make sure rules allow read access to `app_config` collection

### Issue: Models not loading from Firebase
**Solution**:
- Check internet connection on device/emulator
- Verify Firestore data structure matches the expected format
- Check Android Logcat for error messages (filter by "FirebaseConfigManager")

## Security Best Practices

1. **Never commit `google-services.json` to public repositories**
2. **Store sensitive API keys in Firebase, not in code**
3. **Use Firestore security rules to prevent unauthorized writes**
4. **Consider using Firebase Remote Config for more advanced scenarios**
5. **Rotate API keys periodically**
6. **Monitor Firebase usage in the console**

## Adding to .gitignore

Add these lines to your `.gitignore`:
```
# Firebase
google-services.json
app/google-services.json
```

## Next Steps

- Set up Firebase Analytics to track model usage
- Implement Firebase Remote Config for A/B testing different models
- Add Firebase Crashlytics for better error tracking
- Consider adding authentication for personalized model preferences

## Support

For issues or questions:
- Check [Firebase Documentation](https://firebase.google.com/docs)
- Check [Android Firebase Guide](https://firebase.google.com/docs/android/setup)
- Review app logs using Android Logcat

---

**Last Updated**: October 2025
**App Version**: 1.1
**Firebase SDK Version**: 33.7.0

