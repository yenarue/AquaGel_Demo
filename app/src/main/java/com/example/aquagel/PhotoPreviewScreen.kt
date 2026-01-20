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
fun PhotoPreviewScreen(
    photoUri: String,
    onNext: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Photo Preview", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Photo URI: $photoUri")
        Button(onClick = { onNext(photoUri) }) {
            Text(text = "Continue to Wound Info")
        }
    }
}
