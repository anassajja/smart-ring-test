package com.luffy.smartringtest.ui

import java.util.Calendar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.luffy.smartringtest.R
import com.luffy.smartringtest.UserProfile
import com.luffy.smartringtest.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    userProfile: UserProfile? = null,
    stepGoal: Int = 10000,
    statusText: String,
    batteryText: String,
    heartRateText: String,
    spo2Text: String,
    pbgText: String = "",
    stepsText: String,
    sleepText: String,
    temperatureText: String,
    bloodPressureText: String,
    stressText: String,
    isScanning: Boolean,
    isConnected: Boolean,
    isDarkTheme: Boolean,
    showSyncSuccessAnimation: Boolean = false,
    onDismissSyncAnimation: () -> Unit = {},
    onToggleDarkTheme: () -> Unit,
    onScanClick: () -> Unit,
    onReadBatteryClick: () -> Unit,
    onReadHeartRateClick: () -> Unit,
    onReadSpO2Click: () -> Unit,
    onReadPbgClick: () -> Unit = {},
    onReadStepsClick: () -> Unit,
    onReadSleepClick: () -> Unit,
    onReadTemperatureClick: () -> Unit,
    onReadBloodPressureClick: () -> Unit,
    onReadStressClick: () -> Unit,
    onOpenEcgClick: () -> Unit,
    onOpenDeviceScreen: () -> Unit = {},
    bmiText: String = "",
    metText: String = "",
    edaText: String = "",
    bloodComponentsText: String = "",
    hrvText: String = "",
    onReadHrvClick: () -> Unit = {},
    onHealthGlanceClick: () -> Unit = {},
    showHealthGlanceReport: Boolean = false,
    healthGlanceReportText: String = "",
    onDismissHealthGlanceReport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMmol by remember { mutableStateOf(true) }
    var showAllMetricsDialog by remember { mutableStateOf(false) }
    var showBmiDialog by remember { mutableStateOf(false) }

    // Auto-dismiss sync animation after 3.5s
    LaunchedEffect(showSyncSuccessAnimation) {
        if (showSyncSuccessAnimation) {
            delay(3500)
            onDismissSyncAnimation()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header (Greeting + Share + Avatar)
            DashboardHeader(userProfile, isDarkTheme, onToggleDarkTheme)

            // 2. Dedicated Standalone Modern Daily Steps Widget
            ModernDailyStepsWidget(
                stepsText = stepsText,
                stepGoal = stepGoal,
                onClick = onReadStepsClick
            )

            // 3. Featured Live Biometrics (Wide ECG Oscilloscope & Cardio Split Cards)
            FeaturedLiveBiometricsCard(
                heartRateText = heartRateText,
                hrvText = hrvText,
                onReadHeartRateClick = onReadHeartRateClick,
                onReadHrvClick = onReadHrvClick,
                onOpenEcgClick = onOpenEcgClick
            )

            // 4. Bento Box Health Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SleepSummaryCard(
                    sleepText = sleepText,
                    onClick = onReadSleepClick,
                    modifier = Modifier.weight(1f)
                )
                BentoMetricCard(
                    iconRes = R.drawable.ic_blood_oxygen,
                    title = "Blood Oxygen",
                    value = spo2Text.ifEmpty { "-- %" },
                    accentColor = OxygenBlue,
                    onClick = onReadSpO2Click,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BentoMetricCard(
                    iconRes = R.drawable.ic_blood_pressure,
                    title = "Blood Pressure",
                    value = bloodPressureText.ifEmpty { "-- mmHg" },
                    accentColor = BpPink,
                    onClick = onReadBloodPressureClick,
                    modifier = Modifier.weight(1f)
                )
                
                val pbgDisplay = when {
                    pbgText.isEmpty() -> "--"
                    pbgText.contains("Reading", ignoreCase = true) -> "..."
                    else -> pbgText.substringAfter("PBG:").substringBefore("mmol/L").trim().replace(",", ".")
                }
                val mgdlDisplay = if (pbgDisplay.toFloatOrNull() != null) (pbgDisplay.toFloat() * 18.018f).toInt().toString() else "--"

                BentoMetricCard(
                    iconRes = R.drawable.ic_blood_glucose,
                    title = "Blood Glucose",
                    value = if (pbgText.contains("Reading", true)) "Reading..." else if (showMmol) "$pbgDisplay mmol/L" else "$mgdlDisplay mg/dL",
                    accentColor = TempOrange,
                    onClick = { 
                        if (pbgDisplay == "--") onReadPbgClick() else showMmol = !showMmol 
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BentoMetricCard(
                    iconRes = R.drawable.ic_temperature,
                    title = "Body Temp",
                    value = temperatureText.ifEmpty { "-- °C" },
                    accentColor = TempOrange,
                    onClick = onReadTemperatureClick,
                    modifier = Modifier.weight(1f)
                )
                BentoMetricCard(
                    iconRes = R.drawable.ic_stress,
                    title = "Stress Index",
                    value = stressText.ifEmpty { "--" },
                    accentColor = StressYellow,
                    onClick = onReadStressClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // New Metric Rows (Body Composition, Blood Components, MET, EDA, PPG, Health Glance)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BentoMetricCard(
                    iconRes = R.drawable.ic_body_comp_v2,
                    title = "Body Composition",
                    value = bmiText.ifEmpty { "--" },
                    accentColor = Color(0xFF2DD4BF),
                    onClick = { showBmiDialog = true },
                    modifier = Modifier.weight(1f)
                )
                BentoMetricCard(
                    iconRes = R.drawable.ic_blood_components_v2,
                    title = "Blood Components",
                    value = bloodComponentsText.ifEmpty { "--" },
                    accentColor = Color(0xFFE56350),
                    onClick = onReadSpO2Click,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BentoMetricCard(
                    iconRes = R.drawable.ic_met_v2,
                    title = "MET (Activity)",
                    value = metText.ifEmpty { "--" },
                    accentColor = Color(0xFF10B981),
                    onClick = onReadStepsClick,
                    modifier = Modifier.weight(1f)
                )
                BentoMetricCard(
                    iconRes = R.drawable.ic_eda_v2,
                    title = "EDA Activity",
                    value = edaText.ifEmpty { "--" },
                    accentColor = Color(0xFF3B82F6),
                    onClick = onReadStressClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val ppgVal = heartRateText.replace(Regex("[^0-9]"), "")
                BentoMetricCard(
                    iconRes = R.drawable.ic_ppg_v2,
                    title = "PPG Optical Sensor",
                    value = if (heartRateText.contains("Measuring")) "..." else if (ppgVal.isNotEmpty()) "$ppgVal bpm" else "--",
                    accentColor = HeartRed,
                    onClick = onReadHeartRateClick,
                    modifier = Modifier.weight(1f)
                )
                BentoMetricCard(
                    iconRes = R.drawable.ic_health_glance_v2,
                    title = "Health Glance",
                    value = "One-Key Exam",
                    accentColor = Color(0xFF4ADE80),
                    onClick = onHealthGlanceClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // 5. Sports Records Section (Workouts)
            SportsRecordsSection()

            // 6. All Metrics Health Explorer Launcher Button
            OutlinedButton(
                onClick = { showAllMetricsDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Explore All 12 Wearable Metrics 📊",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Primary)
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        // Floating Sparkling Star Sync Completion Banner
        AnimatedVisibility(
            visible = showSyncSuccessAnimation,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Primary,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✨ ", fontSize = 18.sp)
                    Text(
                        text = "5-Min Auto-Sync Completed!",
                        style = MaterialTheme.typography.labelLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (showAllMetricsDialog) {
        AllMetricsExplorerDialog(onDismiss = { showAllMetricsDialog = false })
    }

    if (showHealthGlanceReport) {
        AlertDialog(
            onDismissRequest = onDismissHealthGlanceReport,
            title = { Text("Health Examination Report") },
            text = {
                Text(
                    text = healthGlanceReportText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismissHealthGlanceReport,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Close Report")
                }
            }
        )
    }

    if (showBmiDialog) {
        BodyCompositionBmiDialog(
            userProfile = userProfile,
            bmiText = bmiText,
            onDismiss = { showBmiDialog = false },
            onEditProfileClick = {
                showBmiDialog = false
                onOpenDeviceScreen()
            }
        )
    }
}

@Composable
private fun BodyCompositionBmiDialog(
    userProfile: UserProfile?,
    bmiText: String,
    onDismiss: () -> Unit,
    onEditProfileClick: () -> Unit
) {
    val heightCm = userProfile?.height ?: 175
    val weightKg = userProfile?.weight ?: 70
    val isMale = userProfile?.isMale ?: true
    val hMeters = heightCm / 100.0
    val bmiValue = if (hMeters > 0) weightKg / (hMeters * hMeters) else 22.85
    val bmiFormatted = "%.1f".format(bmiValue)

    val (bmiCategory, categoryColor) = when {
        bmiValue < 18.5 -> "Underweight" to Color(0xFF60A5FA)
        bmiValue <= 24.9 -> "Normal Weight" to Color(0xFF10B981)
        bmiValue <= 29.9 -> "Overweight" to Color(0xFFFBBF24)
        else -> "Obese" to Color(0xFFF87171)
    }

    val bodyFatPct = if (isMale) (1.20 * bmiValue + 0.23 * (userProfile?.age ?: 25) - 16.2).coerceIn(8.0, 35.0)
                     else (1.20 * bmiValue + 0.23 * (userProfile?.age ?: 25) - 5.4).coerceIn(12.0, 45.0)
    val bmrKcal = if (isMale) (10 * weightKg + 6.25 * heightCm - 5 * (userProfile?.age ?: 25) + 5).toInt()
                  else (10 * weightKg + 6.25 * heightCm - 5 * (userProfile?.age ?: 25) - 161).toInt()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_body_comp_v2),
                    contentDescription = null,
                    tint = Color(0xFF2DD4BF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text("Body Composition & BMI")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = categoryColor.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$bmiFormatted kg/m²",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = categoryColor
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = bmiCategory.uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = categoryColor
                            )
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("BMI Classification Scale", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Row(
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
                    ) {
                        Box(modifier = Modifier.weight(18.5f).fillMaxHeight().background(Color(0xFF60A5FA)))
                        Box(modifier = Modifier.weight(6.4f).fillMaxHeight().background(Color(0xFF10B981)))
                        Box(modifier = Modifier.weight(5.0f).fillMaxHeight().background(Color(0xFFFBBF24)))
                        Box(modifier = Modifier.weight(10.0f).fillMaxHeight().background(Color(0xFFF87171)))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("<18.5", fontSize = 10.sp, color = Color.Gray)
                        Text("18.5–24.9", fontSize = 10.sp, color = Color.Gray)
                        Text("25–29.9", fontSize = 10.sp, color = Color.Gray)
                        Text(">30", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BmiDetailRow("Height", "$heightCm cm")
                    BmiDetailRow("Weight", "$weightKg kg")
                    BmiDetailRow("Est. Body Fat", "%.1f%%".format(bodyFatPct))
                    BmiDetailRow("Basal Metabolic Rate (BMR)", "$bmrKcal kcal/day")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun BmiDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground))
    }
}

// --- Dedicated Standalone Modern Daily Steps Widget ---

@Composable
private fun ModernDailyStepsWidget(
    stepsText: String,
    stepGoal: Int,
    onClick: () -> Unit
) {
    val isMeasuring = stepsText.contains("Measuring", ignoreCase = true)
    val stepCount = Regex("""Steps:\s*(\d+)""", RegexOption.IGNORE_CASE)
        .find(stepsText)?.groupValues?.get(1)?.toIntOrNull()
        ?: Regex("""^(\d+)""").find(stepsText.replace("🚶", "").trim())?.groupValues?.get(1)?.toIntOrNull()
        ?: 0

    val targetGoal = if (stepGoal > 0) stepGoal else 8000
    val progressFraction = (stepCount.toFloat() / targetGoal).coerceIn(0f, 1f)
    val percentage = (progressFraction * 100).toInt()

    val caloriesKcal = (stepCount * 0.04f).toInt()
    val calorieGoal = 500
    val calorieProgress = (caloriesKcal.toFloat() / calorieGoal).coerceIn(0f, 1f)
    val distanceKm = "%.2f".format(stepCount * 0.00075f)
    val activeMins = (stepCount / 100).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Widget Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF55D1C9).copy(alpha = 0.15f)
                    ) {
                        Box(modifier = Modifier.padding(10.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.ic_steps),
                                contentDescription = "Steps",
                                tint = Color(0xFF55D1C9),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Step Activity",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Goal Target: $targetGoal steps",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF55D1C9).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$percentage%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF0DB39E),
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }

            // Central Progress Section (Gauges + Values)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Dual Concentric Progress Rings
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val strokeW = 10.dp.toPx()

                        // Outer Steps Ring Track & Arc
                        drawArc(
                            color = Color(0xFF55D1C9).copy(alpha = 0.2f),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFF55D1C9),
                            startAngle = 135f,
                            sweepAngle = 270f * progressFraction,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )

                        // Inner Calorie Ring Track & Arc
                        val innerStrokeW = 7.dp.toPx()
                        val inset = 14.dp.toPx()
                        drawArc(
                            color = TempOrange.copy(alpha = 0.2f),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = innerStrokeW, cap = StrokeCap.Round),
                            topLeft = Offset(inset, inset),
                            size = Size(w - inset * 2, w - inset * 2)
                        )
                        drawArc(
                            color = TempOrange,
                            startAngle = 135f,
                            sweepAngle = 270f * calorieProgress,
                            useCenter = false,
                            style = Stroke(width = innerStrokeW, cap = StrokeCap.Round),
                            topLeft = Offset(inset, inset),
                            size = Size(w - inset * 2, w - inset * 2)
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.ic_footprints),
                        contentDescription = null,
                        tint = Color(0xFF55D1C9),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Stats Right Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (isMeasuring) "Measuring..." else "$stepCount",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = if (isMeasuring) 22.sp else 32.sp
                            )
                        )
                        if (!isMeasuring) {
                            Text(
                                text = " steps",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniMetricTag(label = "Calories", value = "$caloriesKcal kcal", color = TempOrange)
                        MiniMetricTag(label = "Distance", value = "$distanceKm km", color = Color(0xFF55D1C9))
                        MiniMetricTag(label = "Active", value = "$activeMins m", color = OxygenBlue)
                    }
                }
            }

            // Dual Progress Bars
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Steps Progress", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text("$stepCount / $targetGoal", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF0DB39E), fontWeight = FontWeight.Bold))
                }
                Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
                    drawLine(
                        color = Color(0xFF55D1C9).copy(alpha = 0.2f),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = size.height,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF55D1C9),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width * progressFraction, size.height / 2),
                        strokeWidth = size.height,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniMetricTag(label: String, value: String, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        Text(value, style = MaterialTheme.typography.labelMedium.copy(color = color, fontWeight = FontWeight.Bold))
    }
}

