package com.example.note.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", 0)
        val reminderId = intent.getIntExtra("reminderId", -1)
        val title = intent.getStringExtra("title") ?: ""
        val message = intent.getStringExtra("message") ?: ""
        val type = intent.getStringExtra("type") ?: "reminder"
        val dateMillis = intent.getLongExtra("dateMillis", 0)

        // Закрываем текущее уведомление
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(notificationId)

        when (intent.action) {
            "ACTION_DONE" -> {
                if (reminderId != -1) {
                    val updateWork = OneTimeWorkRequestBuilder<UpdateReminderWorker>()
                        .setInputData(workDataOf(
                            "reminderId" to reminderId,
                            "action" to "complete"
                        ))
                        .build()
                    WorkManager.getInstance(context).enqueue(updateWork)
                }
            }
            "ACTION_SNOOZE_1H" -> {
                if (reminderId != -1) {
                    val updateWork = OneTimeWorkRequestBuilder<UpdateReminderWorker>()
                        .setInputData(workDataOf(
                            "reminderId" to reminderId,
                            "action" to "snooze",
                            "hours" to 1
                        ))
                        .build()
                    WorkManager.getInstance(context).enqueue(updateWork)
                }
                scheduleSnooze(context, title, message, type, dateMillis, 1, notificationId, reminderId)
            }
            "ACTION_SNOOZE_3H" -> {
                if (reminderId != -1) {
                    val updateWork = OneTimeWorkRequestBuilder<UpdateReminderWorker>()
                        .setInputData(workDataOf(
                            "reminderId" to reminderId,
                            "action" to "snooze",
                            "hours" to 3
                        ))
                        .build()
                    WorkManager.getInstance(context).enqueue(updateWork)
                }
                scheduleSnooze(context, title, message, type, dateMillis, 3, notificationId, reminderId)
            }
        }

        // Отправляем широковещательное сообщение для обновления UI
        val updateIntent = Intent("REFRESH_REMINDERS")
        context.sendBroadcast(updateIntent)
    }

    private fun scheduleSnooze(
        context: Context,
        title: String,
        message: String,
        type: String,
        dateMillis: Long,
        hours: Int,
        originalId: Int,
        reminderId: Int
    ) {
        val snoozeWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(hours.toLong(), TimeUnit.HOURS)
            .setInputData(workDataOf(
                "reminderId" to reminderId,
                "title" to title,
                "message" to message,
                "type" to type,
                "date" to dateMillis + (hours * 3600000L),
                "notificationId" to originalId + 1000
            ))
            .build()

        WorkManager.getInstance(context).enqueue(snoozeWork)
    }
}