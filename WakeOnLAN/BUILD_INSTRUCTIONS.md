# Building the Wake On LAN App

## Important Note

**This project requires Android Studio for a complete build**. The Makefile is provided for basic development tasks, but some resources (like icon files and themes) need to be created in Android Studio before the project can be fully built or run.

## Using the Makefile (Recommended)

We provide a Makefile that simplifies common development tasks. The Makefile provides colorful output and handles common build issues automatically.

```bash
# Show all available commands
make help

# Build the project
make build

# Run all unit tests
make test

# Generate a debug APK
make apk

# Install on a connected device
make install

# Run on a connected device
make run

# Clean build files
make clean
```

## Importing the Project in Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Browse to and select the WakeOnLAN folder

### Adding Missing Resources

The project requires the following resources that need to be created in Android Studio:

1. **App Icons**: Create app icon files in `app/src/main/res/mipmap/` folders:
   - ic_launcher.xml
   - ic_launcher_round.xml
   
2. **Theme**: Create theme files in `app/src/main/res/values/` folder:
   - themes.xml (containing Theme.WakeOnLAN style)

Android Studio can create these automatically with the Asset Studio tool.

## Running Tests

### Via Makefile
```bash
make test
```

### Via Android Studio
1. In Android Studio, right-click on the `app/src/test/java` folder
2. Select "Run 'Tests in 'com.example.wakeonlan...'"
3. The test results will appear in the "Run" window

## Building and Installing the App

### Option 1: Using the Makefile
```bash
# Build and create APK
make apk

# Install on connected device
make install

# Build, install and run in one command
make run
```

### Option 2: Build and run from Android Studio
1. Connect an Android device or start an emulator
2. Click the "Run" button (green triangle) in the toolbar
3. Select your device/emulator from the list and click "OK"

### Option 3: Build APK manually
1. From the menu, select Build > Build Bundle(s) / APK(s) > Build APK(s)
2. Wait for the build to complete
3. Android Studio will show a notification with "APK(s) generated successfully"
4. Click "locate" to find the APK file
5. The APK will be in `app/build/outputs/apk/debug/app-debug.apk`

## Installing the APK Manually

1. Copy the APK to your Android device
2. On your device, find the APK file using a file manager
3. Tap the APK file and follow the prompts to install it
4. You may need to enable "Unknown sources" in your device's security settings

## Troubleshooting

### Gradle Wrapper Issues
If you encounter issues with the gradle wrapper, the Makefile should automatically fix this. If not, run:
```bash
make gradle-wrapper
```

### Missing Resources Error
If you encounter errors about missing resources (like `mipmap/ic_launcher` or `style/Theme.WakeOnLAN`), you need to:
1. Import the project into Android Studio
2. Create the missing resources using Android Studio's built-in tools

### Other Gradle Issues
If you encounter Gradle sync issues:
1. From the menu, select File > Sync Project with Gradle Files
2. If that doesn't work, try File > Invalidate Caches / Restart

### Test Failures
If tests fail:
1. Make sure all dependencies are properly loaded
2. Check that the Android SDK is properly configured in Android Studio 