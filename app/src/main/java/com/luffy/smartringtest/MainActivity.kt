package com.luffy.smartringtest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.luffy.smartringtest.ble.BluetoothManager
import com.luffy.smartringtest.ui.DashboardScreen
import com.luffy.smartringtest.ui.DeviceScreen
import com.luffy.smartringtest.ui.EcgScreen
import com.luffy.smartringtest.ui.HistoryScreen
import com.luffy.smartringtest.ui.ProfileScreen
import com.luffy.smartringtest.ui.theme.Primary
import com.luffy.smartringtest.ui.theme.SmartRingTestTheme
import com.luffy.smartringtest.ui.theme.StepsGreen
import com.luffy.smartringtest.ui.theme.TextSecondary
import com.veepoo.protocol.model.datas.EcgDiagnosis

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val DAYS_TO_SYNC = 3
    }

    private lateinit var bluetoothManager: BluetoothManager

    private var userProfile by mutableStateOf<UserProfile?>(null)
    private var isDarkTheme by mutableStateOf(false)
    private var stepGoal by mutableIntStateOf(10000)

    // Live-metric state
    private val devices = mutableStateListOf<Pair<String, String>>()
    private var statusText by mutableStateOf("Ready")
    private var batteryText by mutableStateOf("")
    private var heartRateText by mutableStateOf("")
    private var spo2Text by mutableStateOf("")
    private var pbgText by mutableStateOf("")
    private var stepsText by mutableStateOf("")
    private var sleepText by mutableStateOf("")
    private var temperatureText by mutableStateOf("")
    private var bloodPressureText by mutableStateOf("")
    private var stressText by mutableStateOf("")

    // ECG state
    private var ecgStatusText by mutableStateOf("Ready")
    private var ecgProgress by mutableStateOf(0f)
    private var ecgHeartRate by mutableStateOf(0)
    private var ecgWearOk by mutableStateOf(false)
    private val ecgWaveform = mutableListOf<Int>()
    private var ecgWaveformVersion by mutableStateOf(0)
    private var ecgResultSummary by mutableStateOf("")
    private var ecgDiagnosisText by mutableStateOf("")
    private var isEcgMeasuring by mutableStateOf(false)

    private var isScanning by mutableStateOf(false)
    private var isConnected by mutableStateOf(false)

    // History & Sync state
    private val historyReadings = mutableStateListOf<HealthReading>()
    private var historyStatusText by mutableStateOf("Not synced yet")
    private var historyProgress by mutableStateOf(0f)
    private var isSyncingHistory by mutableStateOf(false)
    private var showSyncSuccessAnimation by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        userProfile = ProfileStore.load(this)
        stepGoal = ProfileStore.loadStepGoal(this)

        bluetoothManager = BluetoothManager(this)
        bluetoothManager.init()
        
        bluetoothManager.onConnectionStateChanged = { connected ->
            runOnUiThread {
                isConnected = connected
                if (!connected) {
                    statusText = "Ring disconnected"
                }
            }
        }

        requestBluetoothPermissions()

        setContent {
            SmartRingTestTheme(darkTheme = isDarkTheme) {
                var selectedTab by remember { mutableIntStateOf(0) }
                var showingEcgScreen by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (!showingEcgScreen) {
                            BottomNavBar(
                                selectedTab = selectedTab,
                                isConnected = isConnected,
                                onTabSelected = { selectedTab = it }
                            )
                        }
                    }
                ) { innerPadding ->
                    androidx.compose.runtime.LaunchedEffect(isConnected) {
                        if (isConnected) {
                            while (true) {
                                kotlinx.coroutines.delay(300_000L) // 5-minute auto-sync
                                syncHistory()
                            }
                        }
                    }

                    if (showingEcgScreen) {
                        EcgScreen(
                            statusText = ecgStatusText,
                            progress = ecgProgress,
                            heartRate = ecgHeartRate,
                            wearOk = ecgWearOk,
                            waveform = ecgWaveform.toList(),
                            resultSummary = ecgResultSummary,
                            diagnosisText = ecgDiagnosisText,
                            isMeasuring = isEcgMeasuring,
                            waveformVersion = ecgWaveformVersion,
                            onStartClick = { startEcg() },
                            onStopClick = { stopEcg() },
                            onBackClick = { showingEcgScreen = false },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        when (selectedTab) {
                            0 -> DashboardScreen(
                                userProfile = userProfile,
                                stepGoal = stepGoal,
                                statusText = statusText,
                                batteryText = batteryText,
                                heartRateText = heartRateText,
                                spo2Text = spo2Text,
                                pbgText = pbgText,
                                stepsText = stepsText,
                                sleepText = sleepText,
                                temperatureText = temperatureText,
                                bloodPressureText = bloodPressureText,
                                stressText = stressText,
                                isScanning = isScanning,
                                isConnected = isConnected,
                                isDarkTheme = isDarkTheme,
                                onToggleDarkTheme = { isDarkTheme = !isDarkTheme },
                                onScanClick = { startScan() },
                                onReadBatteryClick = { readBattery() },
                                onReadHeartRateClick = { readHeartRate() },
                                onReadSpO2Click = { readSpO2() },
                                onReadPbgClick = { readPbg() },
                                onReadStepsClick = { readSteps() },
                                onReadSleepClick = { readSleep() },
                                onReadTemperatureClick = { readTemperature() },
                                onReadBloodPressureClick = { readBloodPressure() },
                                onReadStressClick = { readStress() },
                                onOpenEcgClick = { showingEcgScreen = true },
                                onOpenDeviceScreen = { selectedTab = 1 },
                                modifier = Modifier.padding(innerPadding)
                            )
                            1 -> DeviceScreen(
                                statusText = statusText,
                                batteryText = batteryText,
                                devices = devices,
                                isScanning = isScanning,
                                isConnected = isConnected,
                                onScanClick = { startScan() },
                                onDeviceClick = { _, mac -> connect(mac) },
                                onReadBatteryClick = { readBattery() },
                                onBackClick = { selectedTab = 0 },
                                onFindDeviceClick = { /* Handled internally by dialog */ },
                                onDisconnectClick = { disconnectRing() },
                                onCameraClick = { launchCamera() },
                                modifier = Modifier.padding(innerPadding)
                            )
                            2 -> HistoryScreen(
                                readings = historyReadings,
                                statusText = historyStatusText,
                                progress = historyProgress,
                                isSyncing = isSyncingHistory,
                                onSyncClick = { syncHistory() },
                                onBackClick = { selectedTab = 0 },
                                modifier = Modifier.padding(innerPadding)
                            )
                            3 -> ProfileScreen(
                                userProfile = userProfile,
                                isConnected = isConnected,
                                stepGoal = stepGoal,
                                onStepGoalChange = { 
                                    stepGoal = it 
                                    ProfileStore.saveStepGoal(this@MainActivity, it)
                                },
                                onSave = { email, height, weight, age, isMale, avatarUri, avatarPreset ->
                                    val p = UserProfile(email, height, weight, age, isMale, avatarUri, avatarPreset)
                                    ProfileStore.save(this, p)
                                    userProfile = p
                                    syncProfileToRing()
                                },
                                isDarkTheme = isDarkTheme,
                                onToggleDarkTheme = { isDarkTheme = !isDarkTheme },
                                onOpenDeviceScreen = { selectedTab = 1 },
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
        pbgText = ""
        stepsText = ""
        sleepText = ""
        temperatureText = ""
        bloodPressureText = ""
        stressText = ""
    }

    private fun connect(mac: String) {
        if (isScanning) {
            bluetoothManager.stopScan()
            isScanning = false
        }
        statusText = "Connecting to $mac..."
        bluetoothManager.connectDevice(mac) { success, message ->
            isScanning = false
            if (success) {
                statusText = "Scan finished. Ring connected ✓"
                isConnected = true
                syncProfileToRing()
            } else {
                statusText = "Scan finished. Connection failed (✗ $message)"
                isConnected = false
            }
        }
    }

    private fun disconnectRing() {
        bluetoothManager.disconnectDevice()
        isConnected = false
        statusText = "Disconnected by user"
        clearAllMetrics()
    }

    private fun launchCamera() {
        try {
            val intent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivity(intent)
        } catch (e: Exception) {
            statusText = "Camera open failed: ${e.message}"
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
            batteryText = if (success) message else "Failed: $message"
        }
    }

    private fun readHeartRate() {
        heartRateText = "Reading heart rate..."
        bluetoothManager.readHeartRate { success, _, message ->
            heartRateText = if (success) message else "Failed: $message"
        }
    }

    private fun readSpO2() {
        spo2Text = "Reading SpO2..."
        bluetoothManager.readSpO2 { success, _, message ->
            spo2Text = if (success) message else "Failed: $message"
        }
    }

    private fun readPbg() {
        pbgText = "Reading PBG..."
        bluetoothManager.readPbg { success, _, message ->
            pbgText = if (success) message else "Failed: $message"
        }
    }

    private fun readSteps() {
        stepsText = "Reading steps..."
        bluetoothManager.readSteps { success, _, message ->
            stepsText = if (success) message else "Failed: $message"
        }
    }

    private fun readSleep() {
        sleepText = "Reading sleep..."
        bluetoothManager.readSleep { success, summary, message ->
            sleepText = if (success) message else "Failed: $message"
            Log.d(TAG, "Sleep result: success=$success summary=$summary")
        }
    }

    private fun readTemperature() {
        temperatureText = "Reading temperature..."
        bluetoothManager.readTemperature { success, temp, message ->
            temperatureText = if (success) message else "Failed: $message"
            Log.d(TAG, "Temperature result: success=$success temp=$temp")
        }
    }

    private fun readBloodPressure() {
        bloodPressureText = "Reading blood pressure..."
        bluetoothManager.readBloodPressure { success, systolic, diastolic, message ->
            bloodPressureText = if (success) message else "Failed: $message"
            Log.d(TAG, "BP result: success=$success sys=$systolic dia=$diastolic")
        }
    }

    private fun readStress() {
        stressText = "Reading stress..."
        bluetoothManager.readStress { success, level, message ->
            stressText = if (success) message else "Failed: $message"
            Log.d(TAG, "Stress result: success=$success level=$level")
        }
    }

    // --- ECG ---

    private fun startEcg() {
        ecgStatusText = "Starting ECG..."
        ecgProgress = 0f
        ecgHeartRate = 0
        ecgWaveform.clear()
        ecgResultSummary = ""
        ecgDiagnosisText = ""
        isEcgMeasuring = true

        bluetoothManager.readEcg(
            onInfo = { info ->
                runOnUiThread {
                    Log.d(TAG, "ECG info: freq=${info.frequency}, draw=${info.drawFrequency}")
                }
            },
            onState = { state ->
                runOnUiThread {
                    ecgProgress = state.progress / 100f
                    ecgHeartRate = state.hr1
                    ecgWearOk = state.wear == 1
                    ecgStatusText = if (state.wear == 1) {
                        "✓ Measuring... ${state.progress}%"
                    } else {
                        "⚠ Poor contact — press firmly with two fingers"
                    }
                }
            },
            onAdc = { ch1, _ ->
                runOnUiThread {
                    synchronized(ecgWaveform) {
                        ecgWaveform.addAll(ch1.toList())
                        val overflow = ecgWaveform.size - 800
                        if (overflow > 0) {
                            ecgWaveform.subList(0, overflow).clear()
                        }
                        ecgWaveformVersion++
                    }
                }
            },
            onResult = { result ->
                runOnUiThread {
                    Log.d(TAG, "ECG result: $result")
                    ecgResultSummary = buildString {
                        append("Duration: ${result.duration}s  •  HR: ${result.aveHeart} bpm")
                        if (result.aveHrv > 0) append("\nHRV: ${result.aveHrv} ms")
                        if (result.aveQT > 0) append("\nQT: ${result.aveQT} ms")
                    }
                }
            },
            onDiagnosis = { diagnosis ->
                runOnUiThread {
                    Log.d(TAG, "ECG diagnosis: $diagnosis")
                    ecgDiagnosisText = buildDiagnosisText(diagnosis)
                    isEcgMeasuring = false
                    ecgStatusText = "✓ Complete"
                }
            },
            onError = { message ->
                runOnUiThread {
                    Log.e(TAG, "ECG error: $message")
                    ecgStatusText = "✗ $message"
                    isEcgMeasuring = false
                }
            }
        )
    }

    private fun stopEcg() {
        bluetoothManager.stopEcg()
        isEcgMeasuring = false
        ecgStatusText = "Stopped"
    }

    private fun buildDiagnosisText(diagnosis: EcgDiagnosis): String {
        val flags = mutableListOf<String>()
        val arr = diagnosis.diagnosis8
        if (arr != null) {
            val labels = listOf(
                "Normal", "Bradycardia", "Tachycardia", "Arrhythmia",
                "PVC", "AFib", "ST change", "Unknown"
            )
            arr.forEachIndexed { i, v ->
                if (v == 1 && i < labels.size) flags.add(labels[i])
            }
        }
        val hr = diagnosis.heartRate
        val hrv = diagnosis.hrv

        return buildString {
            if (hr > 0) append("HR: $hr bpm\n")
            if (hrv > 0) append("HRV: $hrv ms\n")
            if (flags.isNotEmpty()) append("Flags: ${flags.joinToString(", ")}")
            else append("No abnormalities detected")
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
                    showSyncSuccessAnimation = true
                } else {
                    historyStatusText = "✗ $message"
                }
            }
        )
    }
}

@Composable
private fun BottomNavBar(
    selectedTab: Int,
    isConnected: Boolean,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        Triple(0, "Health", R.drawable.ic_health_nav),
        Triple(1, "Device", R.drawable.ic_smart_ring),
        Triple(2, "History", R.drawable.ic_history_nav),
        Triple(3, "Profile", R.drawable.ic_profile_nav)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 10.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                contentColor = Primary,
                tonalElevation = 0.dp
            ) {
                items.forEach { (index, label, iconRes) ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabSelected(index) },
                        icon = {
                            Box {
                                Icon(
                                    painter = painterResource(iconRes),
                                    contentDescription = label,
                                    tint = if (isSelected) Primary else TextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                if (index == 1 && isConnected) {
                                    Surface(
                                        shape = CircleShape,
                                        color = StepsGreen,
                                        modifier = Modifier
                                            .size(8.dp)
                                            .align(Alignment.TopEnd)
                                    ) {}
                                }
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Primary else TextSecondary
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Primary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    }
}
