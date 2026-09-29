Многофункциональное приложение для ведения заметок, организованных по темам (папкам), с поддержкой чек-листов, важных дат и системы напоминаний. 
Написано на Kotlin с использованием Jetpack Compose и Material 3.

Скриншоты в папке Screenshots

Язык: Kotlin
UI: Jetpack Compose, Material 3
Архитектура: MVVM (ViewModel + StateFlow)
Асинхронность: Coroutines, StateFlow
Навигация: `NavigationSuiteScaffold` + собственное состояние навигации внутри `NotesViewModel`
Хранение данных: JSON-файлы во внутреннем хранилище приложения + Gson
Фоновая работа: WorkManager (`OneTimeWorkRequest` с `setInitialDelay`)
Уведомления: NotificationCompat, NotificationChannel, PendingIntent, `BroadcastReceiver` для действий из уведомления
Разрешения: POST_NOTIFICATIONS (Android 13+)
Настройки: SharedPreferences
