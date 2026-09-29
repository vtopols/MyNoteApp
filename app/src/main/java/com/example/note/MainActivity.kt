package com.example.note

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.note.screens.*
import com.example.note.ui.theme.LocalAppColorScheme
import com.example.note.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        enableEdgeToEdge()

        // Запрашиваем разрешение на уведомления для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        setContent {
            MyApplicationTheme {
                MyApplicationApp(onBackPressed = { finish() })
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector
) {
    NOTES("Заметки", Icons.Default.EditNote),
    DATES("Даты", Icons.Default.DateRange),
    TASKS("Задачи", Icons.Default.CheckCircle),
    REMINDERS("Напомни", Icons.Default.Notifications)
    // SETTINGS удалён - будет в TopBar
}

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationApp(
    onBackPressed: () -> Unit,
    notesViewModel: NotesViewModel = viewModel(factory = NotesViewModelFactory(LocalContext.current))
) {
    val colors = LocalAppColorScheme.current
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.NOTES) }
    var notesNavigationEvent by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    // Состояние для отслеживания глубины навигации в заметках
    val navigationState by notesViewModel.navigationState.collectAsStateWithLifecycle()
    val isInFolder = navigationState.currentFolderId != null

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val topBarHeight = screenHeight * 0.09f

    // Обработка системной кнопки "Назад"
    BackHandler(enabled = true) {
        when {
            showSettings -> showSettings = false
            isInFolder -> notesViewModel.navigateBack()
            else -> onBackPressed()
        }
    }

    // Если открыты настройки - показываем только их
    if (showSettings) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = colors.background,
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(topBarHeight),
                    color = colors.primaryContainer
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        Text(
                            text = "Настройки",
                            color = colors.onSurface,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        // Кнопка закрытия настроек
                        IconButton(
                            onClick = { showSettings = false },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Закрыть",
                                tint = colors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        ) { scaffoldPadding ->
            SettingsScreen(
                modifier = Modifier.padding(scaffoldPadding)
            )
        }
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach { destination ->
                    item(
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.label,
                                tint = colors.primary
                            )
                        },
                        label = { Text(destination.label, color = colors.primary) },
                        selected = destination == currentDestination,
                        onClick = {
                            if (destination == AppDestinations.NOTES && destination == currentDestination) {
                                notesNavigationEvent = !notesNavigationEvent
                            }
                            currentDestination = destination
                        }
                    )
                }
            },
            containerColor = colors.background
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = colors.background,
                topBar = {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(topBarHeight),
                        color = colors.primaryContainer
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                            Text(
                                text = currentDestination.label,
                                color = colors.onSurface,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            // Кнопка настроек - отступ снизу 0
                            IconButton(
                                onClick = { showSettings = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Настройки",
                                    tint = colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            ) { scaffoldPadding ->
                when (currentDestination) {
                    AppDestinations.NOTES -> NotesScreen(
                        modifier = Modifier.padding(scaffoldPadding),
                        navigationEvent = notesNavigationEvent,
                        viewModel = notesViewModel
                    )
                    AppDestinations.DATES -> DatesScreen(
                        modifier = Modifier.padding(scaffoldPadding)
                    )
                    AppDestinations.TASKS -> TasksScreen(
                        modifier = Modifier.padding(scaffoldPadding)
                    )
                    AppDestinations.REMINDERS -> RemindersScreen(
                        modifier = Modifier.padding(scaffoldPadding)
                    )
                }
            }
        }
    }
}