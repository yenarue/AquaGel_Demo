package com.example.aquagel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WoundInfoScreen(
    photoUri: String,
    onSubmit: (WoundInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Wound Info", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Photo URI: $photoUri")
        Text(text = "Placeholder form fields go here.")
        Button(
            onClick = {
                onSubmit(
                    WoundInfo(
                        ageRange = "30-39",
                        gender = "Female",
                        woundType = "Laceration",
                        location = "Forearm",
                        size = "2 cm",
                        timeSinceInjury = "2 hours"
                    )
                )
            }
        ) {
            Text(text = "Submit Dummy Info")
        }
    }
}
