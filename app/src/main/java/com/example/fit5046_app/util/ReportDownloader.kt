package com.example.fit5046_app.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.fit5046_app.model.LogDisplayItem
import com.example.fit5046_app.model.LogType
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import java.util.*

object ReportDownloader {

    fun saveReportAsPdf(context: Context, logs: List<LogDisplayItem>): File? {
        if (logs.isEmpty()) {
            Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
            return null
        }

        // --- Setup PDF ---
        val pdf = PdfDocument()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            textSize = 20f
        }
        val sectionPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 16f
        }
        val bodyPaint = Paint().apply {
            textSize = 14f
        }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        var page = pdf.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = 80

        // --- Title ---
        canvas.drawText("Health Report", 220f, yPos.toFloat(), titlePaint)
        yPos += 30
        val date = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").format(java.time.LocalDateTime.now())
        canvas.drawText("Generated on: $date", 40f, yPos.toFloat(), bodyPaint)
        yPos += 40

        // --- Loop through logs grouped by type ---
        var currentSection: LogType? = null
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")

        logs.sortedBy { it.dateTime }.forEach { log ->
            // Section header (Diet, Exercise, etc.)
            if (currentSection != log.type) {
                currentSection = log.type
                yPos += 25
                canvas.drawText(
                    log.type.displayName.uppercase(Locale.getDefault()),
                    40f,
                    yPos.toFloat(),
                    sectionPaint
                )
                yPos += 15
                canvas.drawLine(
                    40f,
                    yPos.toFloat(),
                    555f,
                    yPos.toFloat(),
                    Paint().apply { strokeWidth = 1f }
                )
                yPos += 20
            }

            // Format the date nicely
            val formattedDate = log.dateTime.format(formatter)

            // --- Build a descriptive line depending on type ---
            val details = when (log) {
                is LogDisplayItem.DietItem -> {
                    val cals = log.calories?.toString() ?: "-"
                    val carbs = log.carbGrams?.toString() ?: "-"
                    "Calories: $cals | Carbs: $carbs g"
                }
                is LogDisplayItem.GlucoseItem -> "Glucose: ${log.value} mmol/L"
                is LogDisplayItem.ExerciseItem ->
                    "Exercise: ${log.exerciseType.displayName}, ${log.durationMinutes} min, ${log.intensity.displayName}"
                is LogDisplayItem.MedicationItem ->
                    "Medication: ${log.name}, ${log.dosage}${log.dosageUnit}"
            }

            val notes = log.notes?.let { "Notes: $it" } ?: ""
            val textLine = "• $formattedDate | $details $notes"

            // --- Handle page overflow ---
            if (yPos > 780) {
                pdf.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdf.pages.size + 1).create()
                page = pdf.startPage(newPageInfo)
                canvas = page.canvas
                yPos = 80
                canvas.drawText("Health Report (cont.)", 200f, yPos.toFloat(), titlePaint)
                yPos += 40
            }

            // --- Draw log line ---
            canvas.drawText(textLine, 50f, yPos.toFloat(), bodyPaint)
            yPos += 25
        }

        pdf.finishPage(page)

        // --- Save file ---
//        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "reports")
//        if (!dir.exists()) dir.mkdirs()
//        val fileName = "Health_Report_${System.currentTimeMillis()}.pdf"
//        val file = File(dir, fileName)

        // --- Save to Public Downloads folder ---
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val fileName = "Health_Report_${System.currentTimeMillis()}.pdf"
        val file = File(downloadsDir, fileName)

        return try {
            FileOutputStream(file).use { pdf.writeTo(it) }
            pdf.close()
            Toast.makeText(context, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
            file
        } catch (e: Exception) {
            pdf.close()
            e.printStackTrace()
            Toast.makeText(context, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }
}
