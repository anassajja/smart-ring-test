package com.luffy.smartringtest.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TestScreen(
    devices: List<Pair<String, String>>,
    statusText: String,
    // Metric display lines
    batteryText: String,
    heartRateText: String,
    spo2Text: String,
    stepsText: String,
    sleepText: String,
    temperatureText: String,
    bloodPressureText: String,
    stressText: String,
    hrvText: String,
    fatigueText: String,
    // UI state
    isScanning: Boolean,
    isConnected: Boolean,
    // Callbacks
    onScanClick: () -> Unit,
    onDeviceClick: (name: String, mac: String) -> Unit,
    onReadBatteryClick: () -> Unit,
    onReadHeartRateClick: () -> Unit,
    onReadSpO2Click: () -> Unit,
    onReadStepsClick: () -> Unit,
    onReadSleepClick: () -> Unit,
    onReadTemperatureClick: () -> Unit,
    onReadBloodPressureClick: () -> Unit,
    onReadStressClick: () -> Unit,
    onReadHrvClick: () -> Unit,
    onReadFatigueClick: () -> Unit,
    onOpenHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Smart Ring Test",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(12.dp))

        Text(text = statusText, style = MaterialTheme.typography.bodyLarge)

        // Metric results
        MetricLine(batteryText)
        MetricLine(heartRateText)
        MetricLine(spo2Text)
        MetricLine(stepsText)
        MetricLine(sleepText)
        MetricLine(temperatureText)
        MetricLine(bloodPressureText)
        MetricLine(stressText)
        MetricLine(hrvText)
        MetricLine(fatigueText)

        Spacer(Modifier.height(12.dp))

        // Scan button
        Button(
            onClick = onScanClick,
            enabled = !isScanning && !isConnected,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    isConnected -> "Connected"
                    isScanning -> "Scanning..."
                    else -> "Scan for Devices"
                }
            )
        }

        // Action buttons (only when connected)
        if (isConnected) {
            Spacer(Modifier.height(12.dp))

            ActionButton("Read Battery", onReadBatteryClick)
            ActionButton("Read Heart Rate", onReadHeartRateClick)
            ActionButton("Read SpO2", onReadSpO2Click)
            ActionButton("Read Steps", onReadStepsClick)
            ActionButton("Read Sleep", onReadSleepClick)
            ActionButton("Read Temperature", onReadTemperatureClick)
            ActionButton("Read Blood Pressure", onReadBloodPressureClick)
            ActionButton("Read Stress", onReadStressClick)
            // HRV hidden — not supported by this ring
            ActionButton("Read Fatigue", onReadFatigueClick)

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onOpenHistoryClick,
                modifier = Modifier.fillMaxWidth()
            ) { Text("📜 Open History") }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Devices found: ${devices.size}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        if (devices.isEmpty()) {
            Text("No devices yet. Tap Scan.", style = MaterialTheme.typography.bodyMedium)
        } else {
            devices.forEach { (name, mac) ->
                DeviceRow(
                    name = name,
                    mac = mac,
                    enabled = !isConnected,
                    onClick = { onDeviceClick(name, mac) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MetricLine(text: String) {
    if (text.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Spacer(Modifier.height(8.dp))
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) { Text(label) }
}

@Composable
private fun DeviceRow(
    name: String,
    mac: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyLarge)
                Text(mac, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                if (enabled) "Tap to connect" else "Connected",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}