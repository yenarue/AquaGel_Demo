package com.example.demoaquagel

import android.os.Bundle
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.core.content.FileProvider
import android.content.Intent
import android.widget.Toast
import java.util.Locale
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

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
                        onGoStage = { photoUri ->
                            val encoded = java.net.URLEncoder.encode(photoUri, "UTF-8")
                            navController.navigate("${Routes.StageResult}?photoUri=$encoded")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "${Routes.StageResult}?photoUri={photoUri}",
                    arguments = listOf(
                        navArgument("photoUri") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    StageResultScreen(
                        photoUri = backStackEntry.arguments?.getString("photoUri"),
                        onRetake = {
                            navController.popBackStack(Routes.CameraCapture, inclusive = false)
                        },
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
    val activity = LocalContext.current as ComponentActivity
    val viewModel: MonitoringViewModel = viewModel(activity)
    val latestSample by viewModel.latestSample.collectAsStateWithLifecycle()
    val recentSamples by viewModel.recentSamples.collectAsStateWithLifecycle()
    val animatedTemperature by animateFloatAsState(
        targetValue = latestSample.temperature,
        animationSpec = tween(durationMillis = 1500),
        label = "temperature"
    )
    val animatedHumidity by animateFloatAsState(
        targetValue = latestSample.humidity,
        animationSpec = tween(durationMillis = 1500),
        label = "humidity"
    )
    val animatedImpedance by animateFloatAsState(
        targetValue = latestSample.impedance.toFloat(),
        animationSpec = tween(durationMillis = 1500),
        label = "impedance"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Live Monitoring", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "실시간 측정 중",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Sensor data is being continuously collected.")
        Spacer(modifier = Modifier.height(20.dp))
        MetricCard(
            label = "Temperature",
            value = String.format(Locale.US, "%.1f", animatedTemperature),
            unit = "°C"
        ) {
            LineChart(
                samples = recentSamples,
                valueSelector = { it.temperature },
                minValue = 36.0f,
                maxValue = 38.5f,
                unit = "°C"
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        MetricCard(
            label = "Humidity",
            value = String.format(Locale.US, "%.1f", animatedHumidity),
            unit = "%"
        ) {
            LineChart(
                samples = recentSamples,
                valueSelector = { it.humidity },
                minValue = 40.0f,
                maxValue = 85.0f,
                unit = "%"
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        MetricCard(
            label = "Impedance",
            value = String.format(Locale.US, "%.0f", animatedImpedance),
            unit = "Ω"
        ) {
            LineChart(
                samples = recentSamples,
                valueSelector = { it.impedance.toFloat() },
                minValue = 350f,
                maxValue = 900f,
                unit = "Ω"
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGoCamera) {
            Text(text = "Go to Healing Stage")
        }
    }
}

@Composable
fun CameraCaptureScreen(onGoStage: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (ex: Exception) {
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Camera Capture", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        if (hasPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val file = File(
                    context.cacheDir,
                    "capture_${System.currentTimeMillis()}.jpg"
                )
                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exception: ImageCaptureException) {
                        }

                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val uriString = file.toUri().toString()
                            onGoStage(uriString)
                        }
                    }
                )
            }) {
                Text(text = "Capture Photo")
            }
        } else {
            Text(
                text = "Camera permission is required to capture a photo.",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            }) {
                Text(text = "Grant Permission")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBack) {
            Text(text = "Back")
        }
    }
}

