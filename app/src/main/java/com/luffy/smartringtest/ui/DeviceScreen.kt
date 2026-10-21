package com.luffy.smartringtest.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luffy.smartringtest.R
import com.luffy.smartringtest.ui.theme.Primary

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
    onFindDeviceClick: () -> Unit,
    onDisconnectClick: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showFindDeviceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        
        Text(
            text = "Device",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        // Hero Device Card (H Ring Style)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Primary.copy(alpha = 0.15f), RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Primary
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_smart_ring),
                            contentDescription = "Device",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isConnected) "Smart Ring Linked ✓" else if (isScanning) "Searching Ring..." else "Smart Ring V80",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isConnected) "✨ Device Active & Synchronized" else if (isScanning) "📡 Scanning BLE signals..." else "⚡ Ready to Pair • C1:40:32...",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isConnected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isConnected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isConnected) "Connected" else "Disconnected",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isConnected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    if (isConnected) {
                        TextButton(onClick = onDisconnectClick) {
                            Text("Disconnect", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_update),
                            contentDescription = "Sync",
                            tint = Primary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onScanClick() }
                        )
                    }
                }
            }
        }

        if (batteryText.isNotEmpty() && isConnected) {
            Text(
                text = "Battery: ${batteryText.removePrefix("🔋").trim()}",
                style = MaterialTheme.typography.bodyMedium.copy(color = Primary, fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Scan & Device Detection Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isScanning) "Searching for Smart Rings..." else "Discovered Devices (${devices.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Button(
                        onClick = onScanClick,
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isScanning) "Scanning..." else "Scan Now")
                    }
                }

                if (isScanning) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painter = painterResource(R.drawable.ic_radar), contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Searching for nearby BLE smart rings...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (devices.isNotEmpty()) {
                    devices.forEachIndexed { index, (name, mac) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDeviceClick(name, mac) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = name.ifEmpty { "Smart Ring" },
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = mac,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Button(
                                onClick = { onDeviceClick(name, mac) },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Connect")
                            }
                        }
                        if (index < devices.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // Functions Section
        Text(
            text = "Functions",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsRow(
                    iconRes = R.drawable.ic_smart_ring,
                    title = "Find Device",
                    iconTint = Color(0xFFFBBF24),
                    onClick = { showFindDeviceDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(
                    iconRes = R.drawable.ic_profile_nav,
                    title = "Camera Shutter",
                    iconTint = Color(0xFF93C5FD),
                    onClick = onCameraClick
                )
            }
        }

        // Settings Section
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsRow(iconRes = R.drawable.ic_health_nav, title = "Health & Monitor", iconTint = Color(0xFFF87171), onClick = {})
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                SettingsRow(iconRes = R.drawable.ic_system, title = "System", iconTint = Color(0xFF94A3B8), onClick = {})
            }
        }
        
        Spacer(Modifier.height(24.dp))
    }

    if (showFindDeviceDialog) {
        AlertDialog(
            onDismissRequest = { showFindDeviceDialog = false },
            title = { Text("Find Device") },
            text = { Text("Remote locating and vibrating is not supported by this ring model's protocol. Ensure Bluetooth is on and the ring is nearby.") },
            confirmButton = {
                TextButton(onClick = { showFindDeviceDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    iconRes: Int,
    title: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.weight(1f)
        )
        Text("›", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
