package com.luffy.smartringtest.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luffy.smartringtest.ui.theme.BatteryGreen
import com.luffy.smartringtest.ui.theme.HeartRed
import com.luffy.smartringtest.ui.theme.HeartRedBg
import com.luffy.smartringtest.ui.theme.Primary
import com.luffy.smartringtest.ui.theme.StepsGreen
import com.luffy.smartringtest.ui.theme.StepsGreenBg

@Composable
fun DeviceScreen(
    statusText: String,
    batteryText: String,
    devices: List<Pair<String, String>>,
    isScanning: Boolean,
    isConnected: Boolean,
    onScanClick: () -> Unit,
    onDeviceClick: (name: String, mac: String) -> Unit,
    onReadBatteryClick: () -> Unit,
    onBackClick: () -> Unit,
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
        // Top Header Card
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
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = Primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Smart Ring OS • Device Center",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Device Management",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    Text(
                        text = "Pair, connect and check Bluetooth ring status",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Back")
                }
            }
        }

        if (isConnected) {
            // Connected Ring Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(StepsGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💍", fontSize = 42.sp)
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Smart Ring Pro Connected",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    val cleanBattery = batteryText.removePrefix("🔋").trim()
                    val displayBattery = if (cleanBattery.isNotEmpty()) cleanBattery else "Tap to check"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StepsGreenBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔋", fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = displayBattery,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = BatteryGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Primary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚡", fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "BLE Active",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = onReadBatteryClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Check Battery Status")
                    }
                }
            }
        } else {
            // Radar Scanning Ripple Animation Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RadarAnimation(isScanning = isScanning)

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = if (isScanning) "Searching for Smart Rings..." else "Tap to scan for nearby rings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = onScanClick,
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isScanning) "Scanning..." else "Scan for Smart Rings")
                    }
                }
            }
        }

        // Discovered Devices List Card
        if (devices.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Discovered Rings (${devices.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    devices.forEach { (name, mac) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isConnected) { onDeviceClick(name, mac) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("💍", fontSize = 24.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = name.ifEmpty { "Smart Ring" },
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        )
                                        Text(
                                            text = mac,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        Text(
                                            text = "📶 Signal Strength: Strong (-62 dBm)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onDeviceClick(name, mac) },
                                    enabled = !isConnected,
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(if (isConnected) "Connected" else "Connect")
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RadarAnimation(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isScanning) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = (size.minDimension / 2) * pulseProgress
                val alpha = (1f - pulseProgress).coerceIn(0f, 1f)
                drawCircle(
                    color = Color(0xFF0DB39E).copy(alpha = alpha * 0.4f),
                    radius = radius
                )
                drawCircle(
                    color = Color(0xFF0DB39E).copy(alpha = (alpha * 0.2f).coerceIn(0f, 1f)),
                    radius = radius * 0.6f
                )
            }
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("📡", fontSize = 32.sp)
        }
    }
}
