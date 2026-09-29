package com.example.note.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.note.ui.theme.LocalAppColorScheme

data class TaskProject(
    val id: Int,
    val name: String,
    val tasks: List<Task> = emptyList(),
    val dateCreated: Long = System.currentTimeMillis()
)

data class Task(
    val id: Int,
    val title: String,
    val weight: Int = 1,
    val isCompleted: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis()
)

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(factory = TasksViewModelFactory(LocalContext.current))
) {
    val colors = LocalAppColorScheme.current
    val projects by viewModel.projects.collectAsState()
    var showCreateProjectDialog by remember { mutableStateOf(false) }
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var showDeleteProjectDialog by remember { mutableStateOf(false) }
    var showDeleteTaskDialog by remember { mutableStateOf(false) }
    var selectedProject by remember { mutableStateOf<TaskProject?>(null) }
    var projectToDelete by remember { mutableStateOf<TaskProject?>(null) }
    var taskToDelete by remember { mutableStateOf<Pair<TaskProject, Task>?>(null) }
    var newProjectName by remember { mutableStateOf("") }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskWeight by remember { mutableStateOf(1) }

    if (showDeleteProjectDialog && projectToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteProjectDialog = false; projectToDelete = null },
            title = { Text("Удалить проект?", color = colors.error) },
            text = { Text("Все задачи в проекте \"${projectToDelete!!.name}\" будут удалены без возможности восстановления.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProject(projectToDelete!!.id)
                        showDeleteProjectDialog = false
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteProjectDialog = false; projectToDelete = null }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    if (showDeleteTaskDialog && taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteTaskDialog = false; taskToDelete = null },
            title = { Text("Удалить задачу?", color = colors.error) },
            text = { Text("Задача \"${taskToDelete!!.second.title}\" будет удалена.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTask(taskToDelete!!.first.id, taskToDelete!!.second.id)
                        showDeleteTaskDialog = false
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTaskDialog = false; taskToDelete = null }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    if (showCreateProjectDialog) {
        AlertDialog(
            onDismissRequest = { showCreateProjectDialog = false; newProjectName = "" },
            title = { Text("Новый проект") },
            text = {
                OutlinedTextField(
                    value = newProjectName,
                    onValueChange = { newProjectName = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjectName.isNotBlank()) {
                            viewModel.createProject(newProjectName)
                            newProjectName = ""
                            showCreateProjectDialog = false
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White)
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateProjectDialog = false; newProjectName = "" }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    if (showCreateTaskDialog && selectedProject != null) {
        AlertDialog(
            onDismissRequest = { showCreateTaskDialog = false; newTaskTitle = ""; newTaskWeight = 1 },
            title = { Text("Новая задача") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Вес (1-10):", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = newTaskWeight.toFloat(),
                        onValueChange = { newTaskWeight = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(thumbColor = colors.primary, activeTrackColor = colors.primary)
                    )
                    Text("Вес: $newTaskWeight", color = colors.primary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addTask(selectedProject!!.id, newTaskTitle, newTaskWeight)
                            newTaskTitle = ""
                            newTaskWeight = 1
                            showCreateTaskDialog = false
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White)
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTaskDialog = false; newTaskTitle = ""; newTaskWeight = 1 }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    ConstraintLayout(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        val (contentRef, buttonRef) = createRefs()

        Box(
            modifier = Modifier
                .constrainAs(contentRef) {
                    top.linkTo(parent.top, margin = 16.dp)
                    bottom.linkTo(buttonRef.top, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
                .fillMaxWidth()
        ) {
            if (projects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = colors.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Нет проектов", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                        Text(text = "Нажмите \"Создать проект\" чтобы начать", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(projects, key = { it.id }) { project ->
                        TaskProjectCard(
                            project = project,
                            onAddTask = { selectedProject = project; showCreateTaskDialog = true },
                            onToggleTask = { task -> viewModel.toggleTask(project.id, task.id) },
                            onDeleteProject = { projectToDelete = project; showDeleteProjectDialog = true },
                            onDeleteTask = { task -> taskToDelete = Pair(project, task); showDeleteTaskDialog = true }
                        )
                    }
                }
            }
        }

        Button(
            onClick = { showCreateProjectDialog = true },
            modifier = Modifier
                .constrainAs(buttonRef) {
                    bottom.linkTo(parent.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth(0.91f)  // 90% от ширины экрана
                .height(48.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Добавить",
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Создать проект", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
    }
}

@Composable
fun TaskProjectCard(
    project: TaskProject,
    onAddTask: () -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteProject: () -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    val colors = LocalAppColorScheme.current
    var expanded by remember { mutableStateOf(false) }
    val totalWeight = project.tasks.sumOf { it.weight }
    val completedWeight = project.tasks.sumOf { if (it.isCompleted) it.weight else 0 }
    val progress = if (totalWeight > 0) completedWeight.toFloat() / totalWeight else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Заголовок проекта с обработкой короткого и долгого нажатия
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { expanded = !expanded },
                            onLongPress = { onDeleteProject() }
                        )
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть",
                    modifier = Modifier.size(24.dp),
                    tint = colors.primary
                )
            }

            // Прогресс-бар
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = colors.primary,
                trackColor = colors.onSurface.copy(alpha = 0.2f)
            )
            Text(
                text = "${(progress * 100).toInt()}% выполнено",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp),
                color = colors.onSurfaceVariant
            )

            // Список задач (разворачивается)
            if (expanded) {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    project.tasks.forEach { task ->
                        key(task.id) {
                            TaskItem(
                                task = task,
                                onToggleTask = { onToggleTask(task) },
                                onDeleteTask = { onDeleteTask(task) }
                            )
                        }
                    }
                    Button(
                        onClick = onAddTask,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(40.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryContainer, contentColor = colors.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Добавить задачу", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggleTask: () -> Unit,
    onDeleteTask: () -> Unit
) {
    val colors = LocalAppColorScheme.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleTask() },
                    colors = CheckboxDefaults.colors(checkedColor = colors.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 15.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.onSurface
                )
            }
            Text(
                text = "вес: ${task.weight}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(
                onClick = { onDeleteTask() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Удалить задачу",
                    modifier = Modifier.size(18.dp),
                    tint = colors.error
                )
            }
        }
    }
}