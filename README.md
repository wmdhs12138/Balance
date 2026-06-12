# Balance / 余额查询

<p>
  <img src="https://img.shields.io/badge/platform-Android-brightgreen" alt="Platform" />
  <img src="https://img.shields.io/badge/minSdk-26-orange" alt="Min SDK" />
  <img src="https://img.shields.io/badge/kotlin-2.1-purple" alt="Kotlin" />
  <img src="https://img.shields.io/github/license/wmdhs12138/Balance" alt="License" />
  <img src="https://img.shields.io/badge/Compose-Material%203-blue" alt="Compose" />
</p>

An open-source Android client for querying AI relay station account balances.

一个开源的 Android 客户端，用于查询 AI 中转站账户余额。

## Screenshots

<div align="center">
  <img width="240" alt="Main screen" src="https://github.com/user-attachments/assets/f2c60dba-500d-48fa-b8cc-d813c23250d6" />
  &nbsp;&nbsp;&nbsp;
  <img width="240" alt="Settings" src="https://github.com/user-attachments/assets/4afad6c0-db6e-454c-b19e-61ed31988c9b" />
</div>


## Features

- 🔍 **Multi-provider support** — built-in seeds for popular relay stations + unlimited custom endpoints
- 🔐 **Local-first & private** — login payloads encrypted with Android Keystore AES/GCM, never leave your device
- 🎨 **Material 3 + Dynamic Color** — adapts to your system theme on Android 12+
- 🌙 **Theme modes** — light, dark, and system-follow
- 🌍 **I18n** — English, 简体中文, Français, Deutsch, Русский, 日本語
- 📦 **Room local database** — persistent provider list and cached balance state
- 🧩 **MVVM architecture** — Kotlin Coroutines + Flow for clean separation

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (Coroutines + Flow) |
| Database | Room (SQLite) |
| Preferences | DataStore |
| Encryption | Android Keystore AES/GCM |
| Build | Gradle KTS + Version Catalog |
| SDK | compileSdk 35, minSdk 26 |

## Build

```bash
# Clone the repository
git clone https://github.com/wmdhs12138/Balance.git

# Open in Android Studio, or build via command line:
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/`.

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/io/github/wmdhs12138/balance/
│   │   │   ├── core/
│   │   │   │   ├── balance/       # Balance fetcher implementations
│   │   │   │   ├── crypto/        # AES/GCM encryption
│   │   │   │   ├── database/      # Room database & DAOs
│   │   │   │   ├── model/         # Domain models
│   │   │   │   ├── net/           # URL utilities
│   │   │   │   ├── preferences/   # DataStore preferences
│   │   │   │   └── repository/    # App container & repositories
│   │   │   ├── feature/
│   │   │   │   ├── login/         # Web login
│   │   │   │   └── main/          # Main screen (Compose)
│   │   │   └── ui/
│   │   │       ├── locale/        # Locale context
│   │   │       └── theme/         # Color, Typography, Theme
│   │   └── res/                   # Resources & i18n strings
│   └── test/                      # Unit tests
├── schemas/                        # Room migration schemas
└── build.gradle.kts
```

## Privacy

Balance is **local-first**. All data — provider configurations, cookies, and login payloads — is stored exclusively on your device. Login payloads are further encrypted at rest using Android Keystore-backed AES/GCM. No telemetry, no analytics, no network requests beyond the relay stations you configure.

Backup is explicitly disabled for the app's local database via `android:allowBackup="false"` on the Room database.

## Contributing

Contributions are welcome! Please feel free to submit a PR.

- For translation contributions, see [docs/translations.md](docs/translations.md).

## License

[MIT](LICENSE) © 2026 wmdhs12138
