package com.luffy.smartringtest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.luffy.smartringtest.ble.BluetoothManager
import com.luffy.smartringtest.ui.HistoryScreen
import com.luffy.smartringtest.ui.ProfileScreen
import com.luffy.smartringtest.ui.TestScreen
import com.luffy.smartringtest.ui.theme.SmartRingTestTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val DAYS_TO_SYNC = 3
    }

    private lateinit var bluetoothManager: BluetoothManager

    private var userProfile by mutableStateOf<UserProfile?>(null)
    private var showingHistory by mutableStateOf(false)

    // Live-metric state
    private val devices = mutableStateListOf<Pair<String, String>>()
    private var statusText by mutableStateOf("Ready")
    private var batteryText by mutableStateOf("")
    private var heartRateText by mutableStateOf("")
    private var spo2Text by mutableStateOf("")
    private var stepsText by mutableStateOf("")
    private var sleepText by mutableStateOf("")
    private var temperatureText by mutableStateOf("")
    private var bloodPressureText by mutableStateOf("")
    private var stressText by mutableStateOf("")
    private var hrvText by mutableStateOf("")
    private var fatigueText by mutableStateOf("")
    private var isScanning by mutableStateOf(false)
    private var isConnected by mutableStateOf(false)

    // History state
    private val historyReadings = mutableStateListOf<HealthReading>()
    private var historyStatusText by mutableStateOf("Not synced yet")
    private var historyProgress by mutableStateOf(0f)
    private var isSyncingHistory by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userProfile = ProfileStore.load(this)

        bluetoothManager = BluetoothManager(this)
        bluetoothManager.init()

        requestBluetoothPermissions()

        setContent {
            SmartRingTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val profile = userProfile
                    when {
                        profile == null -> {
                            ProfileScreen(
                                onSave = { height, weight, age, isMale ->
                                    val p = UserProfile(height, weight, age, isMale)
                                    ProfileStore.save(this, p)
                                    userProfile = p
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        showingHistory -> {
                            HistoryScreen(
                                readings = historyReadings,
                                statusText = historyStatusText,
                                progress = historyProgress,
                                isSyncing = isSyncingHistory,
                                onSyncClick = { syncHistory() },
                                onBackClick = { showingHistory = false },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        else -> {
                            TestScreen(
                                devices = devices,
                                statusText = statusText,
                                batteryText = batteryText,
                                heartRateText = heartRateText,
                                spo2Text = spo2Text,
                                stepsText = stepsText,
                                sleepText = sleepText,
                                temperatureText = temperatureText,
                                bloodPressureText = bloodPressureText,
                                stressText = stressText,
                                hrvText = hrvText,
                                fatigueText = fatigueText,
                                isScanning = isScanning,
                                isConnected = isConnected,
                                onScanClick = { startScan() },
                                onDeviceClick = { _, mac -> connect(mac) },
                                onReadBatteryClick = { readBattery() },
                                onReadHeartRateClick = { readHeartRate() },
                                onReadSpO2Click = { readSpO2() },
                                onReadStepsClick = { readSteps() },
                                onReadSleepClick = { readSleep() },
                                onReadTemperatureClick = { readTemperature() },
                                onReadBloodPressureClick = { readBloodPressure() },
                                onReadStressClick = { readStress() },
                                onReadHrvClick = { readHrv() },
                                onReadFatigueClick = { readFatigue() },
                                onOpenHistoryClick = { showingHistory = true },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Permissions ---

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        statusText = if (allGranted) "Permissions granted. Ready to scan."
        else "Permissions denied. Cannot scan."
    }

    private fun requestBluetoothPermissions() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!has(Manifest.permission.BLUETOOTH_SCAN)) needed += Manifest.permission.BLUETOOTH_SCAN
            if (!has(Manifest.permission.BLUETOOTH_CONNECT)) needed += Manifest.permission.BLUETOOTH_CONNECT
        }
        if (!has(Manifest.permission.ACCESS_FINE_LOCATION)) needed += Manifest.permission.ACCESS_FINE_LOCATION

        if (needed.isEmpty()) {
            statusText = "Permissions already granted. Ready to scan."
        } else {
            statusText = "Requesting permissions..."
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    private fun has(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    // --- Connection ---

    private fun startScan() {
        devices.clear()
        clearAllMetrics()
        isScanning = true
        statusText = "Scanning..."

        bluetoothManager.startScan(
            onDeviceFound = { name, mac ->
                if (devices.none { it.second == mac }) devices.add(name to mac)
            },
            onFinished = {
                isScanning = false
                statusText = if (devices.isEmpty()) "Scan finished. No devices found." else "Scan finished."
            }
        )
    }

    private fun clearAllMetrics() {
        batteryText = ""
        heartRateText = ""
        spo2Text = ""
        stepsText = ""
        sleepText = ""
        temperatureText = ""
        bloodPressureText = ""
        stressText = ""
        hrvText = ""
        fatigueText = ""
    }

    private fun connect(mac: String) {
        statusText = "Connecting to $mac..."
        bluetoothManager.connectDevice(mac) { success, message ->
            if (success) {
                statusText = "✓ $message"
                isConnected = true
                syncProfileToRing()
            } else {
                statusText = "✗ $message"
                isConnected = false
            }
        }
    }

    private fun syncProfileToRing() {
        val profile = userProfile ?: return
        bluetoothManager.syncPersonInfo(
            height = profile.height,
            weight = profile.weight,
            age = profile.age,
            isMale = profile.isMale,
            onResult = { success, message ->
                Log.d(TAG, "syncPersonInfo: success=$success, $message")
                statusText = if (success) "✓ Connected, profile synced"
                else "✓ Connected (profile sync failed)"
            }
        )
    }

    // --- Live metrics ---

    private fun readBattery() {
        batteryText = "Reading battery..."
        bluetoothManager.readBattery { success, _, message ->
            batteryText = if (success) "🔋 $message" else "✗ $message"
        }
    }

    private fun readHeartRate() {
        heartRateText = "Reading heart rate..."
        bluetoothManager.readHeartRate { success, _, message ->
            heartRateText = if (success) "❤️ $message" else "✗ $message"
        }
    }

    private fun readSpO2() {
        spo2Text = "Reading SpO2..."
        bluetoothManager.readSpO2 { success, _, message ->
            spo2Text = if (success) "🩸 $message" else "✗ $message"
        }
    }

    private fun readSteps() {
        stepsText = "Reading steps..."
        bluetoothManager.readSteps { success, _, message ->
            stepsText = if (success) "🚶 $message" else "✗ $message"
        }
    }

    private fun readSleep() {
        sleepText = "Reading sleep..."
        bluetoothManager.readSleep { success, _, message ->
            sleepText = if (success) "😴 $message" else "✗ $message"
        }
    }

    private fun readTemperature() {
        temperatureText = "Reading temperature..."
        bluetoothManager.readTemperature { success, temp, message ->
            temperatureText = if (success) "🌡️ $message" else "✗ $message"
            Log.d(TAG, "Temperature result: success=$success temp=$temp")
        }
    }

    private fun readBloodPressure() {
        bloodPressureText = "Reading blood pressure..."
        bluetoothManager.readBloodPressure { success, systolic, diastolic, message ->
            bloodPressureText = if (success) "💓 $message" else "✗ $message"
            Log.d(TAG, "BP result: success=$success sys=$systolic dia=$diastolic")
        }
    }

    private fun readStress() {
        stressText = "Reading stress..."
        bluetoothManager.readStress { success, level, message ->
            stressText = if (success) "😰 $message" else "✗ $message"
            Log.d(TAG, "Stress result: success=$success level=$level")
        }
    }

    private fun readHrv() {
        hrvText = "Reading HRV..."
        bluetoothManager.readHrv { success, hrv, message ->
            hrvText = if (success) "💗 $message" else "✗ $message"
            Log.d(TAG, "HRV result: success=$success hrv=$hrv")
        }
    }

    private fun readFatigue() {
        fatigueText = "Reading fatigue..."
        bluetoothManager.readFatigue { success, level, message ->
            fatigueText = if (success) "😴 $message" else "✗ $message"
            Log.d(TAG, "Fatigue result: success=$success level=$level")
        }
    }

    // --- History ---

    private fun syncHistory() {
        historyReadings.clear()
        isSyncingHistory = true
        historyProgress = 0f
        historyStatusText = "Syncing $DAYS_TO_SYNC days of data..."

        bluetoothManager.syncHistory(
            daysBack = DAYS_TO_SYNC,
            onProgress = { p ->
                historyProgress = p
                historyStatusText = "Syncing... ${(p * 100).toInt()}%"
            },
            onComplete = { success, readings, message ->
                isSyncingHistory = false
                historyProgress = 0f
                if (success) {
                    historyReadings.addAll(readings)
                    historyStatusText = "✓ $message"
                } else {
                    historyStatusText = "✗ $message"
                }
            }
        )
    }
}