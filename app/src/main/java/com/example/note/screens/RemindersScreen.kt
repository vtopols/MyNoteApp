package com.example.note.screens

import android.app.DatePickerDialog
import androidx.compose.ui.text.style.TextOverflow
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.note.ui.theme.LocalAppColorScheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun RemindersScreen(
    modifier: Modifier = Modifier,
    viewModel: RemindersViewModel = viewModel(factory = RemindersViewModelFactory(LocalContext.current))
) {
    val colors = LocalAppColorScheme.current
    val reminders by viewModel.reminders.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedRepeat by remember { mutableStateOf(RepeatType.NONE) }

    if (showCreateDialog) {
        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }

        if (showDatePicker) {
            DatePickerDialog(
                LocalContext.current,
                { _, y, m, d ->
                    selectedDateTime.set(y, m, d)
                    showDatePicker = false
                    showTimePicker = true
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        if (showTimePicker) {
            TimePickerDialog(
                LocalContext.current,
                { _, h, min ->
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, h)
                    selectedDateTime.set(Calendar.MINUTE, min)
                    showTimePicker = false
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
            ).show()
        }

        AlertDialog(
            onDismissRequest = {
                showCreateDialog = false
                newTitle = ""
                newDescription = ""
                selectedRepeat = RepeatType.NONE
            },
            title = { Text("Новое напоминание") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Заголовок") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Описание (необязательно)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Дата и время:", color = colors.onSurfaceVariant)
                            Text(
                                text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru")).format(selectedDateTime.time),
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Повторение:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RepeatType.values().forEach { type ->
                            FilterChip(
                                selected = selectedRepeat == type,
                                onClick = { selectedRepeat = type },
                                label = {
                                    Text(
                                        when (type) {
                                            RepeatType.NONE -> "Нет"
                                            RepeatType.DAILY -> "день"
                                            RepeatType.WEEKLY -> "нед."
                                            RepeatType.MONTHLY -> "мес."
                                        },
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.addReminder(newTitle, newDescription, selectedDateTime, selectedRepeat)
                            newTitle = ""
                            newDescription = ""
                            selectedRepeat = RepeatType.NONE
                            showCreateDialog = false
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White)
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateDialog = false
                    newTitle = ""
                    newDescription = ""
                    selectedRepeat = RepeatType.NONE
                }) {
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
            if (reminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = colors.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Нет напоминаний", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                        Text("Нажмите \"Создать напоминание\" чтобы начать", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onDelete = { viewModel.deleteReminder(reminder.id) },
                            onComplete = { viewModel.completeReminder(reminder.id) }
                        )
                    }
                }
            }
        }

        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .constrainAs(buttonRef) {
                    bottom.linkTo(parent.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth(0.91f)  // 90% от ширины экрана
                .height(48.dp),      // Высота 48dp
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
            Text("Создать напоминание", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
    }
}

@Composable
fun ReminderCard(reminder: Reminder, onDelete: () -> Unit, onComplete: () -> Unit) {
    val colors = LocalAppColorScheme.current
    val timeUntil = reminder.dateTime.timeInMillis - System.currentTimeMillis()
    val daysUntil = TimeUnit.MILLISECONDS.toDays(timeUntil)
    val hoursUntil = TimeUnit.MILLISECONDS.toHours(timeUntil) % 24

    val statusText = when {
        reminder.status == ReminderStatus.COMPLETED -> "Выполнено"
        timeUntil < 0 -> "Просрочено"
        daysUntil > 0 -> "Через $daysUntil дн."
        hoursUntil > 0 -> "Через $hoursUntil ч."
        else -> "Скоро!"
    }

    val statusColor = when {
        reminder.status == ReminderStatus.COMPLETED -> colors.onSurfaceVariant
        timeUntil < 0 -> colors.error
        daysUntil == 0L && hoursUntil < 1 -> colors.primary
        else -> colors.onSurfaceVariant
    }

    var showDelete by remember { mutableStateOf(false) }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Удалить?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(
                    onClick = { onDelete(); showDelete = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(90.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = colors.primary, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.onSurface
                )
                if (reminder.description.isNotBlank()) {
                    Text(
                        reminder.description,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = colors.onSurfaceVariant
                    )
                }
                Row {
                    Text(
                        text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru")).format(reminder.dateTime.time),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = statusColor
                    )
                    if (reminder.repeatType != RepeatType.NONE) {
                        Text(
                            text = " • ${when (reminder.repeatType) {
                                RepeatType.DAILY -> "день"
                                RepeatType.WEEKLY -> "нед."
                                RepeatType.MONTHLY -> "мес."
                                else -> ""
                            }}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = colors.primary
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(statusText, style = MaterialTheme.typography.bodySmall, fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    if (reminder.status != ReminderStatus.COMPLETED && reminder.dateTime.timeInMillis > System.currentTimeMillis()) {
                        IconButton(onClick = onComplete, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Выполнить",
                                modifier = Modifier.size(16.dp),
                                tint = colors.primary
                            )
                        }
                    }
                    IconButton(onClick = { showDelete = true }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Удалить",
                            modifier = Modifier.size(16.dp),
                            tint = colors.error
                        )
                    }
                }
            }
        }
    }
}