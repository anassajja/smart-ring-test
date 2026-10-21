package com.luffy.smartringtest.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import java.io.File
import java.io.FileOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.luffy.smartringtest.R
import com.luffy.smartringtest.UserProfile

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userProfile: UserProfile? = null,
    isConnected: Boolean = false,
    onSave: (email: String, height: Int, weight: Int, age: Int, isMale: Boolean, avatarUri: String?, avatarPreset: String) -> Unit,
    stepGoal: Int = 8000,
    onStepGoalChange: (Int) -> Unit = {},
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onOpenDeviceScreen: () -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf("main") }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFF7F9FA))) {
        Crossfade(targetState = currentScreen) { screen ->
            when (screen) {
                "main" -> ProfileMainView(
                    userProfile = userProfile,
                    stepGoal = stepGoal,
                    onSave = onSave,
                    onNavigate = { currentScreen = it }
                )
                "account" -> AccountSubScreen(
                    email = userProfile?.email ?: "anass.ajja2020@gmail.com",
                    onBack = { currentScreen = "main" }
                )
                "data_services" -> DataServicesSubScreen(onBack = { currentScreen = "main" })
                "permissions" -> PermissionsSubScreen(onBack = { currentScreen = "main" })
                "feedback" -> FeedbackSubScreen(onBack = { currentScreen = "main" })
                "about" -> AboutSubScreen(onBack = { currentScreen = "main" }, onNavigate = { currentScreen = it })
                "goals" -> GoalsSubScreen(
                    stepGoal = stepGoal,
                    onStepGoalChange = onStepGoalChange,
                    onBack = { currentScreen = "main" }
                )
                "units" -> UnitsSubScreen(onBack = { currentScreen = "main" })
                "privacy" -> PrivacyPolicySubScreen(onBack = { currentScreen = "about" })
                "terms" -> TermsOfUseSubScreen(onBack = { currentScreen = "about" })
            }
        }
    }
}

// --- Main Profile View ---

