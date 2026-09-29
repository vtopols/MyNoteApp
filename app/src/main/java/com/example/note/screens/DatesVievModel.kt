package com.example.note.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

class DatesViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = DatesStorage(application.applicationContext)
    private val workManager = WorkManager.getInstance(application)

    private val _dates = MutableStateFlow<List<ImportantDate>>(emptyList())
    val dates: StateFlow<List<ImportantDate>> = _dates.asStateFlow()

    init {
        loadDates()
    }

    private fun loadDates() {
        viewModelScope.launch {
            try {
                val loadedDates = storage.loadDates()
                _dates.value = loadedDates
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveDates() {
        viewModelScope.launch {
            try {
                storage.saveDates(_dates.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun scheduleNotification(date: ImportantDate, type: String, notificationId: Int) {
        try {
            val now = System.currentTimeMillis()
            var targetCalendar = date.date.clone() as Calendar

            when (type) {
                "evening" -> {
                    targetCalendar.add(Calendar.DAY_OF_MONTH, -1)
                    targetCalendar.set(Calendar.HOUR_OF_DAY, 21)
                }
                "morning" -> {
                    targetCalendar.set(Calendar.HOUR_OF_DAY, 9)
                }
            }
            targetCalendar.set(Calendar.MINUTE, 0)
            targetCalendar.set(Calendar.SECOND, 0)

            val targetTime = targetCalendar.timeInMillis
            if (targetTime > now) {
                val delay = targetTime - now
                val work = OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf(
                        "title" to date.title,
                        "message" to date.description.ifEmpty { date.title },
                        "type" to type,
                        "date" to date.date.timeInMillis,
                        "notificationId" to notificationId
                    ))
                    .addTag("date_${date.id}_$type")
                    .build()
                workManager.enqueueUniqueWork("${type}_${date.id}", ExistingWorkPolicy.REPLACE, work)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addDate(title: String, date: Calendar) {
        try {
            val newId = (_dates.value.maxOfOrNull { it.id } ?: 0) + 1
            val newDate = ImportantDate(id = newId, title = title, date = date)
            _dates.update { it + newDate }

            val random = Random()
            scheduleNotification(newDate, "evening", random.nextInt(10000))
            scheduleNotification(newDate, "morning", random.nextInt(10000))

            saveDates()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteDate(id: Int) {
        try {
            workManager.cancelUniqueWork("evening_$id")
            workManager.cancelUniqueWork("morning_$id")
            _dates.update { it.filter { date -> date.id != id } }
            saveDates()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateDate(id: Int, title: String, date: Calendar) {
        try {
            workManager.cancelUniqueWork("evening_$id")
            workManager.cancelUniqueWork("morning_$id")
            _dates.update { list ->
                list.map { if (it.id == id) ImportantDate(id = id, title = title, date = date) else it }
            }
            val updatedDate = _dates.value.find { it.id == id }
            if (updatedDate != null) {
                val random = Random()
                scheduleNotification(updatedDate, "evening", random.nextInt(10000))
                scheduleNotification(updatedDate, "morning", random.nextInt(10000))
            }
            saveDates()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAllNotifications() {
        try {
            workManager.cancelAllWork()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}