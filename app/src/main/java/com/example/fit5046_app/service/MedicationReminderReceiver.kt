package com.example.fit5046_app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.fit5046_app.MainActivity
import com.example.fit5046_app.R

class MedicationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        println("DEBUG: MedicationReminderReceiver triggered!")
        val medicationName = intent.getStringExtra("medicationName") ?: "Medication"
        val dosage = intent.getStringExtra("dosage") ?: ""
        val reminderType = intent.getStringExtra("reminderType") ?: "MEDICATION"

        println("DEBUG: Showing notification for $medicationName - $dosage")
        showNotification(context, medicationName, dosage, reminderType)
    }

    private fun showNotification(context: Context, medicationName: String, dosage: String, reminderType: String) {
        println("DEBUG: Creating notification for $medicationName")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "medication_reminders",
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for taking medication"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
            println("DEBUG: Created notification channel")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "medication_reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💊 Time for your medication!")
            .setContentText("$medicationName${if (dosage.isNotEmpty()) " - $dosage" else ""}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time to take your $medicationName${if (dosage.isNotEmpty()) " ($dosage)" else ""}. Don't forget to log this in your health diary!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .build()

        println("DEBUG: About to show notification with ID: ${medicationName.hashCode()}")
        notificationManager.notify(medicationName.hashCode(), notification)
        println("DEBUG: Notification should now be visible!")
    }
}
