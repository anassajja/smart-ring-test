package com.luffy.smartringtest.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luffy.smartringtest.UserProfile
import com.luffy.smartringtest.ui.theme.Primary
import com.luffy.smartringtest.ui.theme.PrimaryLight

@Composable
fun ProfileScreen(
    userProfile: UserProfile? = null,
    onSave: (height: Int, weight: Int, age: Int, isMale: Boolean) -> Unit,
    devices: List<Pair<String, String>> = emptyList(),
    isScanning: Boolean = false,
    isConnected: Boolean = false,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onDeviceClick: (name: String, mac: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var heightText by remember(userProfile) { mutableStateOf(userProfile?.height?.toString() ?: "175") }
    var weightText by remember(userProfile) { mutableStateOf(userProfile?.weight?.toString() ?: "70") }
    var ageText by remember(userProfile) { mutableStateOf(userProfile?.age?.toString() ?: "25") }
    var isMale by remember(userProfile) { mutableStateOf(userProfile?.isMale ?: true) }
    var errorText by remember { mutableStateOf("") }
    var savedSuccessText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Column {
            Text(
                text = "Device & Profile",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Ring connectivity, theme preferences and health metrics setup",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Appearance / Theme Preferences Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎨 Appearance Theme",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeChip(
                        label = "Light Mode ☀️",
                        selected = !isDarkTheme,
                        onClick = { if (isDarkTheme) onToggleDarkTheme() },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeChip(
                        label = "Dark Mode 🌙",
                        selected = isDarkTheme,
                        onClick = { if (!isDarkTheme) onToggleDarkTheme() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Ring Device Management Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📡 Ring Bluetooth Connection",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onScanClick,
                    enabled = !isScanning && !isConnected,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            isConnected -> "Connected to Ring"
                            isScanning -> "Scanning for Rings..."
                            else -> "Scan for Smart Rings"
                        }
                    )
                }

                if (devices.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Discovered Devices:",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(Modifier.height(8.dp))

                    devices.forEach { (name, mac) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isConnected) { onDeviceClick(name, mac) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        name.ifEmpty { "Unknown Ring" },
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        mac,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                Text(
                                    if (isConnected) "Connected" else "Tap to Connect",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Primary)
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }

        // Profile Setup Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "👤 User Profile",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Used by the ring to accurately compute step length, calories, and sleep statistics.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it.filter { c -> c.isDigit() } },
                    label = { Text("Height (cm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it.filter { c -> c.isDigit() } },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter { c -> c.isDigit() } },
                    label = { Text("Age (years)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Biological Sex",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SexChip(
                        label = "Male",
                        selected = isMale,
                        onClick = { isMale = true },
                        modifier = Modifier.weight(1f)
                    )
                    SexChip(
                        label = "Female",
                        selected = !isMale,
                        onClick = { isMale = false },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorText.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (savedSuccessText.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = savedSuccessText,
                        color = Primary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = {
                        val height = heightText.toIntOrNull()
                        val weight = weightText.toIntOrNull()
                        val age = ageText.toIntOrNull()

                        errorText = when {
                            height == null || height !in 55..255 -> "Height must be 55–255 cm"
                            weight == null || weight !in 25..255 -> "Weight must be 25–255 kg"
                            age == null || age !in 1..255 -> "Age must be 1–255"
                            else -> ""
                        }

                        if (errorText.isEmpty()) {
                            onSave(height!!, weight!!, age!!, isMale)
                            savedSuccessText = "✓ Profile saved & synced to ring!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save & Sync Profile")
                }
            }
        }
    }
}

@Composable
private fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (selected) "✓ $label" else label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun SexChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) PrimaryLight else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (selected) "✓ $label" else label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
