package com.example.fit5046_app.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import com.example.fit5046_app.GlucoseAnalysis
import com.example.fit5046_app.DietAnalysis
import com.example.fit5046_app.ExerciseAnalysis
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ReportPdfGenerator {

    fun generateHealthReport(
        context: Context,
        glucoseAnalysis: GlucoseAnalysis,
        dietAnalysis: DietAnalysis,
        exerciseAnalysis: ExerciseAnalysis
    ): File? {
        return try {
            Log.d("ReportPdfGenerator", "DEBUG: Starting PDF generation...")
            val pdfDocument = PdfDocument()
            var pageNumber = 1
            val pageWidth = 595  // A4 width in px
            val pageHeight = 842 // A4 height in px

            Log.d("ReportPdfGenerator", "DEBUG: Creating page 1...")
            // Page 1: Title and Health Correlation Summary
            var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            var canvas = page.canvas
            var yPosition = 40

            // Title
            canvas.drawText("Health Report", 50f, yPosition.toFloat(), createTitlePaint())
            yPosition += 40
            canvas.drawText("7-Day Health Analysis", 50f, yPosition.toFloat(), createSubtitlePaint())
            yPosition += 30
            canvas.drawText("Generated: ${getCurrentDate()}", 50f, yPosition.toFloat(), createBodyPaint())
            yPosition += 50

            // Health Correlation Overview
            canvas.drawText("Health Correlation Analysis", 50f, yPosition.toFloat(), createHeaderPaint())
            yPosition += 30
            canvas.drawText("This report shows the relationship between your glucose levels,", 50f, yPosition.toFloat(), createBodyPaint())
            yPosition += 20
            canvas.drawText("diet intake, and exercise patterns over the past 7 days.", 50f, yPosition.toFloat(), createBodyPaint())
            yPosition += 50

            // Blood Glucose Summary
            canvas.drawText("Blood Glucose Summary", 50f, yPosition.toFloat(), createHeaderPaint())
            yPosition += 30
            canvas.drawText("Average Glucose: ${String.format("%.1f", glucoseAnalysis.averageGlucose)} mmol/L", 70f, yPosition.toFloat(), createBodyPaint())
            yPosition += 25
            canvas.drawText("Good Readings: ${glucoseAnalysis.goodReadings}", 70f, yPosition.toFloat(), createBodyPaint())
            yPosition += 25
            canvas.drawText("High Readings: ${glucoseAnalysis.highReadings}", 70f, yPosition.toFloat(), createBodyPaint())
            yPosition += 25
            canvas.drawText("Low Readings: ${glucoseAnalysis.lowReadings}", 70f, yPosition.toFloat(), createBodyPaint())
            yPosition += 25
            canvas.drawText("Total Readings: ${glucoseAnalysis.totalReadings}", 70f, yPosition.toFloat(), createBodyPaint())
            yPosition += 50

            // Diet Summary
            canvas.drawText("Diet Summary", 50f, yPosition.toFloat(), createHeaderPaint())
            yPosition += 30
            canvas.drawText("Average Daily Calories: ${dietAnalysis.averageDailyCalories} cal/day", 70f, yPosition.toFloat(), createBodyPaint())
            yPosition += 25
            canvas.drawText("Average Carbs: ${String.format("%.1f", dietAnalysis.averageCarbs)} g", 70f, yPosition.toFloat(), createBodyPaint())
            yPosition += 25
            canvas.drawText("Average Protein: ${String.format("%.1f", dietAnalysis.averageProtein)} g", 70f, yPosition.toFloat(), createBodyPaint())
            yPosition += 25
            canvas.drawText("Average Fats: ${String.format("%.1f", dietAnalysis.averageFats)} g", 70f, yPosition.toFloat(), createBodyPaint())
            yPosition += 50

            // Exercise Summary
            canvas.drawText("Exercise Summary", 50f, yPosition.toFloat(), createHeaderPaint())
            yPosition += 30
            canvas.drawText("Total Minutes (7 days): ${exerciseAnalysis.totalMinutes} minutes", 70f, yPosition.toFloat(), createBodyPaint())
            yPosition += 25
            canvas.drawText("Average per Day: ${String.format("%.1f", exerciseAnalysis.averageMinutesPerDay)} minutes", 70f, yPosition.toFloat(), createBodyPaint())

            pdfDocument.finishPage(page)
            pageNumber++

            Log.d("ReportPdfGenerator", "DEBUG: Creating page 2...")
            // Page 2: Detailed Daily Breakdown
            page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            yPosition = 40

            canvas.drawText("Detailed Daily Analysis", 50f, yPosition.toFloat(), createTitlePaint())
            yPosition += 50

            // Daily Glucose Breakdown
            canvas.drawText("Daily Glucose Readings", 50f, yPosition.toFloat(), createHeaderPaint())
            yPosition += 30

            glucoseAnalysis.dailyAverages.forEach { daily ->
                val glucoseValue = if (daily.average != null) {
                    String.format("%.1f mmol/L", daily.average)
                } else {
                    "No data"
                }
                canvas.drawText("${daily.date}: $glucoseValue", 70f, yPosition.toFloat(), createBodyPaint())
                yPosition += 20
                if (yPosition > 750) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    canvas = page.canvas
                    yPosition = 40
                }
            }

            yPosition += 30

            // Daily Diet Breakdown
            canvas.drawText("Daily Calorie Intake", 50f, yPosition.toFloat(), createHeaderPaint())
            yPosition += 30

            dietAnalysis.dailyCalories.forEach { daily ->
                canvas.drawText("${daily.date}: ${daily.calories} calories", 70f, yPosition.toFloat(), createBodyPaint())
                yPosition += 20
                if (yPosition > 750) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    canvas = page.canvas
                    yPosition = 40
                }
            }

            yPosition += 30

            // Daily Exercise Breakdown
            canvas.drawText("Daily Exercise", 50f, yPosition.toFloat(), createHeaderPaint())
            yPosition += 30

            exerciseAnalysis.dailyMinutes.forEach { daily ->
                canvas.drawText("${daily.date}: ${daily.minutes} minutes", 70f, yPosition.toFloat(), createBodyPaint())
                yPosition += 20
                if (yPosition > 750) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    canvas = page.canvas
                    yPosition = 40
                }
            }

            pdfDocument.finishPage(page)

            Log.d("ReportPdfGenerator", "DEBUG: Creating page 3...")
            // Page 3: Exercise Types and Recommendations
            page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            yPosition = 40
            pageNumber++

            canvas.drawText("Exercise Breakdown", 50f, yPosition.toFloat(), createTitlePaint())
            yPosition += 50

            canvas.drawText("Exercise Types (Past 7 Days):", 50f, yPosition.toFloat(), createHeaderPaint())
            yPosition += 30

            if (exerciseAnalysis.exerciseTypes.isNotEmpty()) {
                exerciseAnalysis.exerciseTypes.forEach { (type, minutes) ->
                    canvas.drawText("${type.displayName}: $minutes minutes", 70f, yPosition.toFloat(), createBodyPaint())
                    yPosition += 25
                }
            } else {
                canvas.drawText("No exercise data available", 70f, yPosition.toFloat(), createBodyPaint())
                yPosition += 25
            }

            yPosition += 30

            // Health Insights
            canvas.drawText("Health Insights & Recommendations", 50f, yPosition.toFloat(), createHeaderPaint())
            yPosition += 30

            val recommendations = generateRecommendations(glucoseAnalysis, dietAnalysis, exerciseAnalysis)
            recommendations.forEach { recommendation ->
                canvas.drawText("• $recommendation", 70f, yPosition.toFloat(), createBodyPaint())
                yPosition += 25
                if (yPosition > 750) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    canvas = page.canvas
                    yPosition = 40
                }
            }

            pdfDocument.finishPage(page)

            Log.d("ReportPdfGenerator", "DEBUG: Writing PDF to file...")
            // Save PDF to Downloads folder (visible in Google Files)
            val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "HealthReports")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
                Log.d("ReportPdfGenerator", "DEBUG: Created downloads directory")
            }

            val fileName = "Health_Report_${System.currentTimeMillis()}.pdf"
            val file = File(downloadsDir, fileName)

            Log.d("ReportPdfGenerator", "DEBUG: File path: ${file.absolutePath}")

            // Use buffered output stream and properly close it
            file.outputStream().use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            pdfDocument.close()

            Log.d("ReportPdfGenerator", "DEBUG: PDF saved successfully to: ${file.absolutePath}")
            Log.d("ReportPdfGenerator", "DEBUG: File size: ${file.length()} bytes")

            file
        } catch (e: Exception) {
            Log.e("ReportPdfGenerator", "ERROR: PDF generation failed", e)
            e.printStackTrace()
            null
        }
    }

    private fun generateRecommendations(
        glucose: GlucoseAnalysis,
        diet: DietAnalysis,
        exercise: ExerciseAnalysis
    ): List<String> {
        val recommendations = mutableListOf<String>()

        // Glucose recommendations
        if (glucose.averageGlucose > 0) {
            when {
                glucose.averageGlucose < 4f -> recommendations.add("Your glucose levels are running low. Monitor carefully and consult your healthcare provider.")
                glucose.averageGlucose > 7f -> recommendations.add("Your glucose levels are elevated. Consider adjusting your diet and increasing exercise.")
                else -> recommendations.add("Your glucose levels are within a healthy range. Continue current management.")
            }
        }

        // High readings
        if (glucose.highReadings > glucose.goodReadings / 2) {
            recommendations.add("You have frequent high glucose readings. Track which meals cause spikes.")
        }

        // Exercise recommendations
        if (exercise.averageMinutesPerDay < 20) {
            recommendations.add("Aim for at least 30 minutes of exercise daily for better glucose control.")
        } else {
            recommendations.add("Great job maintaining regular exercise! Continue this routine.")
        }

        // Diet recommendations
        if (diet.averageDailyCalories > 2500) {
            recommendations.add("Your daily calorie intake is high. Consider portion control.")
        } else if (diet.averageDailyCalories > 0 && diet.averageDailyCalories < 1500) {
            recommendations.add("Your calorie intake is low. Ensure you're eating enough to maintain energy.")
        }

        // Macronutrient balance
        val totalMacros = diet.averageCarbs + diet.averageProtein + diet.averageFats
        if (totalMacros > 0) {
            val carbPercent = (diet.averageCarbs / totalMacros) * 100
            if (carbPercent > 60) {
                recommendations.add("Your diet is high in carbohydrates. Consider increasing protein and healthy fats.")
            }
        }

        return recommendations.take(5)
    }

    private fun createTitlePaint(): Paint = Paint().apply {
        textSize = 32f
        isFakeBoldText = true
        color = android.graphics.Color.BLACK
    }

    private fun createSubtitlePaint(): Paint = Paint().apply {
        textSize = 20f
        color = android.graphics.Color.DKGRAY
    }

    private fun createHeaderPaint(): Paint = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
        color = android.graphics.Color.BLACK
    }

    private fun createBodyPaint(): Paint = Paint().apply {
        textSize = 12f
        color = android.graphics.Color.BLACK
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
}