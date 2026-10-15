package com.luffy.smartringtest.ui

import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luffy.smartringtest.ui.theme.HeartRed
import com.luffy.smartringtest.ui.theme.HeartRedBg
import com.luffy.smartringtest.ui.theme.StepsGreen
import com.luffy.smartringtest.ui.theme.StepsGreenBg
import com.luffy.smartringtest.ui.theme.TextPrimary
import com.luffy.smartringtest.ui.theme.TextSecondary

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ECG Monitor",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Electrocardiogram Recording",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

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
                            Text("❤️", fontSize = 22.sp)
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (heartRate > 0) "$heartRate BPM" else "-- BPM",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = HeartRed
                                )
                            )
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
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
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
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
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                EcgWaveformCanvas(waveform = waveform)
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
                            color = TextPrimary
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = resultSummary,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
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
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                    )
                }
            }
        }
    }
}

@Composable
private fun EcgWaveformCanvas(waveform: List<Int>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Draw dark medical grid background
        val gridStep = 30f
        var xGrid = 0f
        while (xGrid < w) {
            drawLine(
                color = Color(0xFF334155),
                start = Offset(xGrid, 0f),
                end = Offset(xGrid, h),
                strokeWidth = 1f
            )
            xGrid += gridStep
        }

        var yGrid = 0f
        while (yGrid < h) {
            drawLine(
                color = Color(0xFF334155),
                start = Offset(0f, yGrid),
                end = Offset(w, yGrid),
                strokeWidth = 1f
            )
            yGrid += gridStep
        }

        // Center baseline
        drawLine(
            color = Color(0xFF475569),
            start = Offset(0f, h / 2),
            end = Offset(w, h / 2),
            strokeWidth = 2f
        )

        if (waveform.size < 2) return@Canvas

        val visibleSamples = 300
        val data = if (waveform.size > visibleSamples) {
            waveform.takeLast(visibleSamples)
        } else {
            waveform
        }

        if (data.size < 2) return@Canvas

        var minV = Int.MAX_VALUE
        var maxV = Int.MIN_VALUE
        for (v in data) {
            if (v < minV) minV = v
            if (v > maxV) maxV = v
        }

        if (minV == Int.MAX_VALUE || maxV == Int.MIN_VALUE) return@Canvas
        if (maxV - minV < 5) return@Canvas

        val range = (maxV - minV).toFloat().coerceAtLeast(1f)
        val path = Path()
        val stepX = w / (data.size - 1).coerceAtLeast(1)

        data.forEachIndexed { i, v ->
            val normalized = ((v - minV).toFloat() / range).coerceIn(0f, 1f)
            val y = h - (normalized * h * 0.85f) - (h * 0.075f)
            val x = i * stepX
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = Color(0xFF00E676),  // Neon medical green
            style = Stroke(width = 3f)
        )
    }
}
