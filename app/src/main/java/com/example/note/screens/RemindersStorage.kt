package com.example.note.screens

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.Calendar

class RemindersStorage(private val context: Context) {
    private val fileName = "reminders_data.json"
    private val gson = Gson()

    fun saveReminders(reminders: List<Reminder>) {
        try {
            val remindersForSave = reminders.map { reminder ->
                ReminderData(
                    id = reminder.id,
                    title = reminder.title,
                    description = reminder.description,
                    timestamp = reminder.dateTime.timeInMillis,
                    repeatType = reminder.repeatType.name,
                    status = reminder.status.name
                )
            }
            val jsonString = gson.toJson(remindersForSave)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadReminders(): List<Reminder> {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) return emptyList()

            val jsonString = file.readText()
            val type = object : TypeToken<List<ReminderData>>() {}.type
            val remindersData: List<ReminderData> = gson.fromJson(jsonString, type) ?: emptyList()

            remindersData.map { data ->
                Reminder(
                    id = data.id,
                    title = data.title,
                    description = data.description,
                    dateTime = Calendar.getInstance().apply { timeInMillis = data.timestamp },
                    repeatType = try { RepeatType.valueOf(data.repeatType) } catch (e: Exception) { RepeatType.NONE },
                    status = try { ReminderStatus.valueOf(data.status) } catch (e: Exception) { ReminderStatus.ACTIVE }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    data class ReminderData(
        val id: Int,
        val title: String,
        val description: String,
        val timestamp: Long,
        val repeatType: String,
        val status: String
    )
}