package com.example.note.screens

data class NoteFolder(
    val id: Int,
    val name: String,
    val dateCreated: Long,
    val notes: List<Note> = emptyList()
)

data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val dateCreated: Long,
    val lastModified: Long,
    val checklist: List<ChecklistItem> = emptyList(),
    val isChecklist: Boolean = false
)

data class ChecklistItem(
    val id: Int,
    val text: String,
    val isChecked: Boolean = false
)

data class NoteNavigationState(
    val currentFolderId: Int? = null,
    val folderPath: List<NoteFolder> = emptyList()
)