@Composable
private fun ProfileMainView(
    userProfile: UserProfile?,
    stepGoal: Int,
    onSave: (email: String, height: Int, weight: Int, age: Int, isMale: Boolean, avatarUri: String?, avatarPreset: String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }

    var emailText by remember(userProfile) { mutableStateOf(userProfile?.email ?: "anass.ajja2020@gmail.com") }
    var heightText by remember(userProfile) { mutableStateOf(userProfile?.height?.toString() ?: "175") }
    var weightText by remember(userProfile) { mutableStateOf(userProfile?.weight?.toString() ?: "70") }
    var ageText by remember(userProfile) { mutableStateOf(userProfile?.age?.toString() ?: "25") }
    var isMale by remember(userProfile) { mutableStateOf(userProfile?.isMale ?: true) }
    var avatarUri by remember(userProfile) { mutableStateOf(userProfile?.avatarUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val file = File(context.filesDir, "profile_avatar.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                avatarUri = Uri.fromFile(file).toString()
                onSave(emailText, heightText.toIntOrNull()?:175, weightText.toIntOrNull()?:70, ageText.toIntOrNull()?:25, isMale, avatarUri, "")
            } catch (e: Exception) {
                avatarUri = uri.toString()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.Normal
                )
            )
            Icon(
                painter = painterResource(R.drawable.ic_qrcode),
                contentDescription = "QR Code",
                tint = Color.Black,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { showQrDialog = true }
            )
        }

        // Avatar & Email Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .clickable { photoPickerLauncher.launch("image/*") },
                color = Color.Transparent,
                border = BorderStroke(3.dp, Color(0xFF55D1C9))
            ) {
                if (!avatarUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_profile_nav),
                        contentDescription = null,
                        tint = Color(0xFF55D1C9),
                        modifier = Modifier.padding(22.dp).fillMaxSize()
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = emailText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.clickable { showEditDialog = true }
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Click the profile picture to edit personal information",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.clickable { showEditDialog = true }
            )
            Spacer(Modifier.height(8.dp))
        }

        // Health Sharing Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "My Smart Ring Health Vitals: $emailText")
                    }
                    context.startActivity(Intent.createChooser(intent, "Health Sharing"))
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Health sharing", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text("Family health guardianship", fontSize = 13.sp, color = Color.Gray)
                }
                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    contentDescription = null,
                    tint = Color(0xFF55D1C9),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Side-by-Side Unit and Goals Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .clickable { onNavigate("units") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_height),
                        contentDescription = null,
                        tint = Color(0xFF55D1C9),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Unit", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        Text("Edit units for v...", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp)
                    .clickable { onNavigate("goals") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_steps),
                        contentDescription = null,
                        tint = Color(0xFF55D1C9),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Goals", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        Text("Adjust your var...", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Settings List Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column {
                ProfileListRow(iconRes = R.drawable.ic_profile_nav, title = "Account", onClick = { onNavigate("account") })
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileListRow(iconRes = R.drawable.ic_system, title = "Data Services", onClick = { onNavigate("data_services") })
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileListRow(iconRes = R.drawable.ic_shield_permissions, title = "Permissions", onClick = { onNavigate("permissions") })
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileListRow(iconRes = R.drawable.ic_feedback, title = "Feedback", onClick = { onNavigate("feedback") })
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ProfileListRow(iconRes = R.drawable.ic_info, title = "About", onClick = { onNavigate("about") })
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    // Modal Edit Personal Information Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Personal Information") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = emailText,
                        onValueChange = { emailText = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Height (cm)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Weight (kg)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text("Age (years)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val h = heightText.toIntOrNull() ?: 175
                        val w = weightText.toIntOrNull() ?: 70
                        val a = ageText.toIntOrNull() ?: 25
                        onSave(emailText, h, w, a, isMale, avatarUri, "")
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF55D1C9))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("Profile QR Code") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        painter = painterResource(R.drawable.ic_qrcode),
                        contentDescription = null,
                        tint = Color(0xFF55D1C9),
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(emailText, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Text("Pairing Code: SR-${emailText.hashCode().toString().takeLast(6)}", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun ProfileListRow(iconRes: Int, title: String, onClick: () -> Unit) {
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
                tint = Color(0xFF55D1C9),
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(title, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Normal)
        }
        Text("›", fontSize = 20.sp, color = Color.LightGray)
    }
}

// --- Sub Screen: Account ---

