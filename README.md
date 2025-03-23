# Wake On LAN Android App

A simple Android application that allows you to send Wake-on-LAN magic packets to wake up devices on your local network.

## Features

- Send Wake-on-LAN magic packets to remote devices
- Validate MAC address input with multiple formats (00:11:22:33:44:55, 00-11-22-33-44-55, 001122334455)
- Uses standard WoL port (9)
- Works on any Android device with API level 21+ (Android 5.0 and above)
- Automatic update checking from GitHub releases

## Project Structure

This project was built following test-driven development principles:

1. **Core Functionality (WakeOnLan.kt)** - Handles MAC address validation, magic packet creation, and network operations
2. **User Interface (MainActivity.kt)** - Simple UI for entering MAC address and broadcast IP
3. **Update Checker (UpdateChecker.kt)** - Checks for app updates from GitHub releases
4. **Unit Tests** - Comprehensive tests for both core functionality and UI interactions

## How to Use

1. Enter the MAC address of the target device
2. Enter the broadcast IP address of your network (typically 192.168.1.255)
3. Tap the "Wake Device" button to send the magic packet

The app will automatically check for updates when launched and prompt you to download and install new versions when available.

## Requirements

- The target device must have Wake-on-LAN enabled in its BIOS/firmware settings
- Your network must allow broadcast packets
- The device that you want to wake up must be connected to the same network (or accessible via the provided broadcast IP)
- Internet connection (for update checking)

## Building and Installation

### Important Note

This project requires Android Studio for a complete build. The Makefile is provided for basic development tasks, but some resources (like icon files and themes) need to be created in Android Studio.

### Using the Makefile

We provide a Makefile to simplify common development tasks:

```bash
# Show help with all available commands
make help

# Build the project
make build

# Run tests
make test

# Build debug APK
make apk

# Install on a connected device
make install

# Run the app on a connected device
make run
```

For more detailed instructions, see [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md).

## Running from the Command Line

You can also use the app through Android's shell interface by launching an intent:

```bash
am start -n com.example.wakeonlan/.MainActivity --es "mac" "00:11:22:33:44:55" --es "ip" "192.168.1.255"
```

## Update System

The app includes an automatic update mechanism that:

1. Checks GitHub releases for newer versions on app startup
2. Shows a notification dialog when updates are available
3. Allows downloading and installing the update directly from the app
4. Handles version comparison to ensure only newer versions are offered

This ensures you always have the latest version with the newest features and bug fixes. 