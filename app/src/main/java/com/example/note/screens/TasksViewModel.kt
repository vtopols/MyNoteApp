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

class TasksViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = TasksStorage(application.applicationContext)
    private val _projects = MutableStateFlow<List<TaskProject>>(emptyList())
    val projects: StateFlow<List<TaskProject>> = _projects.asStateFlow()

    init { loadProjects() }

    private fun loadProjects() {
        viewModelScope.launch { _projects.value = storage.loadProjects() }
    }

    private fun saveProjects() {
        viewModelScope.launch { storage.saveProjects(_projects.value) }
    }

    fun createProject(name: String) {
        val newId = (_projects.value.maxOfOrNull { it.id } ?: 0) + 1
        _projects.update { it + TaskProject(newId, name) }
        saveProjects()
    }

    fun deleteProject(projectId: Int) {
        _projects.update { projects ->
            projects.filter { it.id != projectId }
        }
        saveProjects()
    }

    fun addTask(projectId: Int, title: String, weight: Int) {
        _projects.update { projects ->
            projects.map { project ->
                if (project.id == projectId) {
                    val newId = (project.tasks.maxOfOrNull { it.id } ?: 0) + 1
                    project.copy(tasks = project.tasks + Task(newId, title, weight))
                } else project
            }
        }
        saveProjects()
    }

    fun deleteTask(projectId: Int, taskId: Int) {
        _projects.update { projects ->
            projects.map { project ->
                if (project.id == projectId) {
                    project.copy(tasks = project.tasks.filter { it.id != taskId })
                } else {
                    project
                }
            }
        }
        saveProjects()
    }

    fun toggleTask(projectId: Int, taskId: Int) {
        _projects.update { projects ->
            projects.map { project ->
                if (project.id == projectId) {
                    project.copy(tasks = project.tasks.map { task ->
                        if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
                    })
                } else project
            }
        }
        saveProjects()
    }
}

class TasksViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(context.applicationContext as Application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}