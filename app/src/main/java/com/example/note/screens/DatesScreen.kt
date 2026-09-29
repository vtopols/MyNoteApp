package com.example.note.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import kotlinx.coroutines.launch
import com.example.note.ui.theme.LocalAppColorScheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ImportantDate(
    val id: Int,
    val title: String,
    val date: Calendar,
    val description: String = ""
)

data class MonthGroup(
    val monthName: String,
    val dates: List<ImportantDate>
)

val monthNames = listOf(
    "ЯНВАРЬ", "ФЕВРАЛЬ", "МАРТ", "АПРЕЛЬ", "МАЙ", "ИЮНЬ",
    "ИЮЛЬ", "АВГУСТ", "СЕНТЯБРЬ", "ОКТЯБРЬ", "НОЯБРЬ", "ДЕКАБРЬ"
)

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DatesScreen(
    modifier: Modifier = Modifier,
    viewModel: DatesViewModel = viewModel(factory = DatesViewModelFactory(LocalContext.current))
) {
    val colors = LocalAppColorScheme.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val topPadding = screenHeight * 0.02f
    val horizontalPadding = screenWidth * 0.04f
    val betweenItems = 8.dp  // Фиксированный отступ
    val buttonBottomMargin = screenHeight * 0.02f
    val cardHorizontalPadding = screenWidth * 0.03f
    val dotsBottomMargin = screenHeight * 0.015f

    val dates by viewModel.dates.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newDateTitle by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var dateToDelete by remember { mutableStateOf<ImportantDate?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                }
                showDatePicker = false
                showAddDialog = true
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
        showDatePicker = false
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Новая дата") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newDateTitle,
                        onValueChange = { newDateTitle = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Дата: ${
                            SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
                                .format(Date(selectedDate.timeInMillis))
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDateTitle.isNotBlank()) {
                            viewModel.addDate(newDateTitle, selectedDate)
                            newDateTitle = ""
                            showAddDialog = false
                        }
                    },
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAddDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    if (showDeleteDialog && dateToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление") },
            text = { Text("Вы уверены, что хотите удалить дату \"${dateToDelete!!.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDate(dateToDelete!!.id)
                    showDeleteDialog = false
                    dateToDelete = null
                }) {
                    Text("Удалить", color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; dateToDelete = null }) {
                    Text("Отмена")
                }
            },
            shape = RoundedCornerShape(32.dp)
        )
    }

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endOfMonth = (today.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
    }

    val nearMonthDates = dates.filter {
        !it.date.before(today) && !it.date.after(endOfMonth)
    }.sortedBy { it.date.timeInMillis }

    val allDates = dates.sortedBy { it.date.timeInMillis }

    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding)
    ) {
        val (pagerRef, dotsRef, buttonRef) = createRefs()

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .constrainAs(pagerRef) {
                    top.linkTo(parent.top, margin = topPadding)
                    bottom.linkTo(dotsRef.top, margin = dotsBottomMargin)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }
        ) { page ->
            when (page) {
                0 -> DatesListContent(
                    title = "В этом месяце",
                    dates = nearMonthDates,
                    onDateClick = { },
                    onDateLongClick = { date ->
                        dateToDelete = date
                        showDeleteDialog = true
                    },
                    cardHorizontalPadding = cardHorizontalPadding,
                    betweenItems = betweenItems
                )
                1 -> DatesListContent(
                    title = "Все даты",
                    dates = allDates,
                    onDateClick = { },
                    onDateLongClick = { date ->
                        dateToDelete = date
                        showDeleteDialog = true
                    },
                    cardHorizontalPadding = cardHorizontalPadding,
                    betweenItems = betweenItems
                )
            }
        }

        Row(
            modifier = Modifier
                .constrainAs(dotsRef) {
                    bottom.linkTo(buttonRef.top, margin = buttonBottomMargin)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { page ->
                val dotColor = if (pagerState.currentPage == page) {
                    colors.primary
                } else {
                    colors.onSurface.copy(alpha = 0.3f)
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(page)
                            }
                        }
                )
            }
        }

        Button(
            onClick = { showDatePicker = true },
            modifier = Modifier
                .constrainAs(buttonRef) {
                    bottom.linkTo(parent.bottom, margin = buttonBottomMargin)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth(0.9f)  // 90% от ширины экрана
                .height(48.dp),      // Высота 48dp
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Добавить",
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Добавить дату",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun groupDatesByMonth(dates: List<ImportantDate>): List<MonthGroup> {
    if (dates.isEmpty()) return emptyList()
    val grouped = dates.groupBy { it.date.get(Calendar.MONTH) }
    return grouped.map { (monthIndex, datesInMonth) ->
        MonthGroup(monthName = monthNames[monthIndex], dates = datesInMonth.sortedBy { it.date.timeInMillis })
    }.sortedBy { it.dates.first().date.timeInMillis }
}

@Composable
fun DatesListContent(
    title: String,
    dates: List<ImportantDate>,
    onDateClick: (ImportantDate) -> Unit,
    onDateLongClick: (ImportantDate) -> Unit,
    cardHorizontalPadding: androidx.compose.ui.unit.Dp,
    betweenItems: androidx.compose.ui.unit.Dp
) {
    val colors = LocalAppColorScheme.current
    val groupedDates = remember(dates) { groupDatesByMonth(dates) }
    val borderColor = colors.onSurface.copy(alpha = 0.5f)
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 0.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onSurface,
            modifier = Modifier.padding(start = 0.dp, top = 0.dp, bottom = 12.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 0.dp),
            shape = RoundedCornerShape(32.dp),
            color = colors.secondaryContainer,
            tonalElevation = 2.dp
        ) {
            if (dates.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = colors.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Нет дат", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                        Text(text = "Нажмите \"Добавить дату\" чтобы создать", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(betweenItems)) {
                    items(groupedDates) { group ->
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp).padding(bottom = 16.dp)) {
                            Text(
                                text = group.monthName,
                                fontWeight = FontWeight.Normal,
                                fontSize = 26.sp,
                                letterSpacing = 0.5.sp,
                                textAlign = TextAlign.Center,
                                color = colors.onSurface,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                            )
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Transparent,
                                tonalElevation = 0.dp
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    group.dates.forEach { date ->
                                        DateItem(
                                            date = date,
                                            onClick = { onDateClick(date) },
                                            onLongClick = { onDateLongClick(date) },
                                            isToday = isSameDay(date.date, today),
                                            horizontalPadding = cardHorizontalPadding
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}

@Composable
fun DateItem(date: ImportantDate, onClick: () -> Unit, onLongClick: () -> Unit, isToday: Boolean, horizontalPadding: androidx.compose.ui.unit.Dp) {
    val colors = LocalAppColorScheme.current
    val backgroundColor = if (isToday) colors.primaryContainer else colors.surface
    val verticalPadding = if (isToday) 16.dp else 12.dp
    val dateTextSize = if (isToday) 20.sp else 16.sp
    val titleTextSize = if (isToday) 18.sp else 16.sp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date.title,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = titleTextSize,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = colors.onSurface
            )
            Text(
                text = SimpleDateFormat("dd", Locale("ru")).format(Date(date.date.timeInMillis)),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = dateTextSize,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) colors.primary else colors.onSurfaceVariant
            )
        }
    }
}

class DatesViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DatesViewModel(context.applicationContext as Application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}