// --- Header, Activity & Sleep Cards ---

@Composable
private fun DashboardHeader(
    userProfile: UserProfile?,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val isDay = hour in 5..16
    val greetingText = when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..21 -> "Good Evening"
        else -> "Good Night"
    }

    val avatarUri = userProfile?.avatarUri

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.15f))
                    .clickable { onToggleDarkTheme() },
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "User Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_profile_nav),
                        contentDescription = "Default Avatar",
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val greetingIconRes = when (hour) {
                        in 5..11 -> R.drawable.ic_sun
                        in 12..16 -> R.drawable.ic_afternoon
                        in 17..21 -> R.drawable.ic_evening
                        else -> R.drawable.ic_moon
                    }
                    Text(
                        text = greetingText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(greetingIconRes),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Health Overview",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Icon(
            painter = painterResource(R.drawable.ic_share),
            contentDescription = "Share",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, "Smart Ring Test Vitals Summary: Daily Health Overview")
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Vitals"))
                }
        )
    }
}

@Composable
private fun SleepSummaryCard(
    sleepText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReading = sleepText.contains("Reading", ignoreCase = true)
    val isFailed = sleepText.contains("Failed", ignoreCase = true) || sleepText.contains("No sleep", ignoreCase = true)
    val displayValue = when {
        isReading -> "..."
        isFailed -> "0m"
        sleepText.isNotEmpty() -> sleepText.replace("😴", "").replace("Slept", "").trim()
        else -> "--"
    }

    Card(
        modifier = modifier
            .aspectRatio(1.1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SleepPurple.copy(alpha = 0.15f)
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sleep),
                            contentDescription = "Sleep",
                            tint = SleepPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Text(
                    text = "Sleep",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (isReading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SleepPurple, strokeWidth = 2.dp)
            } else {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            ) {
                Box(modifier = Modifier.weight(0.2f).fillMaxSize().background(SleepPurple.copy(alpha=0.3f)))
                Box(modifier = Modifier.weight(0.5f).fillMaxSize().background(SleepPurple.copy(alpha=0.6f)))
                Box(modifier = Modifier.weight(0.3f).fillMaxSize().background(SleepPurple))
            }
        }
    }
}

