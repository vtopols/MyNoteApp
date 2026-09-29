package com.example.note.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.note.screens.Reminder
import com.example.note.screens.ReminderStatus
import com.example.note.screens.RemindersStorage
import java.util.Calendar

class UpdateReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val reminderId = inputData.getInt("reminderId", -1)
            val action = inputData.getString("action") ?: return Result.failure()
            val hours = inputData.getInt("hours", 0)

            val storage = RemindersStorage(applicationContext)
            val reminders = storage.loadReminders().toMutableList()
            val index = reminders.indexOfFirst { it.id == reminderId }

            if (index != -1) {
                val oldReminder = reminders[index]
                val updatedReminder = when (action) {
                    "complete" -> oldReminder.copy(status = ReminderStatus.COMPLETED)
                    "snooze" -> {
                        val newDateTime = oldReminder.dateTime.clone() as Calendar
                        newDateTime.add(Calendar.HOUR_OF_DAY, hours)
                        oldReminder.copy(
                            dateTime = newDateTime,
                            status = ReminderStatus.SNOOZED
                        )
                    }
                    else -> oldReminder
                }
                reminders[index] = updatedReminder
                storage.saveReminders(reminders)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}