@Composable
private fun AccountSubScreen(email: String, onBack: () -> Unit) {
    var selectedRegion by remember { mutableStateOf("United States (US)") }
    var showRegionDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var passwordResetSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", fontSize = 32.sp, color = Color.Black, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(16.dp))
            Text("Account", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRegionDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.ic_region), contentDescription = null, tint = Color(0xFF55D1C9), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Region", fontSize = 16.sp, color = Color.Black)
                    }
                    Text(selectedRegion, fontSize = 16.sp, color = Color.Gray)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.ic_email), contentDescription = null, tint = Color(0xFF55D1C9), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Email", fontSize = 16.sp, color = Color.Black)
                    }
                    Text(email, fontSize = 16.sp, color = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showResetPasswordDialog = true },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.ic_shield_permissions), contentDescription = null, tint = Color(0xFF55D1C9), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Reset Password", fontSize = 16.sp, color = Color.Black)
                }
                Text("›", fontSize = 20.sp, color = Color.LightGray)
            }
        }

        Spacer(Modifier.weight(1f))

        Text("Don't want to use anymore?", fontSize = 14.sp, color = Color(0xFFF87171), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF55D1C9)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Log Out", fontSize = 16.sp, color = Color.White)
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showRegionDialog) {
        val regions = listOf("United States (US)", "Europe (EU)", "Asia-Pacific (APAC)", "Global (Global)")
        AlertDialog(
            onDismissRequest = { showRegionDialog = false },
            title = { Text("Select Account Region") },
            text = {
                Column {
                    regions.forEach { r ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedRegion = r
                                    showRegionDialog = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(r, fontWeight = if (selectedRegion == r) FontWeight.Bold else FontWeight.Normal)
                            if (selectedRegion == r) Text("✓", color = Color(0xFF55D1C9))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRegionDialog = false }) { Text("Done") }
            }
        )
    }

    if (showResetPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            title = { Text("Reset Password") },
            text = {
                if (passwordResetSent) {
                    Text("Password reset instructions have been sent to $email.")
                } else {
                    Text("Click 'Send Link' to receive a password reset verification link sent to $email.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (passwordResetSent) showResetPasswordDialog = false else passwordResetSent = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF55D1C9))
                ) {
                    Text(if (passwordResetSent) "OK" else "Send Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPasswordDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// --- Sub Screen: Goals (With Interactive Goal Dialogs for Steps & Sleep) ---

@Composable
private fun GoalsSubScreen(stepGoal: Int, onStepGoalChange: (Int) -> Unit, onBack: () -> Unit) {
    var showStepGoalDialog by remember { mutableStateOf(false) }
    var showSleepGoalDialog by remember { mutableStateOf(false) }
    var selectedSleepGoal by remember { mutableStateOf("8.0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", fontSize = 32.sp, color = Color.Black, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(16.dp))
            Text("Goals", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }

        Spacer(Modifier.height(16.dp))
        Text("Track your daily goals with your phone and wearable device.", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Steps Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .clickable { showStepGoalDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Steps", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.align(Alignment.Start))
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF55D1C9).copy(alpha = 0.15f)
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.ic_footprints),
                                contentDescription = "Steps Goal",
                                tint = Color(0xFF55D1C9),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Text("0 /$stepGoal", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                }
            }

            // Sleep Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .clickable { showSleepGoalDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sleep", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.align(Alignment.Start))
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF818CF8).copy(alpha = 0.15f)
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.ic_sleep),
                                contentDescription = "Sleep Goal",
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Text("0.0 /${selectedSleepGoal} hours", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                }
            }
        }
    }

    // Step Goal Target Dialog
    if (showStepGoalDialog) {
        var tempGoal by remember { mutableFloatStateOf(stepGoal.toFloat()) }

        AlertDialog(
            onDismissRequest = { showStepGoalDialog = false },
            title = { Text("Daily Step Goal Target") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${tempGoal.toInt()} steps",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF55D1C9)
                        )
                    )

                    Slider(
                        value = tempGoal,
                        onValueChange = { tempGoal = (it / 500).toInt() * 500f },
                        valueRange = 1000f..30000f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF55D1C9), activeTrackColor = Color(0xFF55D1C9))
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(1000, 3000, 5000, 8000, 10000).forEach { goal ->
                                Surface(
                                    onClick = { tempGoal = goal.toFloat() },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (tempGoal.toInt() == goal) Color(0xFF55D1C9) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (goal >= 10000) "${goal/1000}k" else "$goal",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (tempGoal.toInt() == goal) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStepGoalChange(tempGoal.toInt())
                        showStepGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF55D1C9))
                ) {
                    Text("Set Step Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStepGoalDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Sleep Goal Target Dialog
    if (showSleepGoalDialog) {
        var tempSleep by remember { mutableFloatStateOf(selectedSleepGoal.toFloatOrNull() ?: 8.0f) }

        AlertDialog(
            onDismissRequest = { showSleepGoalDialog = false },
            title = { Text("Daily Sleep Target (Hours)") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "%.1f hours".format(tempSleep),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF818CF8)
                        )
                    )

                    Slider(
                        value = tempSleep,
                        onValueChange = { tempSleep = (it * 2).toInt() / 2f },
                        valueRange = 5.0f..12.0f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF818CF8), activeTrackColor = Color(0xFF818CF8))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(6.0f, 7.0f, 7.5f, 8.0f, 8.5f, 9.0f).forEach { h ->
                            Surface(
                                onClick = { tempSleep = h },
                                shape = RoundedCornerShape(8.dp),
                                color = if (tempSleep == h) Color(0xFF818CF8) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "%.1fh".format(h),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (tempSleep == h) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedSleepGoal = "%.1f".format(tempSleep)
                        showSleepGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF818CF8))
                ) {
                    Text("Set Sleep Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSleepGoalDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// --- Sub Screen: Units ---

@Composable
private fun UnitsSubScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FA))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("‹", fontSize = 32.sp, color = Color.Black, modifier = Modifier.clickable { onBack() })
                Spacer(Modifier.width(16.dp))
                Text("Unit", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            }
            Text("Save", fontSize = 16.sp, color = Color(0xFF55D1C9), modifier = Modifier.clickable { onBack() })
        }

        Spacer(Modifier.height(24.dp))
        UnitSectionTitle("Height / Distance")
        UnitSelectionCard(options = listOf("cm, km", "ft, mi"), selectedIndex = 1)

        Spacer(Modifier.height(16.dp))
        UnitSectionTitle("Weight")
        UnitSelectionCard(options = listOf("kg", "lbs"), selectedIndex = 0)

        Spacer(Modifier.height(16.dp))
        UnitSectionTitle("Body Temp.")
        UnitSelectionCard(options = listOf("°C", "°F"), selectedIndex = 0)

        Spacer(Modifier.height(16.dp))
        UnitSectionTitle("Blood Glucose")
        UnitSelectionCard(options = listOf("mmol/L", "mg/dL"), selectedIndex = 0)
    }
}