// --- Sports Records Workouts Section ---

@Composable
private fun SportsRecordsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Sports Records",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "3 Workouts",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            WorkoutItemRow("Outdoor Running", "Today 08:30 AM", "3.4 km", "24 min", "210 kcal", "138 bpm", isDeviceRecorded = false)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            WorkoutItemRow("Brisk Walk", "Yesterday 06:15 PM", "1.8 km", "18 min", "95 kcal", "112 bpm", isDeviceRecorded = true)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            WorkoutItemRow("Cycling Session", "2 days ago", "5.2 km", "15 min", "140 kcal", "125 bpm", isDeviceRecorded = true)
        }
    }
}

@Composable
private fun WorkoutItemRow(
    title: String,
    timeText: String,
    distText: String,
    durText: String,
    calText: String,
    hrText: String,
    isDeviceRecorded: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = if (isDeviceRecorded) Primary.copy(alpha = 0.15f) else StepsGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isDeviceRecorded) "Ring" else "App",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isDeviceRecorded) Primary else StepsGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            Text("$timeText • $durText", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("$distText • $calText", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text("Avg HR: $hrText", style = MaterialTheme.typography.bodySmall.copy(color = HeartRed))
        }
    }
}

// --- Featured Live Biometrics (Wide ECG Oscilloscope Card & Cardio Split) ---

