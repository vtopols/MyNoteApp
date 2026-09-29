package com.example.note.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.note.ui.theme.AppAccentColor
import com.example.note.ui.theme.LocalAppColorScheme
import com.example.note.utils.ThemeManager

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val colors = LocalAppColorScheme.current

    // Инициализируем ThemeManager при первом запуске
    LaunchedEffect(Unit) {
        ThemeManager.init(context)
    }

    var isDark by remember { mutableStateOf(ThemeManager.isDark) }
    var selectedAccent by remember { mutableStateOf(ThemeManager.accentColor) }

    Surface(modifier = modifier.fillMaxSize(), color = colors.background) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Настройки",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Карточка выбора темы (тёмная/светлая)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Тема оформления",
                        color = colors.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = !isDark,
                            onClick = {
                                isDark = false
                                ThemeManager.setDarkTheme(context, false)
                            },
                            label = { Text("Светлая") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = isDark,
                            onClick = {
                                isDark = true
                                ThemeManager.setDarkTheme(context, true)
                            },
                            label = { Text("Тёмная") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Карточка выбора цвета оформления
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Цвет оформления",
                        color = colors.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppAccentColor.values().forEach { accent ->
                            val isSelected = selectedAccent == accent
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) accent.container.copy(alpha = 0.3f) else colors.surface)
                                    .clickable {
                                        selectedAccent = accent
                                        ThemeManager.setAccentColor(context, accent)
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(accent.color)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = accent.displayName,
                                        color = if (isSelected) accent.color else colors.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (isSelected) {
                                    Text(
                                        text = "✓",
                                        color = accent.color,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Карточка "О приложении"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "О приложении",
                        color = colors.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(text = "Версия 1.0.0", color = colors.onSurfaceVariant)
                    Text(
                        text = "Приложение для заметок, списков и напоминаний",
                        fontSize = 12.sp,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}