@Composable
private fun UnitSectionTitle(title: String) {
    Text(title, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun UnitSelectionCard(options: List<String>, selectedIndex: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            options.forEachIndexed { i, opt ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(opt, fontSize = 16.sp, color = Color.Black)
                    if (i == selectedIndex) Text("✓", color = Color(0xFF55D1C9), fontWeight = FontWeight.Bold)
                }
                if (i < options.size - 1) HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
            }
        }
    }
}

// --- Sub Screen: Data Services ---

@Composable
private fun DataServicesSubScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FA)).padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", fontSize = 32.sp, color = Color.Black, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(16.dp))
            Text("Data Services", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Sync with the Cloud", fontSize = 16.sp, color = Color.Black)
                    Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF55D1C9)))
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Status", fontSize = 16.sp, color = Color.Black)
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF55D1C9))) {
                        Text("Sync Now")
                    }
                }
            }
        }
    }
}

// --- Sub Screen: Permissions (Distinct Related Icons & Direct System Intent) ---

@Composable
private fun PermissionsSubScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val perms = listOf(
        Triple("Bluetooth", "For connecting wearable device.", R.drawable.ic_bluetooth_perm),
        Triple("Nearby Devices", "For searching for nearby wearable device.", R.drawable.ic_bluetooth_perm),
        Triple("Location", "For location-related services such as background workout route recording.", R.drawable.ic_location_perm),
        Triple("Notifications", "For pushing app-related notifications and maintaining connection stability.", R.drawable.ic_notification_perm),
        Triple("Notification Bar", "For accessing notification bar and pushing notifications.", R.drawable.ic_notification_perm),
        Triple("Battery Optimization", "For avoiding app being cleaned by system battery settings.", R.drawable.ic_bluetooth_perm),
        Triple("Camera", "For using photos as watch face, avatar or QR codes.", R.drawable.ic_camera_perm),
        Triple("Phone Info", "For accessing phone information and identifying model.", R.drawable.ic_phone_perm),
        Triple("Call Logs", "For pushing incoming call to device.", R.drawable.ic_phone_perm),
        Triple("Phone", "For muting or hanging up calls on your device.", R.drawable.ic_phone_perm),
        Triple("Contacts", "For displaying contact names on pushing incoming calls.", R.drawable.ic_phone_perm),
        Triple("Sports Data", "Used to record your steps and exercise tracks.", R.drawable.ic_sports_perm)
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FA)).padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", fontSize = 32.sp, color = Color.Black, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(16.dp))
            Text("Permissions", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
        Spacer(Modifier.height(16.dp))
        Text("Permissions below are only used while using related functions.", fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp))

        perms.forEach { (title, desc, iconRes) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(iconRes), contentDescription = null, tint = Color(0xFF55D1C9), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        Text(desc, fontSize = 12.sp, color = Color.Gray)
                    }
                    Text("›", fontSize = 20.sp, color = Color.LightGray)
                }
            }
        }
    }
}

