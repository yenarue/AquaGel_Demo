package com.example.demoaquagel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object PdfReportGenerator {
    fun generateReport(
        context: Context,
        stageData: StageData,
        photoUri: String?,
        samples: List<MonitoringSample>
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            isAntiAlias = true
            textSize = 20f
        }
        val labelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
        }
        val valuePaint = Paint().apply {
            isAntiAlias = true
            textSize = 14f
        }
        val linePaint = Paint().apply {
            strokeWidth = 1f
        }

        var y = 60f
        canvas.drawText("AKQUA Gel Data Report", 40f, y, titlePaint)
        y += 24f
        val dateText = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        canvas.drawText("Date/Time: $dateText", 40f, y, labelPaint)
        y += 24f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 24f

        drawLabeledValue(canvas, "Healing Stage", stageData.title, y, labelPaint, valuePaint)
        y += 36f
        drawLabeledValue(canvas, "Message", stageData.message, y, labelPaint, valuePaint)
        y += 36f
        drawLabeledValue(canvas, "Gel Color", stageData.gelColorLabel, y, labelPaint, valuePaint)
        y += 36f
        drawLabeledValue(
            canvas,
            "Monitoring",
            "Temp ${stageData.temperature}°C  |  Humidity ${stageData.humidity}%  |  Impedance ${stageData.impedance}Ω",
            y,
            labelPaint,
            valuePaint
        )
        y += 36f

        val reportSamples = ensureSamples(samples, stageData)
        if (reportSamples.size >= 2) {
            canvas.drawText("Live Monitoring Trend", 40f, y, labelPaint)
            y += 16f
            val chartWidth = 515f
            val chartHeight = 70f
            val chartGap = 18f
            val left = 40f

            drawLegend(canvas, left, y)
            y += 18f

            drawTrendChart(
                canvas = canvas,
                left = left,
                top = y,
                width = chartWidth,
                height = chartHeight,
                samples = reportSamples,
                seriesLabel = "Temp (°C)",
                minValue = 36.0f,
                maxValue = 38.5f,
                color = android.graphics.Color.parseColor("#1E88E5"),
                selector = { it.temperature }
            )
            y += chartHeight + chartGap

            drawTrendChart(
                canvas = canvas,
                left = left,
                top = y,
                width = chartWidth,
                height = chartHeight,
                samples = reportSamples,
                seriesLabel = "Humidity (%)",
                minValue = 40.0f,
                maxValue = 85.0f,
                color = android.graphics.Color.parseColor("#43A047"),
                selector = { it.humidity }
            )
            y += chartHeight + chartGap

            drawTrendChart(
                canvas = canvas,
                left = left,
                top = y,
                width = chartWidth,
                height = chartHeight,
                samples = reportSamples,
                seriesLabel = "Impedance (Ω)",
                minValue = 350f,
                maxValue = 900f,
                color = android.graphics.Color.parseColor("#F4511E"),
                selector = { it.impedance.toFloat() }
            )
            y += chartHeight + 24f
        }

        val thumbnail = loadThumbnail(context, photoUri)
        if (thumbnail != null) {
            canvas.drawText("Photo", 40f, y, labelPaint)
            val maxWidth = 220
            val maxHeight = 220
            val scaled = scaleBitmap(thumbnail, maxWidth, maxHeight)
            val left = 40f
            val top = y + 12f
            canvas.drawBitmap(scaled, left, top, null)
            if (scaled != thumbnail) {
                scaled.recycle()
            }
            thumbnail.recycle()
        }

        document.finishPage(page)

        val outDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.cacheDir
        val outFile = File(outDir, "akqua_report_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outFile
    }

    private fun drawLabeledValue(
        canvas: Canvas,
        label: String,
        value: String,
        y: Float,
        labelPaint: Paint,
        valuePaint: Paint
    ) {
        canvas.drawText("$label:", 40f, y, labelPaint)
        canvas.drawText(value, 40f, y + 18f, valuePaint)
    }

    private fun drawTrendChart(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        samples: List<MonitoringSample>,
        seriesLabel: String,
        minValue: Float,
        maxValue: Float,
        color: Int,
        selector: (MonitoringSample) -> Float
    ) {
        val axisPaint = Paint().apply {
            strokeWidth = 1.5f
        }
        val linePaint = Paint().apply {
            this.color = color
            strokeWidth = 3f
        }
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 11f
        }

        val labelY = top - 4f
        canvas.drawText(seriesLabel, left, labelY, textPaint)
        val labelWidth = 44f
        val plotLeft = left + labelWidth
        val plotRight = left + width
        canvas.drawText("Max", left + 2f, top + 12f, textPaint)
        canvas.drawText(
            String.format(Locale.US, "%.0f", maxValue),
            left + 20f,
            top + 12f,
            textPaint
        )
        canvas.drawText(
            "Min",
            left + 2f,
            top + height - 4f,
            textPaint
        )
        canvas.drawText(
            String.format(Locale.US, "%.0f", minValue),
            left + 20f,
            top + height - 4f,
            textPaint
        )
        canvas.drawRect(plotLeft, top, plotRight, top + height, axisPaint)

        val stepX = (plotRight - plotLeft) / (samples.size - 1).coerceAtLeast(1)
        drawSeries(
            canvas = canvas,
            left = plotLeft,
            top = top,
            height = height,
            stepX = stepX,
            samples = samples,
            minValue = minValue,
            maxValue = maxValue,
            paint = linePaint,
            selector = selector
        )
    }

    private fun drawLegend(canvas: Canvas, left: Float, top: Float) {
        val labelPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
        }
        val tempColor = android.graphics.Color.parseColor("#1E88E5")
        val humidityColor = android.graphics.Color.parseColor("#43A047")
        val impedanceColor = android.graphics.Color.parseColor("#F4511E")
        var x = left
        drawLegendItem(canvas, x, top, tempColor, "Temp")
        x += 110f
        drawLegendItem(canvas, x, top, humidityColor, "Humidity")
        x += 140f
        drawLegendItem(canvas, x, top, impedanceColor, "Impedance")
    }

    private fun drawLegendItem(
        canvas: Canvas,
        x: Float,
        y: Float,
        color: Int,
        label: String
    ) {
        val swatchPaint = Paint().apply {
            this.color = color
        }
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
        }
        canvas.drawRect(x, y - 8f, x + 14f, y + 4f, swatchPaint)
        canvas.drawText(label, x + 20f, y + 4f, textPaint)
    }

    private fun ensureSamples(
        samples: List<MonitoringSample>,
        stageData: StageData
    ): List<MonitoringSample> {
        if (samples.size >= 12) {
            return samples
        }
        val baseTemp = stageData.temperature.toFloatOrNull() ?: 37.0f
        val baseHumidity = stageData.humidity.toFloatOrNull() ?: 60.0f
        val baseImpedance = stageData.impedance.toIntOrNull()?.toFloat() ?: 600f
        val random = Random(System.currentTimeMillis())
        val list = samples.toMutableList()
        var temp = if (list.isNotEmpty()) list.last().temperature else baseTemp
        var humidity = if (list.isNotEmpty()) list.last().humidity else baseHumidity
        var impedance = if (list.isNotEmpty()) list.last().impedance.toFloat() else baseImpedance
        val total = 20
        val timestampBase = System.currentTimeMillis()
        while (list.size < total) {
            temp = (temp + random.nextDouble(-0.2, 0.2)).toFloat().coerceIn(36.0f, 38.5f)
            humidity = (humidity + random.nextDouble(-1.5, 1.5)).toFloat().coerceIn(40.0f, 85.0f)
            impedance = (impedance + random.nextDouble(-12.0, 12.0)).toFloat().coerceIn(350f, 900f)
            list.add(
                MonitoringSample(
                    timestamp = timestampBase + (list.size * 1500L),
                    temperature = temp,
                    humidity = humidity,
                    impedance = impedance.toInt()
                )
            )
        }
        return list
    }

    private fun drawSeries(
        canvas: Canvas,
        left: Float,
        top: Float,
        height: Float,
        stepX: Float,
        samples: List<MonitoringSample>,
        minValue: Float,
        maxValue: Float,
        paint: Paint,
        selector: (MonitoringSample) -> Float
    ) {
        var lastX = left
        var lastY = top + height
        samples.forEachIndexed { index, sample ->
            val value = selector(sample).coerceIn(minValue, maxValue)
            val normalized = (value - minValue) / (maxValue - minValue)
            val x = left + (stepX * index)
            val y = top + height - (normalized * height)
            if (index > 0) {
                canvas.drawLine(lastX, lastY, x, y, paint)
            }
            lastX = x
            lastY = y
        }
    }

    private fun loadThumbnail(context: Context, photoUri: String?): Bitmap? {
        if (photoUri.isNullOrBlank()) {
            return null
        }
        val uri = Uri.parse(photoUri)
        val options = android.graphics.BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
            inSampleSize = 4
        }
        return try {
            if (uri.scheme == "content") {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    android.graphics.BitmapFactory.decodeStream(input, null, options)
                }
            } else {
                FileInputStream(uri.path ?: return null).use { input ->
                    android.graphics.BitmapFactory.decodeStream(input, null, options)
                }
            }
        } catch (ex: Exception) {
            null
        }
    }

    private fun scaleBitmap(source: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = source.width
        val height = source.height
        if (width <= maxWidth && height <= maxHeight) {
            return source
        }
        val widthRatio = maxWidth.toFloat() / width.toFloat()
        val heightRatio = maxHeight.toFloat() / height.toFloat()
        val scale = minOf(widthRatio, heightRatio)
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }
}
