package com.example.demoaquagel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.FileInputStream
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StageData(
    val title: String,
    val message: String,
    val cta: String,
    val gelColorLabel: String,
    val temperature: String,
    val humidity: String,
    val impedance: String,
    val colorArgb: Int
)

enum class HealingStage {
    STAGE_1,
    STAGE_2,
    STAGE_3
}

class StageResultViewModel : ViewModel() {
    private val _photoUri = MutableStateFlow<String?>(null)
    val photoUri: StateFlow<String?> = _photoUri.asStateFlow()

    private val _detectedStage = MutableStateFlow(HealingStage.STAGE_2)
    val detectedStage: StateFlow<HealingStage> = _detectedStage.asStateFlow()

    private val _overrideStage = MutableStateFlow<HealingStage?>(null)
    val overrideStage: StateFlow<HealingStage?> = _overrideStage.asStateFlow()

    private val _photoError = MutableStateFlow<String?>(null)
    val photoError: StateFlow<String?> = _photoError.asStateFlow()

    fun setPhotoUri(context: Context, uriString: String?) {
        if (uriString.isNullOrBlank()) {
            _photoUri.value = null
            _detectedStage.value = HealingStage.STAGE_2
            _photoError.value = "Photo not available."
            return
        }
        if (_photoUri.value == uriString) {
            return
        }
        _photoUri.value = uriString
        _photoError.value = null
        viewModelScope.launch {
            val stage = withContext(Dispatchers.Default) {
                detectStageFromUri(context, uriString)
            }
            _detectedStage.value = stage
        }
    }

    fun setOverride(stage: HealingStage) {
        _overrideStage.value = stage
    }

    fun clearOverride() {
        _overrideStage.value = null
    }

    fun resetDemo() {
        _photoUri.value = null
        _detectedStage.value = HealingStage.STAGE_2
        _overrideStage.value = null
        _photoError.value = null
    }

    fun stageDataFor(stage: HealingStage): StageData {
        return when (stage) {
            HealingStage.STAGE_1 -> StageData(
                title = "Stage 1 — Wound Detected",
                message = "Signs of early inflammation detected.",
                cta = "Increase monitoring & consider care intervention",
                gelColorLabel = "Yellow",
                temperature = "38.1",
                humidity = "82",
                impedance = "420",
                colorArgb = Color.parseColor("#F1C40F")
            )
            HealingStage.STAGE_2 -> StageData(
                title = "Stage 2 — Healing in Progress",
                message = "Wound is healing as expected.",
                cta = "Maintain current care routine",
                gelColorLabel = "Light Yellow",
                temperature = "36.9",
                humidity = "65",
                impedance = "610",
                colorArgb = Color.parseColor("#F7E48A")
            )
            HealingStage.STAGE_3 -> StageData(
                title = "Stage 3 — Healing Completed",
                message = "Healing nearly complete.",
                cta = "Reduce monitoring & resume normal care",
                gelColorLabel = "Transparent",
                temperature = "36.4",
                humidity = "48",
                impedance = "820",
                colorArgb = Color.parseColor("#D7F9E5")
            )
        }
    }

    private suspend fun detectStageFromUri(context: Context, uriString: String): HealingStage {
        return try {
            val bitmap = withContext(Dispatchers.IO) {
                loadBitmap(context, uriString)
            } ?: return HealingStage.STAGE_2
            val stage = detectStageFromBitmap(bitmap)
            bitmap.recycle()
            stage
        } catch (ex: Exception) {
            _photoError.value = "Photo not available."
            HealingStage.STAGE_2
        }
    }

    private fun loadBitmap(context: Context, uriString: String): Bitmap? {
        val uri = Uri.parse(uriString)
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
            inSampleSize = 4
        }
        return if (uri.scheme == "content") {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
        } else {
            val path = uri.path ?: return null
            if (!java.io.File(path).exists()) {
                return null
            }
            FileInputStream(path).use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }
        }
    }

    private fun detectStageFromBitmap(bitmap: Bitmap): HealingStage {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) {
            return HealingStage.STAGE_2
        }
        val cropX = (width * 0.25f).roundToInt()
        val cropY = (height * 0.25f).roundToInt()
        val cropWidth = (width * 0.5f).roundToInt()
        val cropHeight = (height * 0.5f).roundToInt()
        val crop = Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight)

        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        val total = cropWidth * cropHeight
        for (y in 0 until cropHeight) {
            for (x in 0 until cropWidth) {
                val color = crop.getPixel(x, y)
                rSum += Color.red(color)
                gSum += Color.green(color)
                bSum += Color.blue(color)
            }
        }
        crop.recycle()
        val rAvg = (rSum / total).toInt()
        val gAvg = (gSum / total).toInt()
        val bAvg = (bSum / total).toInt()

        val hsv = FloatArray(3)
        Color.RGBToHSV(rAvg, gAvg, bAvg, hsv)
        val hue = hsv[0]
        val saturation = hsv[1]
        val value = hsv[2]

        if (saturation < 0.2f && value > 0.8f) {
            return HealingStage.STAGE_3
        }
        if (hue in 40f..70f && saturation >= 0.5f) {
            return HealingStage.STAGE_1
        }
        if (hue in 40f..70f) {
            return HealingStage.STAGE_2
        }
        return HealingStage.STAGE_2
    }
}
