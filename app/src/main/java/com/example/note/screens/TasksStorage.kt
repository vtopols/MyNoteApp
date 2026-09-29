package com.example.note.screens

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class TasksStorage(private val context: Context) {
    private val fileName = "tasks_data.json"
    private val gson = Gson()

    fun saveProjects(projects: List<TaskProject>) {
        try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(gson.toJson(projects).toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadProjects(): List<TaskProject> {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) return emptyList()
            val type = object : TypeToken<List<TaskProject>>() {}.type
            gson.fromJson(file.readText(), type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}