@Composable
private fun FeaturedLiveBiometricsCard(
    heartRateText: String,
    hrvText: String,
    onReadHeartRateClick: () -> Unit,
    onReadHrvClick: () -> Unit,
    onOpenEcgClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Cardio Split (Heart Rate & HRV)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val hrVal = heartRateText.replace(Regex("[^0-9]"), "").ifEmpty { "--" }
            CardioSplitCard(
                iconRes = R.drawable.ic_heart_rate,
                title = "Heart Rate",
                value = hrVal,
                unit = "bpm",
                accentColor = HeartRed,
                onClick = onReadHeartRateClick,
                modifier = Modifier.weight(1f)
            )
            CardioSplitCard(
                iconRes = R.drawable.ic_stress,
                title = "HRV",
                value = if (hrvText.contains("Measuring")) "..." else if (hrvText.isEmpty()) "--" else hrvText,
                unit = "",
                accentColor = HeartRed,
                onClick = onReadHrvClick,
                modifier = Modifier.weight(1f)
            )
        }

        // ECG Live Signal Wide Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenEcgClick() },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HeartRed.copy(alpha = 0.15f)
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_ecg),
                                    contentDescription = "Live Signal",
                                    tint = HeartRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "ECG Live Waveform Stream",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = HeartRed
                    ) {
                        Text(
                            text = "Measure",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Oscilloscope Waveform Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val path = Path()
                        path.moveTo(0f, h/2)
                        var x = 0f
                        while (x < w) {
                            path.lineTo(x + 20f, h/2)
                            path.lineTo(x + 25f, h/2 - 8f)
                            path.lineTo(x + 30f, h/2 + 6f)
                            path.lineTo(x + 35f, h/2 - 25f)
                            path.lineTo(x + 40f, h/2 + 10f)
                            path.lineTo(x + 45f, h/2)
                            path.lineTo(x + 80f, h/2)
                            x += 80f
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFF00E676),
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardioSplitCard(
    iconRes: Int,
    title: String,
    value: String,
    unit: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Box(modifier = Modifier.padding(6.dp)) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            // Smooth Bezier Trendline Visualization
            Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                val path = Path()
                val w = size.width
                val h = size.height
                path.moveTo(0f, h/2)
                path.cubicTo(w*0.2f, h/2, w*0.3f, h, w*0.5f, h/2)
                path.cubicTo(w*0.7f, 0f, w*0.8f, h/2, w, h/2)
                drawPath(path, color = accentColor.copy(alpha = 0.4f), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(color = accentColor.copy(alpha=0.6f), radius = 3.dp.toPx(), center = Offset(0f, h/2))
                drawCircle(color = accentColor, radius = 4.dp.toPx(), center = Offset(w, h/2))
            }
        }
    }
}

