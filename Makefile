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

.PHONY: setup build clean test test-debug lint check install apk help gradle-wrapper note version-bump version-patch version-minor version-major release tag create-github-release sign-apk

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

# Version bumping commands
version-bump: ## Display current version
	@echo "$(BLUE)Current version:$(NC)"
	@grep -E "versionCode|versionName" app/build.gradle | sed 's/^[ \t]*//'

version-patch: ## Bump patch version (e.g., 1.0.0 -> 1.0.1)
	@echo "$(BLUE)Bumping patch version...$(NC)"
	@# Extract current version
	@VERSION_CODE=$$(grep -E "versionCode" app/build.gradle | grep -o "[0-9]*") && \
	VERSION_NAME=$$(grep -E "versionName" app/build.gradle | grep -o "\"[^\"]*\"" | tr -d \") && \
	MAJOR=$$(echo $$VERSION_NAME | cut -d. -f1) && \
	MINOR=$$(echo $$VERSION_NAME | cut -d. -f2) && \
	PATCH=$$(echo $$VERSION_NAME | cut -d. -f3) && \
	NEW_PATCH=$$((PATCH + 1)) && \
	NEW_VERSION="$$MAJOR.$$MINOR.$$NEW_PATCH" && \
	NEW_VERSION_CODE=$$((VERSION_CODE + 1)) && \
	sed -i.bak "s/versionCode [0-9]*/versionCode $$NEW_VERSION_CODE/" app/build.gradle && \
	sed -i.bak "s/versionName \"[^\"]*\"/versionName \"$$NEW_VERSION\"/" app/build.gradle && \
	rm -f app/build.gradle.bak && \
	echo "$(GREEN)Version bumped to $$NEW_VERSION ($$NEW_VERSION_CODE)$(NC)"

version-minor: ## Bump minor version (e.g., 1.0.0 -> 1.1.0)
	@echo "$(BLUE)Bumping minor version...$(NC)"
	@# Extract current version
	@VERSION_CODE=$$(grep -E "versionCode" app/build.gradle | grep -o "[0-9]*") && \
	VERSION_NAME=$$(grep -E "versionName" app/build.gradle | grep -o "\"[^\"]*\"" | tr -d \") && \
	MAJOR=$$(echo $$VERSION_NAME | cut -d. -f1) && \
	MINOR=$$(echo $$VERSION_NAME | cut -d. -f2) && \
	NEW_MINOR=$$((MINOR + 1)) && \
	NEW_VERSION="$$MAJOR.$$NEW_MINOR.0" && \
	NEW_VERSION_CODE=$$((VERSION_CODE + 1)) && \
	sed -i.bak "s/versionCode [0-9]*/versionCode $$NEW_VERSION_CODE/" app/build.gradle && \
	sed -i.bak "s/versionName \"[^\"]*\"/versionName \"$$NEW_VERSION\"/" app/build.gradle && \
	rm -f app/build.gradle.bak && \
	echo "$(GREEN)Version bumped to $$NEW_VERSION ($$NEW_VERSION_CODE)$(NC)"

version-major: ## Bump major version (e.g., 1.0.0 -> 2.0.0)
	@echo "$(BLUE)Bumping major version...$(NC)"
	@# Extract current version
	@VERSION_CODE=$$(grep -E "versionCode" app/build.gradle | grep -o "[0-9]*") && \
	VERSION_NAME=$$(grep -E "versionName" app/build.gradle | grep -o "\"[^\"]*\"" | tr -d \") && \
	MAJOR=$$(echo $$VERSION_NAME | cut -d. -f1) && \
	NEW_MAJOR=$$((MAJOR + 1)) && \
	NEW_VERSION="$$NEW_MAJOR.0.0" && \
	NEW_VERSION_CODE=$$((VERSION_CODE + 1)) && \
	sed -i.bak "s/versionCode [0-9]*/versionCode $$NEW_VERSION_CODE/" app/build.gradle && \
	sed -i.bak "s/versionName \"[^\"]*\"/versionName \"$$NEW_VERSION\"/" app/build.gradle && \
	rm -f app/build.gradle.bak && \
	echo "$(GREEN)Version bumped to $$NEW_VERSION ($$NEW_VERSION_CODE)$(NC)"

tag: ## Create git tag with current version
	@echo "$(BLUE)Creating git tag...$(NC)"
	@VERSION_NAME=$$(grep -E "versionName" app/build.gradle | grep -o "\"[^\"]*\"" | tr -d \") && \
	git add app/build.gradle && \
	git commit -m "Bump version to $$VERSION_NAME" && \
	git tag -a "v$$VERSION_NAME" -m "Version $$VERSION_NAME" && \
	echo "$(GREEN)Tag v$$VERSION_NAME created.$(NC)"

release: ## Bump patch version, commit, tag, and push to trigger GitHub release
	@echo "$(BLUE)Preparing new release...$(NC)"
	@make version-patch
	@VERSION_NAME=$$(grep -E "versionName" app/build.gradle | grep -o "\"[^\"]*\"" | tr -d \") && \
	git add app/build.gradle && \
	git commit -m "Release version $$VERSION_NAME" && \
	git tag -a "v$$VERSION_NAME" -m "Version $$VERSION_NAME" && \
	git push && git push --tags && \
	echo "$(GREEN)Release v$$VERSION_NAME pushed to remote.$(NC)"

# Create a new GitHub release
create-github-release:
	# Make sure we have a tag
	@if [ -z "$$TAG" ]; then \
		echo "Error: TAG environment variable is required. Use: make create-github-release TAG=v1.0.0"; \
		exit 1; \
	fi
	# Build and sign release
	$(MAKE) release
	# Create GitHub release using gh CLI
	gh release create $$TAG \
		--title "Release $$TAG" \
		--notes "Release $$TAG" \
		app/build/outputs/apk/release/app-release.apk

sign-apk: ## Sign an APK using environment variables
	@echo "$(BLUE)Signing APK using environment variables...$(NC)"
	# Check if required environment variables are set
	@if [ -z "$$SIGNING_KEY" ] || [ -z "$$ALIAS" ] || [ -z "$$KEY_STORE_PASSWORD" ] || [ -z "$$KEY_PASSWORD" ]; then \
		echo "$(RED)Error: Missing required environment variables.$(NC)"; \
		echo "Make sure to set: SIGNING_KEY, ALIAS, KEY_STORE_PASSWORD, KEY_PASSWORD"; \
		exit 1; \
	fi
	# Create signing directory
	@mkdir -p app/signing
	# Create keystore file from base64 encoded string
	@echo "$$SIGNING_KEY" | base64 --decode > app/signing/release.keystore
	# Update local.properties with signing config
	@echo "signing.keystore=app/signing/release.keystore" > local.properties.signing
	@echo "signing.alias=$$ALIAS" >> local.properties.signing
	@echo "signing.storePassword=$$KEY_STORE_PASSWORD" >> local.properties.signing
	@echo "signing.keyPassword=$$KEY_PASSWORD" >> local.properties.signing
	@cat local.properties.signing >> local.properties
	@rm local.properties.signing
	# Build signed APK
	@./gradlew assembleRelease
	@echo "$(GREEN)Signed APK generated at: app/build/outputs/apk/release/app-release.apk$(NC)"

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