// --- Sub Screen: Feedback ---

@Composable
private fun FeedbackSubScreen(onBack: () -> Unit) {
    var feedbackText by remember { mutableStateOf("") }
    var showSubmittedDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FA)).padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("‹", fontSize = 32.sp, color = Color.Black, modifier = Modifier.clickable { onBack() })
                Spacer(Modifier.width(16.dp))
                Text("Feedback", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            }
            Text(
                text = "Confirm",
                fontSize = 16.sp,
                color = if (feedbackText.isNotBlank()) Color(0xFF55D1C9) else Color.Gray,
                modifier = Modifier.clickable(enabled = feedbackText.isNotBlank()) {
                    showSubmittedDialog = true
                }
            )
        }
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = feedbackText,
            onValueChange = { if (it.length <= 1000) feedbackText = it },
            modifier = Modifier.fillMaxWidth().height(200.dp),
            placeholder = { Text("Please describe your feedback or report an issue...") }
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Support Email: hbandservice@outlook.com", fontSize = 12.sp, color = Color(0xFF55D1C9))
            Text("${feedbackText.length}/1000", fontSize = 12.sp, color = Color.Gray)
        }
    }

    if (showSubmittedDialog) {
        AlertDialog(
            onDismissRequest = {
                showSubmittedDialog = false
                onBack()
            },
            title = { Text("Feedback Received") },
            text = { Text("Thank you for your feedback! Our engineering team will review your report.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmittedDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF55D1C9))
                ) {
                    Text("OK")
                }
            }
        )
    }
}

// --- Sub Screen: About (Updated App Name "Smart Ring Test" & Logo) ---

@Composable
private fun AboutSubScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F9FA)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", fontSize = 32.sp, color = Color.Black, modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.width(16.dp))
            Text("About", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }

        Spacer(Modifier.height(40.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E293B),
            shadowElevation = 4.dp
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                Icon(
                    painter = painterResource(R.drawable.ic_ring_logo),
                    contentDescription = "App Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(72.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Smart Ring Test", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text("v0.1.0 (Beta Build 101)", fontSize = 14.sp, color = Color.Gray)

        Spacer(Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().clickable { onNavigate("privacy") }.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Privacy Policy", fontSize = 16.sp, color = Color.Black)
                    Text("›", fontSize = 20.sp, color = Color.LightGray)
                }
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                Row(modifier = Modifier.fillMaxWidth().clickable { onNavigate("terms") }.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Terms of Use", fontSize = 16.sp, color = Color.Black)
                    Text("›", fontSize = 20.sp, color = Color.LightGray)
                }
            }
        }
    }
}

// --- Sub Screen: Privacy Policy ---

@Composable
private fun PrivacyPolicySubScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("‹", fontSize = 32.sp, color = Color.Black, modifier = Modifier.clickable { onBack() })
        Spacer(Modifier.height(16.dp))
        Text("Smart Ring Test Privacy Policy", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text("Updated November 2026", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        Text("Introduction", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text("Smart Ring Test is a wearable health companion application designed for Bluetooth biometric telemetry recording and fitness tracking...", fontSize = 14.sp, color = Color.DarkGray)
    }
}

// --- Sub Screen: Terms of Use ---

@Composable
private fun TermsOfUseSubScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text("‹", fontSize = 32.sp, color = Color.Black, modifier = Modifier.clickable { onBack() })
        Spacer(Modifier.height(16.dp))
        Text("Terms of Service", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text("Latest update: November 2026", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        Text("Preamble", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text("Thank you for using Smart Ring Test and related wearable health services!", fontSize = 14.sp, color = Color.DarkGray)
    }
}
