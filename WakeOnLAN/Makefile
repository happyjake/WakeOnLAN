# Makefile for Wake-on-LAN Android app
# This simplifies common Android development commands

# Set default goal
.DEFAULT_GOAL := help

# ANSI color codes for prettier output
YELLOW := \033[1;33m
GREEN := \033[1;32m
RED := \033[1;31m
BLUE := \033[1;34m
NC := \033[0m # No Color

# Define the path to the gradle wrapper jar
GRADLE_WRAPPER_JAR := gradle/wrapper/gradle-wrapper.jar
GRADLE_WRAPPER_DOWNLOAD_URL := https://github.com/gradle/gradle/raw/v7.5.0/gradle/wrapper/gradle-wrapper.jar

# Ensure gradlew is executable
$(shell chmod +x gradlew)

.PHONY: setup build clean test test-debug lint check install apk help gradle-wrapper note

note: ## Note about Android Studio
	@echo "$(YELLOW)NOTE: This project is designed to be imported into Android Studio.$(NC)"
	@echo "$(YELLOW)For a complete build, please open the project in Android Studio.$(NC)"
	@echo "$(YELLOW)The Makefile is provided for basic development tasks, but some tasks require Android Studio.$(NC)"
	@echo ""
	@echo "$(YELLOW)Missing resources (mipmap/ic_launcher, etc.) need to be created in Android Studio.$(NC)"
	@echo ""

gradle-wrapper: ## Download gradle wrapper jar
	@echo "$(BLUE)Downloading gradle wrapper jar...$(NC)"
	@mkdir -p gradle/wrapper
	@curl -L -o $(GRADLE_WRAPPER_JAR) $(GRADLE_WRAPPER_DOWNLOAD_URL)
	@echo "$(GREEN)Gradle wrapper download complete.$(NC)"

setup: gradle-wrapper ## Ensure gradlew is executable
	@echo "$(BLUE)Setting up project...$(NC)"
	@chmod +x gradlew
	@echo "$(GREEN)Setup complete.$(NC)"

build: setup note ## Build the project
	@echo "$(BLUE)Building project...$(NC)"
	@./gradlew build || (echo "$(RED)Build failed. Some resources may be missing. Please import into Android Studio.$(NC)" && exit 1)
	@echo "$(GREEN)Build complete.$(NC)"

clean: setup ## Clean build files
	@echo "$(BLUE)Cleaning project...$(NC)"
	@./gradlew clean
	@echo "$(GREEN)Clean complete.$(NC)"

test: setup note ## Run unit tests
	@echo "$(BLUE)Running unit tests...$(NC)"
	@./gradlew test || (echo "$(RED)Tests failed, but some failures may be due to missing resources.$(NC)" && echo "$(RED)Please import into Android Studio for a complete build.$(NC)")
	@echo "$(GREEN)Tests complete.$(NC)"

test-debug: setup note ## Run unit tests with detailed output
	@echo "$(BLUE)Running unit tests with detailed output...$(NC)"
	@./gradlew test --stacktrace --info || (echo "$(RED)Tests failed, but some failures may be due to missing resources.$(NC)" && echo "$(RED)Please import into Android Studio for a complete build.$(NC)")
	@echo "$(GREEN)Tests complete.$(NC)"

lint: setup note ## Run lint checks
	@echo "$(BLUE)Running lint checks...$(NC)"
	@./gradlew lint || (echo "$(RED)Lint failed, but some failures may be due to missing resources.$(NC)" && echo "$(RED)Please import into Android Studio for a complete build.$(NC)")
	@echo "$(GREEN)Lint complete.$(NC)"

check: setup note ## Run all checks (lint and tests)
	@echo "$(BLUE)Running all checks...$(NC)"
	@./gradlew check || (echo "$(RED)Checks failed, but some failures may be due to missing resources.$(NC)" && echo "$(RED)Please import into Android Studio for a complete build.$(NC)")
	@echo "$(GREEN)All checks complete.$(NC)"

install: setup note ## Build and install on connected device
	@echo "$(BLUE)Installing app on connected device...$(NC)"
	@./gradlew installDebug || (echo "$(RED)Install failed. Some resources may be missing. Please import into Android Studio.$(NC)" && exit 1)
	@echo "$(GREEN)Install complete.$(NC)"

apk: setup note ## Build debug APK
	@echo "$(BLUE)Building debug APK...$(NC)"
	@./gradlew assembleDebug || (echo "$(RED)APK build failed. Some resources may be missing. Please import into Android Studio.$(NC)" && exit 1)
	@echo "$(GREEN)APK build complete. File located at:$(NC)"
	@echo "$(YELLOW)app/build/outputs/apk/debug/app-debug.apk$(NC)"

run: install ## Install and run the app on connected device
	@echo "$(BLUE)Launching app...$(NC)"
	@adb shell am start -n com.example.wakeonlan/.MainActivity
	@echo "$(GREEN)App launched.$(NC)"

uninstall: ## Uninstall app from connected device
	@echo "$(BLUE)Uninstalling app from connected device...$(NC)"
	@adb uninstall com.example.wakeonlan
	@echo "$(GREEN)Uninstall complete.$(NC)"

help: ## Show this help
	@echo "Wake-on-LAN Android App Makefile"
	@echo ""
	@echo "$(YELLOW)Usage:$(NC)"
	@echo "  make $(GREEN)<target>$(NC)"
	@echo ""
	@echo "$(YELLOW)Targets:$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-15s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@make note 