// --- Bento Box Grid Card ---

@Composable
private fun BentoMetricCard(
    iconRes: Int,
    title: String,
    value: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1.1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            // Mini bar indicator
            Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                drawLine(
                    color = accentColor.copy(alpha = 0.3f),
                    start = Offset(0f, size.height/2),
                    end = Offset(size.width, size.height/2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = accentColor,
                    start = Offset(0f, size.height/2),
                    end = Offset(size.width * 0.7f, size.height/2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

// --- All Metrics Health Explorer Sheet ---

@Composable
private fun AllMetricsExplorerDialog(onDismiss: () -> Unit) {
    val metricsList = listOf(
        Triple("Heart Rate (HR)", "Continuous pulse measurement & HR zones", "Live Sensor"),
        Triple("Blood Oxygen (SpO2)", "Peripheral capillary oxygen saturation", "Live Sensor"),
        Triple("Peripheral Blood Glucose (PBG)", "Optical blood glucose estimation", "Derived Indicator"),
        Triple("Body Temperature", "Skin & core thermal trend", "Live Sensor"),
        Triple("Blood Pressure (BP)", "Systolic & diastolic pulse pressure wave", "Live Sensor"),
        Triple("Stress Index", "HRV-derived autonomic nervous system balance", "Derived Indicator"),
        Triple("Heart Rate Variability (HRV)", "Beat-to-beat RR interval variation", "Live Sensor"),
        Triple("Electrocardiogram (ECG)", "Electrocardiographic wave trace", "Live Sensor"),
        Triple("Sleep Quality & Stages", "Deep, Light, REM, & awake timeline", "Live Sensor"),
        Triple("Steps & Distance", "Triaxial accelerometer activity", "Live Sensor"),
        Triple("Metabolic Equivalent (MET)", "Energy expenditure rate", "Derived Indicator"),
        Triple("Electrodermal Activity (EDA)", "Galvanic skin conductance response", "Derived Indicator")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("All 12 Wearable Health Metrics") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().height(360.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                metricsList.forEach { (name, desc, mode) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Surface(
                                    shape = CircleShape,
                                    color = Primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = mode,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(color = Primary, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(desc, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text("Close Explorer")
            }
        }
    )
}
