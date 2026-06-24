# Glucose Diary

**Glucose Diary** is an Android application for diabetes management. It helps users track blood glucose, diet, exercise, and medication; visualize health trends; receive medication reminders; and get AI-powered guidance tailored to their data.

Built with **Kotlin** and **Jetpack Compose** as part of the **FIT5046** mobile development project.

---

## Features

### Dashboard
- Real-time glucose overview with trend charts
- Daily nutrition and exercise summaries
- Next medication reminder at a glance
- Floating AI assistant (**Diabetica**) for context-aware diabetes support

### Health Logging (Diary)
- Log and manage four entry types:
  - **Blood Glucose** — manual readings and simulated sensor data
  - **Diet** — food search, nutrition info, and camera-based food recognition (ML Kit)
  - **Exercise** — type, duration, and intensity
  - **Medication** — dosage and timing
- Filter logs by date range and type
- Edit or delete existing entries

### Reports
- Date-range health reports with interactive charts (MPAndroidChart)
- Glucose, diet, exercise, and medication visualizations
- Export reports as **PDF**
- Share reports via email or download

### Glucose Spike Analysis
- Detects glucose spikes above normal range (4.0–7.8 mmol/L)
- Correlates spikes with recent food intake
- Generates personalized food recommendations (avoid, moderate, safe, beneficial)

### Medication Reminders
- Schedule recurring medication reminders by day and time
- Local notifications with exact alarm support
- Reminder management from Settings

### Health Blog
- Curated diabetes-related news articles via the News API
- Category filtering and article sharing

### User Profile & Authentication
- Firebase email/password sign-up and sign-in
- Multi-step onboarding flow (profile, diabetes type, goals)
- Profile photo upload to Firebase Storage
- User settings and app information

### AI Chatbot (Diabetica)
- Context-aware assistant powered by **Ollama** (local LLM)
- Uses live glucose, diet, exercise, medication, and profile data for personalized responses
- Accessible from the Dashboard floating action button

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Navigation | Navigation Compose |
| Local Database | Room |
| Backend / Auth | Firebase Auth, Firestore, Storage |
| Networking | Retrofit, OkHttp |
| Image Loading | Coil |
| Charts | MPAndroidChart |
| ML | Google ML Kit (image labeling) |
| Camera | CameraX |
| AI | Ollama (HTTP API) |
| News | News API |

---

## Requirements

- **Android Studio** Ladybug or newer (recommended)
- **JDK 11**
- **Android SDK 36** (compile & target)
- **Minimum SDK 26** (Android 8.0)
- A physical device or emulator with camera support (camera is required)
- **Firebase** project with Auth, Firestore, and Storage enabled
- **Ollama** running locally (for AI chatbot features)
- **News API** key (for Health Blog)

---

## Getting Started

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd ass2
```

### 2. Firebase setup

1. Create a project in the [Firebase Console](https://console.firebase.google.com/).
2. Register an Android app with package name: `com.example.fit5046_app`
3. Download `google-services.json` and place it in the `app/` directory.
4. Enable **Authentication** (Email/Password).
5. Enable **Cloud Firestore** and **Firebase Storage**.
6. Configure Firestore security rules appropriate for your environment.

### 3. News API key

1. Obtain a free API key from [News API](https://newsapi.org/).
2. Update the key in `app/src/main/java/com/example/fit5046_app/NewsRepository.kt`:

```kotlin
private val apiKey = "YOUR_NEWS_API_KEY"
```

### 4. Ollama setup (AI chatbot)

The chatbot connects to Ollama at `http://10.0.2.2:11434` (Android emulator → host machine).

1. Install [Ollama](https://ollama.com/) on your development machine.
2. Pull a model (e.g. `llama3.2` or the model referenced in `OllamaService.kt`).
3. Start the Ollama server:

```bash
ollama serve
```

For a **physical device**, change `OLLAMA_BASE_URL` in `OllamaService.kt` to your machine's LAN IP (e.g. `http://192.168.1.x:11434`).

### 5. Build and run

Open the project in Android Studio, sync Gradle, then:

```bash
./gradlew assembleDebug
```

Or use **Run** in Android Studio with a connected device or emulator.

---

## Project Structure

```
app/src/main/java/com/example/fit5046_app/
├── components/          # Reusable chart and UI components
├── data/                # Room entities, DAOs, database, sensor provider
├── model/               # Domain models (Log, UserProfile, FoodDatabase, etc.)
├── network/             # Retrofit DTOs and News API service
├── service/             # Ollama, glucose spike analysis, reminders, ML nutrition
├── ui/theme/            # Compose theme, colors, typography, food search UI
├── util/                # PDF generation, report download, email share
└── utils/               # Image picker and image utilities

Key screens:
├── MainActivity.kt          # App entry, navigation, auth routing
├── Dashboard.kt             # Home screen with summaries and charts
├── Diary.kt                 # Log history and filtering
├── Log.kt                   # Create/edit health entries
├── Reports.kt               # Analytics and PDF export
├── HealthBlog.kt            # Diabetes news feed
├── GlucoseSpikeAnalysisScreen.kt
├── MedicationReminderScreen.kt
├── OnboardingFlow.kt        # New user registration wizard
├── FloatingChatBot.kt       # AI assistant dialog
└── UserSetting.kt / UserProfile.kt
```

---

## Data & Storage

### Local (Room)
- Blood glucose, diet, exercise, and medication logs
- Medication reminders
- Glucose spike analysis results and food correlations

### Cloud (Firebase)
- **Auth** — user accounts
- **Firestore** — user profiles (`users`, `userProfiles` collections)
- **Storage** — profile images

### Simulated Sensor
Glucose readings are simulated from `app/src/main/assets/SmartSensor_DiabetesMonitoring_1200.csv`, cycling every 10 seconds and persisting to the local database via `SensorDataProvider`.

---

## Permissions

The app requests:

| Permission | Purpose |
|------------|---------|
| `INTERNET` | Firebase, News API, Ollama |
| `CAMERA` | Food photo capture and ML analysis |
| `READ_MEDIA_IMAGES` | Gallery image selection |
| `POST_NOTIFICATIONS` | Medication reminders |
| `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` | Timely medication alerts |
| `VIBRATE` / `WAKE_LOCK` | Notification delivery |

---

## Screenshots

> Add screenshots of the Dashboard, Diary, Reports, and Chatbot here before publishing.

---

## Testing

```bash
./gradlew test              # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests (device/emulator required)
```

---

## Known Limitations

- Glucose sensor data is **simulated** from a CSV file, not a real CGM device.
- The AI chatbot requires a **running Ollama instance** on the host network.
- News API free tier has request limits and may not work from all regions.
- `google-services.json` and API keys should not be committed to public repositories — use environment-specific config for production.

---

## License

This project was developed for educational purposes as part of FIT5046. Add your chosen license here if distributing publicly.

---

## Authors

Monash University — FIT5046 Mobile Application Development
