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
fun CameraCaptureScreen(onNext: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Camera Capture", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Placeholder for camera capture UI.")
        Button(onClick = { onNext("content://demo/photo") }) {
            Text(text = "Use Dummy Photo")
        }
    }
}
