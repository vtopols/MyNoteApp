package com.example.note.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.note.workers.NotificationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Random
import java.util.concurrent.TimeUnit

data class Reminder(
    val id: Int,
    val title: String,
    val description: String = "",
    val dateTime: Calendar,
    val repeatType: RepeatType = RepeatType.NONE,
    val status: ReminderStatus = ReminderStatus.ACTIVE
)

enum class RepeatType {
    NONE, DAILY, WEEKLY, MONTHLY
}

enum class ReminderStatus {
    ACTIVE, SNOOZED, COMPLETED
}

class RemindersViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = RemindersStorage(application.applicationContext)
    private val workManager = WorkManager.getInstance(application)

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    init {
        loadReminders()
    }

    fun loadReminders() {
        viewModelScope.launch {
            try {
                _reminders.value = storage.loadReminders()
            } catch (e: Exception) {
                e.printStackTrace()
                _reminders.value = emptyList()
            }
        }
    }

    private fun saveReminders() {
        viewModelScope.launch {
            try {
                storage.saveReminders(_reminders.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun scheduleNotification(reminder: Reminder) {
        try {
            val now = System.currentTimeMillis()
            val targetTime = reminder.dateTime.timeInMillis

            if (targetTime > now && reminder.status == ReminderStatus.ACTIVE) {
                val delay = targetTime - now

                val notificationText = if (reminder.description.isNotEmpty()) {
                    reminder.description
                } else {
                    reminder.title
                }

                val randomId = Random().nextInt(10000) + reminder.id

                val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf(
                        "reminderId" to reminder.id,
                        "title" to reminder.title,
                        "message" to notificationText,
                        "type" to "reminder",
                        "date" to reminder.dateTime.timeInMillis,
                        "notificationId" to randomId
                    ))
                    .addTag("reminder_${reminder.id}")
                    .build()

                workManager.enqueueUniqueWork(
                    "reminder_${reminder.id}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addReminder(title: String, description: String, dateTime: Calendar, repeatType: RepeatType) {
        val newId = (_reminders.value.maxOfOrNull { it.id } ?: 0) + 1
        val newReminder = Reminder(newId, title, description, dateTime, repeatType)
        _reminders.update { it + newReminder }
        scheduleNotification(newReminder)
        saveReminders()
    }

    fun deleteReminder(id: Int) {
        workManager.cancelUniqueWork("reminder_$id")
        _reminders.update { it.filter { it.id != id } }
        saveReminders()
    }

    fun completeReminder(id: Int) {
        _reminders.update { list ->
            list.map { if (it.id == id) it.copy(status = ReminderStatus.COMPLETED) else it }
        }
        workManager.cancelUniqueWork("reminder_$id")
        saveReminders()
    }
}

class RemindersViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RemindersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RemindersViewModel(context.applicationContext as Application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}