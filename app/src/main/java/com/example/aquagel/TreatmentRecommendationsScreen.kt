package com.example.aquagel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TreatmentRecommendationsScreen(
    photoUri: String,
    woundInfo: WoundInfo?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Recommendations", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Photo URI: $photoUri")
        Text(text = "Wound info: ${woundInfo ?: "Not provided"}")
        Text(text = "Placeholder recommendations shown here.")
    }
}
