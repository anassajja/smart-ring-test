package com.luffy.smartringtest.ui

import java.util.Calendar
import androidx.compose.foundation.background
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luffy.smartringtest.ui.theme.BatteryGreen
import com.luffy.smartringtest.ui.theme.BpPink
import com.luffy.smartringtest.ui.theme.BpPinkBg
import com.luffy.smartringtest.ui.theme.HeartRed
import com.luffy.smartringtest.ui.theme.HeartRedBg
import com.luffy.smartringtest.ui.theme.OxygenBlue
import com.luffy.smartringtest.ui.theme.OxygenBlueBg
import com.luffy.smartringtest.ui.theme.Primary
import com.luffy.smartringtest.ui.theme.SleepPurple
import com.luffy.smartringtest.ui.theme.SleepPurpleBg
import com.luffy.smartringtest.ui.theme.StepsGreen
import com.luffy.smartringtest.ui.theme.StepsGreenBg
import com.luffy.smartringtest.ui.theme.StressYellow
import com.luffy.smartringtest.ui.theme.StressYellowBg
import com.luffy.smartringtest.ui.theme.TempOrange
import com.luffy.smartringtest.ui.theme.TempOrangeBg
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    statusText: String,
    batteryText: String,
    heartRateText: String,
    spo2Text: String,
    stepsText: String,
    sleepText: String,
    temperatureText: String,
    bloodPressureText: String,
    stressText: String,
    isScanning: Boolean,
    isConnected: Boolean,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onScanClick: () -> Unit,
    onReadBatteryClick: () -> Unit,
    onReadHeartRateClick: () -> Unit,
    onReadSpO2Click: () -> Unit,
    onReadStepsClick: () -> Unit,
    onReadSleepClick: () -> Unit,
    onReadTemperatureClick: () -> Unit,
    onReadBloodPressureClick: () -> Unit,
    onReadStressClick: () -> Unit,
    onOpenEcgClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Creative Dashboard Title & Greeting Widget
        DashboardHeaderWidget(
            isDarkTheme = isDarkTheme,
            onToggleDarkTheme = onToggleDarkTheme
        )

        // Top Device Header Card
        RingDeviceCard(
            statusText = statusText,
            batteryText = batteryText,
            isScanning = isScanning,
            isConnected = isConnected,
            onScanClick = onScanClick,
            onReadBatteryClick = onReadBatteryClick
        )

        // Activity Overview Card
        ActivityCard(
            stepsText = stepsText,
            isConnected = isConnected,
            onReadStepsClick = onReadStepsClick
        )

        // Health Metrics Title
        Text(
            text = "Health Metrics",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        // Metric Cards Grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthCard(
                    icon = "❤️",
                    title = "Heart Rate",
                    value = heartRateText.ifEmpty { "-- bpm" },
                    accentColor = HeartRed,
                    backgroundColor = HeartRedBg,
                    buttonLabel = "Measure",
                    enabled = isConnected,
                    onClick = onReadHeartRateClick,
                    modifier = Modifier.weight(1f)
                )

                HealthCard(
                    icon = "🩸",
                    title = "Blood Oxygen",
                    value = spo2Text.ifEmpty { "-- %" },
                    accentColor = OxygenBlue,
                    backgroundColor = OxygenBlueBg,
                    buttonLabel = "Measure",
                    enabled = isConnected,
                    onClick = onReadSpO2Click,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthCard(
                    icon = "😴",
                    title = "Sleep",
                    value = sleepText.ifEmpty { "--" },
                    accentColor = SleepPurple,
                    backgroundColor = SleepPurpleBg,
                    buttonLabel = "Read",
                    enabled = isConnected,
                    onClick = onReadSleepClick,
                    modifier = Modifier.weight(1f)
                )

                HealthCard(
                    icon = "🌡️",
                    title = "Temperature",
                    value = temperatureText.ifEmpty { "-- °C" },
                    accentColor = TempOrange,
                    backgroundColor = TempOrangeBg,
                    buttonLabel = "Measure",
                    enabled = isConnected,
                    onClick = onReadTemperatureClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HealthCard(
                    icon = "💓",
                    title = "Blood Pressure",
                    value = bloodPressureText.ifEmpty { "--/-- mmHg" },
                    accentColor = BpPink,
                    backgroundColor = BpPinkBg,
                    buttonLabel = "Measure",
                    enabled = isConnected,
                    onClick = onReadBloodPressureClick,
                    modifier = Modifier.weight(1f)
                )

                HealthCard(
                    icon = "😰",
                    title = "Stress Level",
                    value = stressText.ifEmpty { "--" },
                    accentColor = StressYellow,
                    backgroundColor = StressYellowBg,
                    buttonLabel = "Measure",
                    enabled = isConnected,
                    onClick = onReadStressClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ECG Banner Card
        EcgBannerCard(
            isConnected = isConnected,
            onOpenEcgClick = onOpenEcgClick
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DashboardHeaderWidget(
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good Morning ☀️"
        in 12..16 -> "Good Afternoon 🌤️"
        in 17..21 -> "Good Evening 🌙"
        else -> "Good Night 🌌"
    }

    val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    val dateString = dateFormat.format(calendar.time)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Smart Ring OS • Pro",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Text(
                    text = "Your daily health vitals at a glance",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Theme Toggle Button
            Surface(
                onClick = onToggleDarkTheme,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isDarkTheme) "☀️" else "🌙",
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RingDeviceCard(
    statusText: String,
    batteryText: String,
    isScanning: Boolean,
    isConnected: Boolean,
    onScanClick: () -> Unit,
    onReadBatteryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isConnected) StepsGreenBg else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isConnected) "💍" else "⚡", fontSize = 22.sp)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isConnected) "Smart Ring Connected" else "No Ring Connected",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (isConnected) StepsGreenBg else HeartRedBg
                ) {
                    Text(
                        text = if (isConnected) "Active" else "Offline",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isConnected) StepsGreen else HeartRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            if (isConnected) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = batteryText.ifEmpty { "Battery: Tap to read" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = BatteryGreen,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    OutlinedButton(
                        onClick = onReadBatteryClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Check Battery")
                    }
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onScanClick,
                    enabled = !isScanning,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isScanning) "Scanning for Ring..." else "Connect Smart Ring")
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(
    stepsText: String,
    isConnected: Boolean,
    onReadStepsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StepsGreenBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚶", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Daily Activity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StepsGreen
                        )
                    )
                }

                if (isConnected) {
                    Button(
                        onClick = onReadStepsClick,
                        colors = ButtonDefaults.buttonColors(containerColor = StepsGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Update Steps", color = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = stepsText.ifEmpty { "Tap update to sync steps" },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = StepsGreen
                )
            )
        }
    }
}

@Composable
private fun HealthCard(
    icon: String,
    title: String,
    value: String,
    accentColor: Color,
    backgroundColor: Color,
    buttonLabel: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 18.sp)
                }

                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                maxLines = 2
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onClick,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = backgroundColor,
                    contentColor = accentColor
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    buttonLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun EcgBannerCard(
    isConnected: Boolean,
    onOpenEcgClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(HeartRedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📈", fontSize = 22.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = "ECG Monitor",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "Real-time electrocardiogram waveform",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Button(
                onClick = onOpenEcgClick,
                enabled = isConnected,
                colors = ButtonDefaults.buttonColors(containerColor = HeartRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start ECG")
            }
        }
    }
}