@Composable
fun StageResultScreen(
    photoUri: String?,
    onRetake: () -> Unit,
    onBackToLive: () -> Unit
) {
    val viewModel: StageResultViewModel = viewModel()
    val activity = LocalContext.current as ComponentActivity
    val monitoringViewModel: MonitoringViewModel = viewModel(activity)
    val context = LocalContext.current
    val detectedStage by viewModel.detectedStage.collectAsStateWithLifecycle()
    val overrideStage by viewModel.overrideStage.collectAsStateWithLifecycle()
    val photoError by viewModel.photoError.collectAsStateWithLifecycle()
    val recentSamples by monitoringViewModel.recentSamples.collectAsStateWithLifecycle()
    val stage = overrideStage ?: detectedStage
    val stageData = viewModel.stageDataFor(stage)
    var menuExpanded by remember { mutableStateOf(false) }
    val decodedPhotoUri = photoUri?.let { java.net.URLDecoder.decode(it, "UTF-8") }
    val coroutineScope = rememberCoroutineScope()
    val statusIcon = when (stage) {
        HealingStage.STAGE_1 -> "!"
        HealingStage.STAGE_2 -> "●"
        HealingStage.STAGE_3 -> "✓"
    }

    LaunchedEffect(decodedPhotoUri) {
        viewModel.setPhotoUri(context, decodedPhotoUri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stageData.title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { menuExpanded = true }) {
                Text(text = "⋮")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Auto Detect") },
                    onClick = {
                        viewModel.clearOverride()
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = "Force Stage 1") },
                    onClick = {
                        viewModel.setOverride(HealingStage.STAGE_1)
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = "Force Stage 2") },
                    onClick = {
                        viewModel.setOverride(HealingStage.STAGE_2)
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = "Force Stage 3") },
                    onClick = {
                        viewModel.setOverride(HealingStage.STAGE_3)
                        menuExpanded = false
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(stageData.colorArgb)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusIcon,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Color(stageData.colorArgb))
            )
            Box(
                modifier = Modifier
                    .height(18.dp)
                    .fillMaxWidth(0.5f)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(stageData.colorArgb))
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stageData.gelColorLabel,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stageData.message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stageData.cta,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Monitoring", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "Temp ${stageData.temperature}°C",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Humidity ${stageData.humidity}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Impedance ${stageData.impedance}Ω",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (!decodedPhotoUri.isNullOrBlank()) {
            AsyncImage(
                model = decodedPhotoUri,
                contentDescription = "Captured photo",
                modifier = Modifier
                    .size(140.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (!photoError.isNullOrBlank()) {
            Text(
                text = photoError ?: "",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetake) {
            Text(text = "Retake Photo")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBackToLive) {
            Text(text = "Back to Live Monitoring")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            coroutineScope.launch {
                try {
                    val file = withContext(Dispatchers.IO) {
                        PdfReportGenerator.generateReport(
                            context,
                            stageData,
                            decodedPhotoUri,
                            recentSamples
                        )
                    }
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(
                        Intent.createChooser(shareIntent, "Share PDF report")
                    )
                } catch (ex: Exception) {
                    Toast.makeText(
                        context,
                        "Failed to generate PDF report.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }) {
            Text(text = "Export PDF Report")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            viewModel.resetDemo()
            onBackToLive()
        }) {
            Text(text = "Reset Demo")
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    unit: String,
    content: @Composable () -> Unit = {}
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
fun LineChart(
    samples: List<MonitoringSample>,
    valueSelector: (MonitoringSample) -> Float,
    minValue: Float,
    maxValue: Float,
    unit: String
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (samples.size < 2) {
            return@Canvas
        }
        val width = size.width
        val height = size.height
        val leftPadding = 52f
        val bottomPadding = 12f
        val chartWidth = width - leftPadding
        val chartHeight = height - bottomPadding
        val gridLines = 3
        for (i in 0..gridLines) {
            val y = chartHeight - (chartHeight / gridLines) * i
            drawLine(
                color = gridColor,
                start = Offset(leftPadding, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }
        drawLine(
            color = axisColor,
            start = Offset(leftPadding, 0f),
            end = Offset(leftPadding, chartHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = axisColor,
            start = Offset(leftPadding, chartHeight),
            end = Offset(width, chartHeight),
            strokeWidth = 2f
        )
        val textPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 20f
            color = axisColor.toArgb()
        }
        drawContext.canvas.nativeCanvas.drawText(
            String.format(Locale.US, "%.0f%s", maxValue, unit),
            2f,
            20f,
            textPaint
        )
        drawContext.canvas.nativeCanvas.drawText(
            String.format(Locale.US, "%.0f%s", minValue, unit),
            2f,
            chartHeight,
            textPaint
        )
        val stepX = chartWidth / (samples.size - 1).coerceAtLeast(1)
        var lastPoint: Offset? = null
        samples.forEachIndexed { index, sample ->
            val value = valueSelector(sample).coerceIn(minValue, maxValue)
            val normalized = (value - minValue) / (maxValue - minValue)
            val x = leftPadding + (stepX * index)
            val y = chartHeight - (normalized * chartHeight)
            val point = Offset(x, y)
            lastPoint?.let { previous ->
                drawLine(
                    color = lineColor,
                    start = previous,
                    end = point,
                    strokeWidth = 4f
                )
            }
            lastPoint = point
        }
    }
}
