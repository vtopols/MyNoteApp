package com.example.note.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = NotesStorage(application.applicationContext)

    private val _folders = MutableStateFlow<List<NoteFolder>>(emptyList())
    val folders: StateFlow<List<NoteFolder>> = _folders.asStateFlow()

    private val _navigationState = MutableStateFlow(NoteNavigationState())
    val navigationState: StateFlow<NoteNavigationState> = _navigationState.asStateFlow()

    private val _expandedStates = MutableStateFlow<MutableMap<Int, Boolean>>(mutableMapOf())
    val expandedStates: StateFlow<Map<Int, Boolean>> = _expandedStates.asStateFlow()

    val currentFolder: NoteFolder?
        get() = navigationState.value.currentFolderId?.let { folderId ->
            _folders.value.find { it.id == folderId }
        }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val loadedFolders = storage.loadFolders()
            _folders.value = loadedFolders
        }
    }

    private fun saveData() {
        viewModelScope.launch {
            storage.saveFolders(_folders.value)
        }
    }

    fun toggleExpanded(noteId: Int) {
        _expandedStates.update { current ->
            current.toMutableMap().apply {
                this[noteId] = !(this[noteId] ?: false)
            }.toMutableMap()
        }
    }

    fun createFolder(name: String) {
        val newId = (_folders.value.maxOfOrNull { it.id } ?: 0) + 1
        val newFolder = NoteFolder(
            id = newId,
            name = name,
            dateCreated = System.currentTimeMillis(),
            notes = emptyList()
        )
        _folders.update { it + newFolder }
        saveData()
    }

    fun deleteFolder(id: Int) {
        _folders.update { it.filter { it.id != id } }
        saveData()
        if (_navigationState.value.currentFolderId == id) {
            _navigationState.update { NoteNavigationState() }
        }
    }

    fun navigateToFolder(folderId: Int) {
        val folder = _folders.value.find { it.id == folderId }
        if (folder != null) {
            _navigationState.update { state ->
                state.copy(
                    currentFolderId = folderId,
                    folderPath = state.folderPath + folder
                )
            }
        }
    }

    fun navigateBack() {
        val currentPath = _navigationState.value.folderPath
        if (currentPath.isNotEmpty()) {
            val newPath = currentPath.dropLast(1)
            _navigationState.update {
                it.copy(
                    currentFolderId = newPath.lastOrNull()?.id,
                    folderPath = newPath
                )
            }
        } else {
            _navigationState.update { NoteNavigationState() }
        }
    }

    fun navigateToRoot() {
        _navigationState.update { NoteNavigationState() }
    }

    fun createNote(title: String, content: String, isChecklist: Boolean = false) {
        val currentFolderId = _navigationState.value.currentFolderId ?: return
        _folders.update { folders ->
            folders.map { folder ->
                if (folder.id == currentFolderId) {
                    val now = System.currentTimeMillis()
                    val newNote = Note(
                        id = now.toInt(),
                        title = title,
                        content = content,
                        dateCreated = now,
                        lastModified = now,
                        checklist = emptyList(),
                        isChecklist = isChecklist
                    )
                    folder.copy(notes = folder.notes + newNote)
                } else {
                    folder
                }
            }
        }
        saveData()
    }

    fun deleteNote(folderId: Int, noteId: Int) {
        _folders.update { folders ->
            folders.map { folder ->
                if (folder.id == folderId) {
                    folder.copy(notes = folder.notes.filter { it.id != noteId })
                } else {
                    folder
                }
            }
        }
        saveData()
    }

    fun updateNote(folderId: Int, noteId: Int, newTitle: String, newContent: String) {
        _folders.update { folders ->
            folders.map { folder ->
                if (folder.id == folderId) {
                    val updatedNotes = folder.notes.map { note ->
                        if (note.id == noteId) {
                            note.copy(
                                title = newTitle,
                                content = newContent,
                                lastModified = System.currentTimeMillis()
                            )
                        } else {
                            note
                        }
                    }
                    folder.copy(notes = updatedNotes)
                } else {
                    folder
                }
            }
        }
        saveData()
    }

    fun addChecklistItem(folderId: Int, noteId: Int, text: String) {
        _folders.update { folders ->
            folders.map { folder ->
                if (folder.id == folderId) {
                    val updatedNotes = folder.notes.map { note ->
                        if (note.id == noteId) {
                            val newId = (note.checklist.maxOfOrNull { it.id } ?: 0) + 1
                            val newItem = ChecklistItem(newId, text, false)
                            note.copy(
                                checklist = note.checklist + newItem,
                                lastModified = System.currentTimeMillis()
                            )
                        } else {
                            note
                        }
                    }
                    folder.copy(notes = updatedNotes)
                } else {
                    folder
                }
            }
        }
        saveData()
    }

    fun toggleChecklistItem(folderId: Int, noteId: Int, itemId: Int) {
        _folders.update { folders ->
            folders.map { folder ->
                if (folder.id == folderId) {
                    val updatedNotes = folder.notes.map { note ->
                        if (note.id == noteId) {
                            val updatedChecklist = note.checklist.map { item ->
                                if (item.id == itemId) {
                                    item.copy(isChecked = !item.isChecked)
                                } else {
                                    item
                                }
                            }
                            note.copy(
                                checklist = updatedChecklist,
                                lastModified = System.currentTimeMillis()
                            )
                        } else {
                            note
                        }
                    }
                    folder.copy(notes = updatedNotes)
                } else {
                    folder
                }
            }
        }
        saveData()
    }

    fun deleteChecklistItem(folderId: Int, noteId: Int, itemId: Int) {
        _folders.update { folders ->
            folders.map { folder ->
                if (folder.id == folderId) {
                    val updatedNotes = folder.notes.map { note ->
                        if (note.id == noteId) {
                            val updatedChecklist = note.checklist.filter { it.id != itemId }
                            note.copy(
                                checklist = updatedChecklist,
                                lastModified = System.currentTimeMillis()
                            )
                        } else {
                            note
                        }
                    }
                    folder.copy(notes = updatedNotes)
                } else {
                    folder
                }
            }
        }
        saveData()
    }

    fun updateChecklistItem(folderId: Int, noteId: Int, itemId: Int, newText: String) {
        _folders.update { folders ->
            folders.map { folder ->
                if (folder.id == folderId) {
                    val updatedNotes = folder.notes.map { note ->
                        if (note.id == noteId) {
                            val updatedChecklist = note.checklist.map { item ->
                                if (item.id == itemId) {
                                    item.copy(text = newText)
                                } else {
                                    item
                                }
                            }
                            note.copy(
                                checklist = updatedChecklist,
                                lastModified = System.currentTimeMillis()
                            )
                        } else {
                            note
                        }
                    }
                    folder.copy(notes = updatedNotes)
                } else {
                    folder
                }
            }
        }
        saveData()
    }
}

class NotesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(context.applicationContext as Application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}