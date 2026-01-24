package com.example.demoaquagel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppRoot()
        }
    }
}

object Routes {
    const val LiveMonitor = "live_monitor"
    const val CameraCapture = "camera_capture"
    const val StageResult = "stage_result"
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Routes.LiveMonitor
            ) {
                composable(Routes.LiveMonitor) {
                    LiveMonitorScreen(
                        onGoCamera = { navController.navigate(Routes.CameraCapture) }
                    )
                }
                composable(Routes.CameraCapture) {
                    CameraCaptureScreen(
                        onGoStage = { navController.navigate(Routes.StageResult) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.StageResult) {
                    StageResultScreen(
                        onBackToLive = {
                            navController.popBackStack(Routes.LiveMonitor, inclusive = false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LiveMonitorScreen(onGoCamera: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Live Monitoring", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Placeholder screen for real-time monitoring.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGoCamera) {
            Text(text = "Go to Camera Capture")
        }
    }
}

@Composable
fun CameraCaptureScreen(onGoStage: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Camera Capture", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Placeholder screen for camera capture flow.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGoStage) {
            Text(text = "Go to Stage Result")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBack) {
            Text(text = "Back")
        }
    }
}

@Composable
fun StageResultScreen(onBackToLive: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Stage Result", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Placeholder screen for healing stage result.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBackToLive) {
            Text(text = "Back to Live Monitoring")
        }
    }
}
