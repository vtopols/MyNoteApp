package com.example.note.screens

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class NotesStorage(private val context: Context) {

    private val fileName = "notes_data.json"
    private val gson = Gson()

    fun saveFolders(folders: List<NoteFolder>) {
        try {
            val jsonString = gson.toJson(folders)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadFolders(): List<NoteFolder> {
        return try {
            val file = File(context.filesDir, fileName)
            if (!file.exists()) return emptyList()

            val jsonString = file.readText()
            val type = object : TypeToken<List<NoteFolder>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}