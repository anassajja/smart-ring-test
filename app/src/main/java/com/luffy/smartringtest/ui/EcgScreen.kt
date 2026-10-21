package com.luffy.smartringtest.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luffy.smartringtest.R
import com.luffy.smartringtest.ui.theme.HeartRed
import com.luffy.smartringtest.ui.theme.HeartRedBg
import com.luffy.smartringtest.ui.theme.StepsGreen
import com.luffy.smartringtest.ui.theme.StepsGreenBg

@Composable
fun EcgScreen(
    statusText: String,
    progress: Float,
    heartRate: Int,
    wearOk: Boolean,
    waveform: List<Int>,
    resultSummary: String,
    diagnosisText: String,
    isMeasuring: Boolean,
    waveformVersion: Int,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hrZoneText = when {
        heartRate <= 0 -> "--"
        heartRate < 60 -> "Resting HR"
        heartRate in 60..100 -> "Normal Rhythm ✓"
        heartRate in 101..140 -> "Light Aerobic"
        else -> "High HR ⚠"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ECG Monitor",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            OutlinedButton(
                onClick = onBackClick,
                enabled = !isMeasuring,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back")
            }
        }

        // Live HR & Wear Status Banner
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(HeartRedBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_heart_rate),
                                contentDescription = "Heart Rate",
                                tint = HeartRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (heartRate > 0) "$heartRate BPM" else "-- BPM",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = HeartRed
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = HeartRed.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = hrZoneText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = HeartRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (wearOk) StepsGreenBg else HeartRedBg
                    ) {
                        Text(
                            text = if (wearOk) "✓ Contact OK" else "⚠ Low Contact",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (wearOk) StepsGreen else HeartRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Place index finger & thumb firmly on the ring electrode during measurement.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                if (isMeasuring) {
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = HeartRed
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Progress: ${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // ECG Medical Waveform Paper Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                EcgWaveformCanvas(waveform = waveform, isMeasuring = isMeasuring)
            }
        }

        // Action Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = if (isMeasuring) onStopClick else onStartClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMeasuring) Color(0xFFDC2626) else HeartRed
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text(
                    text = if (isMeasuring) "Stop ECG Recording" else "Start ECG Measurement",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // Result & Diagnosis Summary Cards
        if (resultSummary.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 ECG Measurement Result",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = resultSummary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        if (diagnosisText.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StepsGreenBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🩺 Diagnostic Summary",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = StepsGreen
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = diagnosisText,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF1E293B))
                    )
                }
            }
        }
    }
}

@Composable
private fun EcgWaveformCanvas(waveform: List<Int>, isMeasuring: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Draw dark medical grid background
        val gridStep = 30f
        var xGrid = 0f
        while (xGrid < w) {
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(xGrid, 0f),
                end = Offset(xGrid, h),
                strokeWidth = 1f
            )
            xGrid += gridStep
        }

        var yGrid = 0f
        while (yGrid < h) {
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(0f, yGrid),
                end = Offset(w, yGrid),
                strokeWidth = 1f
            )
            yGrid += gridStep
        }

        // Center baseline
        drawLine(
            color = Color(0xFF334155),
            start = Offset(0f, h / 2),
            end = Offset(w, h / 2),
            strokeWidth = 2f
        )

        // If no active data or small buffer, draw a resting ECG template baseline curve
        if (waveform.size < 4) {
            drawRestingEcgTemplate(w, h)
            return@Canvas
        }

        val visibleSamples = 300
        val data = if (waveform.size > visibleSamples) {
            waveform.takeLast(visibleSamples)
        } else {
            waveform
        }

        var minV = Int.MAX_VALUE
        var maxV = Int.MIN_VALUE
        var sum = 0L
        for (v in data) {
            if (v < minV) minV = v
            if (v > maxV) maxV = v
            sum += v
        }

        if (minV == Int.MAX_VALUE || maxV == Int.MIN_VALUE) {
            drawRestingEcgTemplate(w, h)
            return@Canvas
        }

        val range = (maxV - minV).toFloat().coerceAtLeast(10f)
        val avg = (sum.toFloat() / data.size)
        val path = Path()
        val stepX = w / (data.size - 1).coerceAtLeast(1)

        // Auto-gain scaling around center baseline
        data.forEachIndexed { i, v ->
            val dev = v - avg
            val normDev = (dev / range).coerceIn(-0.5f, 0.5f)
            val y = (h / 2) - (normDev * h * 0.75f)
            val x = i * stepX
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = Color(0xFF00E676),  // Neon medical green
            style = Stroke(width = 3.5f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRestingEcgTemplate(
    w: Float,
    h: Float
) {
    val path = Path()
    val midY = h / 2
    val pulseWidth = 200f
    var x = 0f

    path.moveTo(0f, midY)
    while (x < w) {
        path.lineTo(x + 40f, midY)
        path.lineTo(x + 50f, midY - 10f)  // P wave
        path.lineTo(x + 60f, midY)
        path.lineTo(x + 70f, midY + 8f)   // Q wave
        path.lineTo(x + 80f, midY - 55f)  // R wave (peak)
        path.lineTo(x + 90f, midY + 18f)  // S wave
        path.lineTo(x + 100f, midY)
        path.lineTo(x + 120f, midY - 15f) // T wave
        path.lineTo(x + 140f, midY)
        path.lineTo(x + pulseWidth, midY)
        x += pulseWidth
    }

    drawPath(
        path = path,
        color = Color(0xFF00E676).copy(alpha = 0.45f),
        style = Stroke(width = 2.5f)
    )
}
