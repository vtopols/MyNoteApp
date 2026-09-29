package com.example.note.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    navigationEvent: Boolean = false,
    viewModel: NotesViewModel = viewModel(factory = NotesViewModelFactory(LocalContext.current))
) {
    val colors = LocalAppColorScheme.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val topPadding = screenHeight * 0.02f
    val horizontalPadding = screenWidth * 0.04f
    val betweenItems = screenHeight * 0.01f
    val buttonBottomMargin = screenHeight * 0.02f
    val cardHorizontalPadding = screenWidth * 0.03f

    val folders by viewModel.folders.collectAsState()
    val navigationState by viewModel.navigationState.collectAsState()
    val expandedStates by viewModel.expandedStates.collectAsState()

    LaunchedEffect(navigationEvent) { if (navigationEvent) viewModel.navigateToRoot() }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showCreateNoteDialog by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditNoteDialog by remember { mutableStateOf(false) }
    var showAddChecklistDialog by remember { mutableStateOf(false) }

    var editingNoteId by remember { mutableStateOf<Int?>(null) }
    var noteForChecklist by remember { mutableStateOf<Note?>(null) }
    var folderIdForDelete by remember { mutableStateOf<Int?>(null) }
    var noteIdForDelete by remember { mutableStateOf<Int?>(null) }
    var isDeletingFolder by remember { mutableStateOf(false) }
    var newChecklistText by remember { mutableStateOf("") }
    var newFolderName by remember { mutableStateOf("") }
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }
    var editNoteTitle by remember { mutableStateOf("") }
    var editNoteContent by remember { mutableStateOf("") }

    val currentFolder = viewModel.currentFolder
    val editingNote = remember(editingNoteId, currentFolder?.notes) {
        if (editingNoteId != null && currentFolder != null) {
            currentFolder.notes.find { it.id == editingNoteId }
        } else null
    }

    LaunchedEffect(editingNote) {
        if (editingNote != null) {
            editNoteTitle = editingNote.title
            editNoteContent = editingNote.content
        }
    }

    // Диалог создания списка
    if (showCreateListDialog) {
        AlertDialog(
            onDismissRequest = { showCreateListDialog = false; newNoteTitle = "" },
            title = { Text("Новый список") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newNoteTitle,
                        onValueChange = { newNoteTitle = it },
                        label = { Text("Название списка") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Text(
                        text = "Пункты можно будет добавить позже",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNoteTitle.isNotBlank()) {
                            viewModel.createNote(newNoteTitle, "", isChecklist = true)
                            newNoteTitle = ""
                            showCreateListDialog = false
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White)
                ) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateListDialog = false; newNoteTitle = "" }) { Text("Отмена") }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    // Диалог добавления пункта в чек-лист
    if (showAddChecklistDialog && noteForChecklist != null && navigationState.currentFolderId != null) {
        AlertDialog(
            onDismissRequest = {
                showAddChecklistDialog = false
                newChecklistText = ""
                noteForChecklist = null
            },
            title = { Text("Добавить пункт в \"${noteForChecklist?.title}\"") },
            text = {
                OutlinedTextField(
                    value = newChecklistText,
                    onValueChange = { newChecklistText = it },
                    label = { Text("Текст пункта") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChecklistText.isNotBlank()) {
                            viewModel.addChecklistItem(
                                navigationState.currentFolderId!!,
                                noteForChecklist!!.id,
                                newChecklistText
                            )
                            newChecklistText = ""
                            showAddChecklistDialog = false
                            noteForChecklist = null
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White)
                ) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddChecklistDialog = false
                    newChecklistText = ""
                    noteForChecklist = null
                }) { Text("Отмена") }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    // Диалог создания папки
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false; newFolderName = "" },
            title = { Text("Новая тема") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Название темы") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.createFolder(newFolderName)
                            newFolderName = ""
                            showCreateFolderDialog = false
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White)
                ) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false; newFolderName = "" }) { Text("Отмена") }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    // Диалог создания заметки
    if (showCreateNoteDialog) {
        AlertDialog(
            onDismissRequest = { showCreateNoteDialog = false; newNoteTitle = ""; newNoteContent = "" },
            title = { Text("Новая заметка") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newNoteTitle,
                        onValueChange = { newNoteTitle = it },
                        label = { Text("Заголовок") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newNoteContent,
                        onValueChange = { newNoteContent = it },
                        label = { Text("Содержание") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        minLines = 3,
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNoteTitle.isNotBlank()) {
                            viewModel.createNote(newNoteTitle, newNoteContent, isChecklist = false)
                            newNoteTitle = ""; newNoteContent = ""; showCreateNoteDialog = false
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White)
                ) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateNoteDialog = false; newNoteTitle = ""; newNoteContent = "" }) { Text("Отмена") }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    // Диалог редактирования заметки
    if (showEditNoteDialog && editingNoteId != null && navigationState.currentFolderId != null) {
        AlertDialog(
            onDismissRequest = {
                showEditNoteDialog = false
                editingNoteId = null
                editNoteTitle = ""
                editNoteContent = ""
            },
            title = { Text("Редактировать заметку") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editNoteTitle,
                        onValueChange = { editNoteTitle = it },
                        label = { Text("Заголовок") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editNoteContent,
                        onValueChange = { editNoteContent = it },
                        label = { Text("Содержание") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        minLines = 3,
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNoteTitle.isNotBlank() && editingNoteId != null) {
                            viewModel.updateNote(
                                navigationState.currentFolderId!!,
                                editingNoteId!!,
                                editNoteTitle,
                                editNoteContent
                            )
                            showEditNoteDialog = false
                            editingNoteId = null
                            editNoteTitle = ""
                            editNoteContent = ""
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White)
                ) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditNoteDialog = false
                    editingNoteId = null
                    editNoteTitle = ""
                    editNoteContent = ""
                }) { Text("Отмена") }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    // Диалог удаления
    if (showDeleteDialog && (folderIdForDelete != null || noteIdForDelete != null)) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; folderIdForDelete = null; noteIdForDelete = null; isDeletingFolder = false },
            title = { Text(if (isDeletingFolder) "Удалить тему?" else "Удалить заметку?", color = colors.error) },
            text = { Text(if (isDeletingFolder) "Все заметки внутри темы также будут удалены." else "Вы уверены?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isDeletingFolder && folderIdForDelete != null) {
                            viewModel.deleteFolder(folderIdForDelete!!)
                        } else if (!isDeletingFolder && noteIdForDelete != null && navigationState.currentFolderId != null) {
                            viewModel.deleteNote(navigationState.currentFolderId!!, noteIdForDelete!!)
                        }
                        showDeleteDialog = false; folderIdForDelete = null; noteIdForDelete = null; isDeletingFolder = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; folderIdForDelete = null; noteIdForDelete = null; isDeletingFolder = false }) { Text("Отмена") }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    ConstraintLayout(modifier = modifier.fillMaxSize().padding(horizontal = horizontalPadding)) {
        val (contentRef, buttonRowRef) = createRefs()

        Box(modifier = Modifier.constrainAs(contentRef) {
            top.linkTo(parent.top, margin = topPadding)
            bottom.linkTo(buttonRowRef.top, margin = buttonBottomMargin)
            start.linkTo(parent.start); end.linkTo(parent.end)
            width = Dimension.fillToConstraints; height = Dimension.fillToConstraints
        }) {
            if (navigationState.currentFolderId == null) {
                if (folders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(64.dp), tint = colors.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Нет тем", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                            Text(text = "Нажмите \"Создать тему\" чтобы начать", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(betweenItems)) {
                        items(folders, key = { it.id }) { folder ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = cardHorizontalPadding)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = { viewModel.navigateToFolder(folder.id) },
                                            onLongPress = {
                                                folderIdForDelete = folder.id
                                                isDeletingFolder = true
                                                showDeleteDialog = true
                                            }
                                        )
                                    },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(colors.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(30.dp), tint = colors.primary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = folder.name, style = MaterialTheme.typography.titleMedium, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = colors.onSurface)
                                        Text(text = "${folder.notes.size} ${getWordEnding(folder.notes.size, "заметка", "заметки", "заметок")}", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                                    }
                                    Text(text = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date(folder.dateCreated)), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            } else {
                val folder = viewModel.currentFolder
                if (folder?.notes?.isEmpty() == true) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Note, contentDescription = null, modifier = Modifier.size(64.dp), tint = colors.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Нет заметок и списков", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                            Text(text = "Нажмите \"Заметку\" или \"Список\" чтобы добавить", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(betweenItems)) {
                        items(folder?.notes ?: emptyList(), key = { it.id }) { note ->
                            val isList = note.isChecklist
                            val isExpanded = expandedStates[note.id] ?: false

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = cardHorizontalPadding),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Заголовок
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .pointerInput(Unit) {
                                                detectTapGestures(
                                                    onTap = { viewModel.toggleExpanded(note.id) },
                                                    onLongPress = {
                                                        noteIdForDelete = note.id
                                                        isDeletingFolder = false
                                                        showDeleteDialog = true
                                                    }
                                                )
                                            }
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(
                                                imageVector = if (isList) Icons.Default.PlaylistAdd else Icons.Default.Note,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = colors.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = note.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 2,
                                                color = colors.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date(note.lastModified)),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colors.onSurfaceVariant,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                                                tint = colors.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    // Развёрнутый контент
                                    if (isExpanded) {
                                        // Контент обычной заметки с долгим нажатием для редактирования
                                        if (!isList && note.content.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = note.content,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.onSurfaceVariant,
                                                maxLines = Int.MAX_VALUE,
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp)
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(
                                                            onLongPress = {
                                                                editingNoteId = note.id
                                                                showEditNoteDialog = true
                                                            }
                                                        )
                                                    }
                                            )
                                        }

                                        // Если заметка без контента и не список - показываем подсказку
                                        if (!isList && note.content.isBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Нажмите и удерживайте для редактирования",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colors.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier
                                                    .padding(horizontal = 16.dp)
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(
                                                            onLongPress = {
                                                                editingNoteId = note.id
                                                                showEditNoteDialog = true
                                                            }
                                                        )
                                                    }
                                            )
                                        }

                                        // Пункты чек-листа
                                        if (isList && note.checklist.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                                note.checklist.forEach { item ->
                                                    key(item.id) {
                                                        ChecklistItemRow(
                                                            item = item,
                                                            folderId = navigationState.currentFolderId!!,
                                                            noteId = note.id,
                                                            viewModel = viewModel,
                                                            colors = colors
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Кнопка добавления пункта для списка
                                        if (isList) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    noteForChecklist = note
                                                    showAddChecklistDialog = true
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryContainer, contentColor = colors.primary)
                                            ) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить пункт", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Добавить пункт", fontSize = 14.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (navigationState.currentFolderId == null) {
            Button(
                onClick = { showCreateFolderDialog = true },
                modifier = Modifier
                    .constrainAs(buttonRowRef) {
                        bottom.linkTo(parent.bottom, margin = buttonBottomMargin)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth(0.9f)  // 90% от ширины экрана
                    .height(48.dp),      // Высота 48dp (было 56dp)
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить", modifier = Modifier.size(20.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Создать тему", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        } else {
            Row(
                modifier = Modifier
                    .constrainAs(buttonRowRef) {
                        bottom.linkTo(parent.bottom, margin = buttonBottomMargin)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth(0.9f),  // 90% от ширины экрана
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showCreateNoteDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),  // Высота 48dp (было 56dp)
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.Note, contentDescription = "Заметка", modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Заметка", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }

                Button(
                    onClick = { showCreateListDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),  // Высота 48dp (было 56dp)
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryContainer, contentColor = colors.primary)
                ) {
                    Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = "Список", modifier = Modifier.size(20.dp), tint = colors.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Список", style = MaterialTheme.typography.titleMedium, color = colors.primary)
                }
            }
        }
    }
}

@Composable
fun ChecklistItemRow(
    item: ChecklistItem,
    folderId: Int,
    noteId: Int,
    viewModel: NotesViewModel,
    colors: com.example.note.ui.theme.AppColorScheme
) {
    var checkedState by remember(item.id) { mutableStateOf(item.isChecked) }
    var isVisible by remember(item.id) { mutableStateOf(true) }

    LaunchedEffect(item.isChecked) {
        checkedState = item.isChecked
    }

    if (!isVisible) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checkedState,
            onCheckedChange = { isChecked ->
                checkedState = isChecked
                viewModel.toggleChecklistItem(folderId, noteId, item.id)
            },
            colors = CheckboxDefaults.colors(checkedColor = colors.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            textDecoration = if (checkedState) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f),
            color = colors.onSurface
        )
        IconButton(
            onClick = {
                isVisible = false
                viewModel.deleteChecklistItem(folderId, noteId, item.id)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Удалить",
                modifier = Modifier.size(18.dp),
                tint = colors.error
            )
        }
    }
}

fun getWordEnding(count: Int, one: String, two: String, five: String): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> one
        count % 10 in 2..4 && (count % 100 !in 12..14) -> two
        else -> five
    }
}