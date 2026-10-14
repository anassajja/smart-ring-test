package com.luffy.smartringtest.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * First-launch profile setup screen.
 * Collects height, weight, age, and sex — needed by the ring to compute
 * distance, calories, and sleep quality accurately.
 */
@Composable
fun ProfileScreen(
    onSave: (height: Int, weight: Int, age: Int, isMale: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var heightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Setup your profile",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "This info helps the ring calculate distance, calories, and sleep quality accurately.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = heightText,
            onValueChange = { heightText = it.filter { c -> c.isDigit() } },
            label = { Text("Height (cm)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it.filter { c -> c.isDigit() } },
            label = { Text("Weight (kg)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = ageText,
            onValueChange = { ageText = it.filter { c -> c.isDigit() } },
            label = { Text("Age (years)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Text("Sex", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SexButton(
                label = "Male",
                selected = isMale,
                onClick = { isMale = true },
                modifier = Modifier.weight(1f)
            )
            SexButton(
                label = "Female",
                selected = !isMale,
                onClick = { isMale = false },
                modifier = Modifier.weight(1f)
            )
        }

        if (errorText.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(24.dp))

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
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save & Continue")
        }
    }
}

@Composable
private fun SexButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (selected) "✓ $label" else label)
        }
    }
}