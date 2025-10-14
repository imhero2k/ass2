package com.example.fit5046_app

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.fit5046_app.model.LogDisplayItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportDownloader {

    fun saveReportAsPdf(context: Context, logs: List<LogDisplayItem>): File? {
        if (logs.isEmpty()) {
            Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
            return null
        }

        val pdf = PdfDocument()
        val paint = Paint()
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

        var yPos = 80
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        // Title
        canvas.drawText("Health Report", 220f, yPos.toFloat(), titlePaint)
        yPos += 30
        val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on: $date", 40f, yPos.toFloat(), bodyPaint)
        yPos += 40

        var currentSection: String? = null
        logs.sortedBy { it.date }.forEach { log ->
            if (currentSection != log.type) {
                currentSection = log.type
                yPos += 25
                canvas.drawText(log.type.uppercase(Locale.getDefault()), 40f, yPos.toFloat(), sectionPaint)
                yPos += 15
                canvas.drawLine(40f, yPos.toFloat(), 555f, yPos.toFloat(), Paint().apply { strokeWidth = 1f })
                yPos += 20
            }

            val text = "• ${log.date}  |  ${log.summary}"
            if (yPos > 780) {
                // start new page
                pdf.finishPage(page)
                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdf.pages.size + 1).create()
                val newPage = pdf.startPage(newPageInfo)
                yPos = 80
                canvas.drawText("Health Report (cont.)", 200f, yPos.toFloat(), titlePaint)
                yPos += 30
                canvas.drawText("…continued", 40f, yPos.toFloat(), bodyPaint)
                yPos += 40
            }
            canvas.drawText(text, 50f, yPos.toFloat(), bodyPaint)
            yPos += 25
        }

        pdf.finishPage(page)

        // Save file
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "reports")
        if (!dir.exists()) dir.mkdirs()
        val fileName = "Health_Report_${System.currentTimeMillis()}.pdf"
        val file = File(dir, fileName)

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
