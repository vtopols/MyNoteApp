package com.example.note.screens

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.Calendar

class DatesStorage(private val context: Context) {

    private val fileName = "important_dates.json"
    private val gson = Gson()

    fun saveDates(dates: List<ImportantDate>) {
        try {
            val datesForSave = dates.map { date ->
                DateData(
                    id = date.id,
                    title = date.title,
                    timestamp = date.date.timeInMillis,
                    description = date.description
                )
            }
            val jsonString = gson.toJson(datesForSave)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadDates(): List<ImportantDate> {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) return emptyList()

            val jsonString = file.readText()
            val type = object : TypeToken<List<DateData>>() {}.type
            val datesData: List<DateData> = gson.fromJson(jsonString, type) ?: emptyList()

            datesData.map { data ->
                ImportantDate(
                    id = data.id,
                    title = data.title,
                    date = Calendar.getInstance().apply { timeInMillis = data.timestamp },
                    description = data.description
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    data class DateData(
        val id: Int,
        val title: String,
        val timestamp: Long,
        val description: String
    )
}