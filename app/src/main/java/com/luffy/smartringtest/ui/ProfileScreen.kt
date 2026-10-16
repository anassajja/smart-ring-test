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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luffy.smartringtest.UserProfile
import com.luffy.smartringtest.ui.theme.Primary
import com.luffy.smartringtest.ui.theme.PrimaryLight
import com.luffy.smartringtest.ui.theme.StepsGreen
import com.luffy.smartringtest.ui.theme.StepsGreenBg

@Composable
fun ProfileScreen(
    userProfile: UserProfile? = null,
    onSave: (height: Int, weight: Int, age: Int, isMale: Boolean) -> Unit,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onOpenDeviceScreen: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var heightText by remember(userProfile) { mutableStateOf(userProfile?.height?.toString() ?: "175") }
    var weightText by remember(userProfile) { mutableStateOf(userProfile?.weight?.toString() ?: "70") }
    var ageText by remember(userProfile) { mutableStateOf(userProfile?.age?.toString() ?: "25") }
    var isMale by remember(userProfile) { mutableStateOf(userProfile?.isMale ?: true) }
    var useMetric by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf("") }
    var savedSuccessText by remember { mutableStateOf("") }

    // Automatic BMI Calculation
    val heightM = (heightText.toFloatOrNull() ?: 175f) / 100f
    val weightKg = weightText.toFloatOrNull() ?: 70f
    val bmi = if (heightM > 0) weightKg / (heightM * heightM) else 0f
    val bmiCategory = when {
        bmi <= 0 -> "--"
        bmi < 18.5f -> "Underweight"
        bmi < 25f -> "Normal Weight ✓"
        bmi < 30f -> "Overweight"
        else -> "Obese"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Styled Header Widget
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Smart Ring OS • Profile & Settings",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Personal Health Profile",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Text(
                    text = "Used by the ring to compute step stride, calories & sleep stats",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Avatar & BMI Health Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isMale) "👨‍💼" else "👩‍💼", fontSize = 32.sp)
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (isMale) "Male Profile" else "Female Profile",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "${heightText} cm • ${weightText} kg • ${ageText} yrs",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StepsGreenBg
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "BMI %.1f".format(bmi),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = StepsGreen,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = bmiCategory,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = StepsGreen,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        // Device Management Quick Link
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenDeviceScreen() },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📡", fontSize = 22.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Manage Bluetooth Ring",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Pair new devices, view battery level & RSSI signal",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                Text("➔", fontSize = 18.sp, color = Primary)
            }
        }

        // Biometrics Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "📋 Biometric Parameters",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it.filter { c -> c.isDigit() } },
                    label = { Text("Height (${if (useMetric) "cm" else "in"})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it.filter { c -> c.isDigit() } },
                    label = { Text("Weight (${if (useMetric) "kg" else "lbs"})") },
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

        // Appearance & Unit Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "⚙️ App Preferences",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Appearance Theme",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ChipButton(
                        label = "Light Mode ☀️",
                        selected = !isDarkTheme,
                        onClick = { if (isDarkTheme) onToggleDarkTheme() },
                        modifier = Modifier.weight(1f)
                    )
                    ChipButton(
                        label = "Dark Mode 🌙",
                        selected = isDarkTheme,
                        onClick = { if (!isDarkTheme) onToggleDarkTheme() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Unit System",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ChipButton(
                        label = "Metric (cm / kg)",
                        selected = useMetric,
                        onClick = { useMetric = true },
                        modifier = Modifier.weight(1f)
                    )
                    ChipButton(
                        label = "Imperial (in / lbs)",
                        selected = !useMetric,
                        onClick = { useMetric = false },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipButton(
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
