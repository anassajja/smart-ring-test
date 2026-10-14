package com.luffy.smartringtest.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luffy.smartringtest.HealthReading

@Composable
fun HistoryScreen(
    readings: List<HealthReading>,
    statusText: String,
    progress: Float,
    isSyncing: Boolean,
    onSyncClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Ring History",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(12.dp))

        Text(text = statusText, style = MaterialTheme.typography.bodyLarge)

        if (isSyncing) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSyncClick,
                enabled = !isSyncing,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isSyncing) "Syncing..." else "Sync History")
            }
            Button(
                onClick = onBackClick,
                enabled = !isSyncing,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Readings: ${readings.size}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        if (readings.isEmpty()) {
            Text(
                "No readings yet. Tap \"Sync History\".",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(readings) { r ->
                    ReadingRow(r)
                }
            }
        }
    }
}

@Composable
private fun ReadingRow(r: HealthReading) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${r.date}  ${r.time}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (r.wear == 1) "worn" else "off",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "❤️ ${r.heartRate} bpm   •   🚶 ${r.steps} steps   •   🔥 ${"%.1f".format(r.calories)} kcal",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "📏 ${"%.2f".format(r.distanceMeters)} km   •   🏃 sport ${r.sportValue}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}