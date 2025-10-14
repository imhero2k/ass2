package com.example.fit5046_app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.fit5046_app.model.MedicationReminder
import java.util.*


class MedicationReminderService {
    companion object {
        private const val REQUEST_CODE_BASE = 1000

        fun scheduleReminder(context: Context, reminder: MedicationReminder) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
                putExtra("medicationName", reminder.medicationName)
                putExtra("dosage", reminder.dosage)
                putExtra("reminderType", reminder.reminderType.name)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // For testing, schedule for 1 minute from now
            val testCalendar = Calendar.getInstance().apply {
                add(Calendar.MINUTE, 1) // Schedule for 1 minute from now
            }

            println("DEBUG: Scheduling alarm for ${reminder.medicationName} at ${testCalendar.time}")
            println("DEBUG: Current time: ${Calendar.getInstance().time}")

            // Use setExactAndAllowWhileIdle for more reliable alarms
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                testCalendar.timeInMillis,
                pendingIntent
            )
        }

        fun cancelReminder(context: Context, reminderId: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MedicationReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }

    }
}
