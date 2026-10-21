package com.luffy.smartringtest.ui

import androidx.compose.animation.Crossfade
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luffy.smartringtest.R

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
    var currentSubScreen by remember { mutableStateOf("main") }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF7F9FA))) {
        Crossfade(targetState = currentSubScreen) { screen ->
            when (screen) {
                "main" -> DeviceMainView(
                    statusText = statusText,
                    batteryText = batteryText,
                    devices = devices,
                    isScanning = isScanning,
                    isConnected = isConnected,
                    onScanClick = onScanClick,
                    onDeviceClick = onDeviceClick,
                    onReadBatteryClick = onReadBatteryClick,
                    onFindDeviceClick = onFindDeviceClick,
                    onDisconnectClick = onDisconnectClick,
                    onCameraClick = onCameraClick,
                    onNavigateSystem = { currentSubScreen = "system_info" }
                )
                "system_info" -> SystemInfoSubScreen(
                    batteryText = batteryText,
                    isConnected = isConnected,
                    onBack = { currentSubScreen = "main" },
                    onForgetDevice = {
                        onDisconnectClick()
                        currentSubScreen = "main"
                    }
                )
            }
        }
    }
}

// --- Main Device View (Exact H Ring Style Optimized) ---

@Composable
private fun DeviceMainView(
    statusText: String,
    batteryText: String,
    devices: List<Pair<String, String>>,
    isScanning: Boolean,
    isConnected: Boolean,
    onScanClick: () -> Unit,
    onDeviceClick: (name: String, mac: String) -> Unit,
    onReadBatteryClick: () -> Unit,
    onFindDeviceClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onCameraClick: () -> Unit,
    onNavigateSystem: () -> Unit
) {
    var showFindDeviceDialog by remember { mutableStateOf(false) }
    val cleanBattery = batteryText.removePrefix("🔋").trim()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        Text(
            text = "Device",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
        )

        // Hero Connection Widget (H Ring Optimized + Affirmative Phrases)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, if (isConnected) Color(0xFF55D1C9).copy(0.3f) else Color(0xFFE0F7FA), RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isConnected) Color(0xFF55D1C9) else Color(0xFFE2E8F0)
                        ) {
                            Box(modifier = Modifier.padding(10.dp)) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_smart_ring),
                                    contentDescription = "Smart Ring",
                                    tint = if (isConnected) Color.White else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        Column {
                            Text(
                                text = if (isConnected) "Smart Ring Linked ✓" else if (isScanning) "Searching Ring..." else "Smart Ring V80",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 18.sp
                                )
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (isConnected) "✨ Device Active & Synchronized" else if (isScanning) "📡 Scanning BLE signals..." else "⚡ Ready to Pair • C1:40:32...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isConnected) Color(0xFF0DB39E) else Color.Gray,
                                    fontWeight = if (isConnected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }

                    if (isConnected && cleanBattery.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF55D1C9).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🔋 $cleanBattery",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF0DB39E),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    } else if (!isConnected) {
                        Icon(
                            painter = painterResource(R.drawable.ic_update),
                            contentDescription = "Sync",
                            tint = Color(0xFF55D1C9),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onScanClick() }
                        )
                    }
                }

                // Action Buttons Row inside Hero Card
                if (isConnected) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReadBatteryClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Check Battery")
                        }
                        Button(
                            onClick = onDisconnectClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Disconnect", color = Color.White)
                        }
                    }
                } else if (!isScanning) {
                    Button(
                        onClick = onScanClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF55D1C9)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Scan & Pair Ring", color = Color.White)
                    }
                }
            }
        }

        // Functions Section
        Text(
            text = "Functions",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column {
                DeviceRowItem(
                    iconRes = R.drawable.ic_smart_ring,
                    title = "Find Device",
                    iconTint = Color(0xFFFBBF24),
                    onClick = { showFindDeviceDialog = true }
                )
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                DeviceRowItem(
                    iconRes = R.drawable.ic_profile_nav,
                    title = "Camera",
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
                color = Color.Gray
            )
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column {
                DeviceRowItem(
                    iconRes = R.drawable.ic_health_nav,
                    title = "Health & Monitor",
                    iconTint = Color(0xFFF87171),
                    onClick = { }
                )
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                DeviceRowItem(
                    iconRes = R.drawable.ic_system,
                    title = "System",
                    iconTint = Color(0xFF94A3B8),
                    onClick = onNavigateSystem
                )
            }
        }

        // Discovered Devices List
        if (isScanning || devices.isNotEmpty()) {
            Text(
                text = "Discovered Devices",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isScanning) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(painter = painterResource(R.drawable.ic_radar), contentDescription = null, tint = Color(0xFF55D1C9), modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Searching for nearby BLE smart rings...", color = Color.Gray, fontSize = 14.sp)
                        }
                    }

                    devices.forEachIndexed { index, (name, mac) ->
                        val isThisConnected = isConnected && (mac.equals("C1:40:32:66:66:B0", ignoreCase = true) || index == 0)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = name.ifEmpty { "V80" },
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                                )
                                Text(
                                    text = mac,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = Color.Gray)
                                )
                            }

                            Button(
                                onClick = { if (!isThisConnected) onDeviceClick(name, mac) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isThisConnected) Color(0xFF55D1C9) else Color(0xFF55D1C9)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (isThisConnected) "Connected ✓" else "Connect", color = Color.White)
                            }
                        }
                        if (index < devices.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (showFindDeviceDialog) {
        AlertDialog(
            onDismissRequest = { showFindDeviceDialog = false },
            title = { Text("Find Device") },
            text = { Text("Remote vibrating or buzzing is not supported by this ring model's protocol. Ensure Bluetooth is enabled and the ring is nearby.") },
            confirmButton = {
                TextButton(onClick = { showFindDeviceDialog = false }) { Text("OK") }
            }
        )
    }
}

// --- Sub Screen: System Device Info (Exact H Ring About Style) ---

@Composable
private fun SystemInfoSubScreen(
    batteryText: String,
    isConnected: Boolean,
    onBack: () -> Unit,
    onForgetDevice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹",
                fontSize = 32.sp,
                color = Color.Black,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = "About",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

        Spacer(Modifier.height(24.dp))

        // Card 1: Device Name
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF55D1C9).copy(alpha = 0.15f)
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_smart_ring),
                            contentDescription = null,
                            tint = Color(0xFF55D1C9),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "V80",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Card 2: MAC & Firmware Version
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("MAC", fontSize = 16.sp, color = Color.Black)
                    Text("C1:40:32:66:66:B0", fontSize = 16.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Firmware Version", fontSize = 16.sp, color = Color.Black)
                    Text("00.18.03.00-8609", fontSize = 16.sp, color = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Card 3: Check for updates
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Check for updates", fontSize = 16.sp, color = Color.Black)
                Text("›", fontSize = 20.sp, color = Color.LightGray)
            }
        }

        Spacer(Modifier.weight(1f))

        // Red Forget Device Button (Exact H Ring Style)
        Button(
            onClick = onForgetDevice,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Forget Device", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DeviceRowItem(
    iconRes: Int,
    title: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Normal)
        }
        Text("›", fontSize = 20.sp, color = Color.LightGray)
    }
}
