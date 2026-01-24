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

object PdfReportGenerator {
    fun generateReport(
        context: Context,
        stageData: StageData,
        photoUri: String?
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
