# 💍 Smart Ring OS — Health & Fitness Companion App

A modern, native Android application built with **Jetpack Compose (Material 3)** for real-time health monitoring, ECG waveform visualization, daily activity tracking, and Bluetooth Low Energy (BLE) management for Smart Rings.

Inspired by commercial health apps like **H Ring / H Band**, this app seamlessly interfaces with smart ring sensors via the Veepoo BLE Protocol and Nordic MCU Manager.

---

## 🌟 Key Features

### 1. 🏠 Health & Vitals Dashboard
- **Dynamic Daytime Greeting**: Displays personalized greetings (*Good Morning ☀️*, *Good Afternoon 🌤️*, *Good Evening 🌙*, *Good Night 🌌*) alongside current date.
- **Daily Activity Goal Meter**: Step progress bar towards a 10,000 steps goal with completion percentage, distance (km), and calories burned (kcal).
- **Vitals Overview Grid**: Direct measurement triggers for:
  - ❤️ **Heart Rate** (BPM & HR Zone classification)
  - 🩸 **Blood Oxygen ($SpO_2$)**
  - 😴 **Sleep Summary**
  - 🌡️ **Body Temperature** (°C / °F)
  - 💓 **Blood Pressure** (mmHg)
  - 😰 **Stress Level**

### 2. 📈 Real-Time ECG Monitor
- **Medical Grid Canvas**: Dark medical paper canvas with real-time neon green ECG trace rendering.
- **Dynamic Auto-Gain**: Auto-scales raw ADC voltage samples so $P$-$Q$-$R$-$S$-$T$ heartbeat peaks stand out crisply.
- **Electrode Contact Quality**: Live wear contact status indicator (*Contact OK* vs *Low Contact* warning).
- **Diagnostic Outcome Summary**: Detailed diagnostic flags (*Normal Sinus Rhythm*, *Arrhythmia*, *Tachycardia*, *Bradycardia*, *HRV*, *QT duration*).

### 3. 💍 Dedicated Device Management Center
- **Radar Scanning Animation**: Circular pulse ripple radar when searching for BLE smart rings.
- **Signal Strength Indicator**: Displays RSSI meter (*e.g., 📶 Strong -62 dBm*).
- **Ring Battery Gauge**: Battery status and one-tap battery query.
- **Device Connection Controls**: One-tap scan, connect, profile sync, and disconnect actions.

### 4. 📜 Historical Vitals & Activity Logs
- Multi-day historical log syncing from ring onboard flash memory.
- Detailed log cards showing date, time, wearing state, heart rate, step count, distance, and calories.

### 5. 👤 Profile & App Preferences
- **Automatic BMI Calculator**: Real-time Body Mass Index computation ($BMI = \frac{\text{weight(kg)}}{(\text{height(m)})^2}$) with health status classification badge (*Normal Weight*, *Overweight*, etc.).
- **Biometric Setup**: Custom height, weight, age, and biological sex parameters synced to the ring for step stride & calorie accuracy.
- **Appearance & Units**:
  - Light Mode ☀️ & Dark Mode 🌙 theme toggle.
  - Unit System selector (Metric: cm/kg vs Imperial: in/lbs).

---

## 📱 Tech Stack & Architecture

- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Single-Activity Architecture + Compose State Management
- **Min SDK**: 26 (Android 8.0) | **Target/Compile SDK**: 37 (Android 15)
- **Hardware Integration**:
  - Bluetooth Low Energy (BLE)
  - Veepoo Protocol SDK (`vpprotocol`, `vpbluetooth`)
  - Nordic MCU Manager (`no.nordicsemi.android:mcumgr-core`)
- **Splash Screen**: `androidx.core:core-splashscreen`

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug / Jellyfish or newer
- **JDK**: Java 17 or higher
- **Android Device**: Physical Android device with Bluetooth & Location permissions enabled (Android 8.0+)

### Building & Running
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/SmartRingTest.git
   cd SmartRingTest
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle dependencies:
   ```bash
   ./gradlew assembleDebug
   ```
4. Connect your Android test device via USB/Wi-Fi debugging and click **Run ▶️**.

---

## 🔒 Permissions
The application requires the following runtime permissions:
- `android.permission.BLUETOOTH_SCAN` (Android 12+)
- `android.permission.BLUETOOTH_CONNECT` (Android 12+)
- `android.permission.ACCESS_FINE_LOCATION` (Required for BLE discovery on Android <= 11)

---

## 📄 License
Distributed under the MIT License. See `LICENSE` for more details.
