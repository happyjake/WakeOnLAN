#!/bin/bash

# Script to load Android signing configuration from .env file

# Check for .env file
ENV_FILE=".env"
if [ ! -f "$ENV_FILE" ]; then
    echo "Error: .env file not found."
    echo "Please create a .env file with the following variables:"
    echo "SIGNING_KEY=base64_encoded_keystore_file"
    echo "ALIAS=your_key_alias"
    echo "KEY_STORE_PASSWORD=your_keystore_password"
    echo "KEY_PASSWORD=your_key_password"
    exit 1
fi

# Source the environment variables
source "$ENV_FILE"

# Check if all required variables are set
if [ -z "$SIGNING_KEY" ] || [ -z "$ALIAS" ] || [ -z "$KEY_STORE_PASSWORD" ] || [ -z "$KEY_PASSWORD" ]; then
    echo "Error: Missing one or more required variables in $ENV_FILE"
    echo "Please make sure the following variables are set:"
    echo "SIGNING_KEY, ALIAS, KEY_STORE_PASSWORD, KEY_PASSWORD"
    exit 1
fi

echo "Environment variables loaded successfully."
echo "You can now run 'make sign-apk' to create a signed APK."

# Export the variables for use in the current shell
export SIGNING_KEY
export ALIAS
export KEY_STORE_PASSWORD
export KEY_PASSWORD
