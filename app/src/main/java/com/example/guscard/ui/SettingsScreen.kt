package com.example.guscard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.guscard.BuildConfig
import com.example.guscard.PrefsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    currentTheme: Int,
    onThemeChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Вид карт храним локально, так как он не требует перезапуска Activity
    var viewMode by remember { mutableStateOf(PrefsHelper.getViewMode(context)) }
    var startScreen by remember { mutableStateOf(PrefsHelper.getStartScreen(context)) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Настройки", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад", color = MaterialTheme.colorScheme.primary) } }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Блок: Внешний вид
            SettingsAccordion(title = "Внешний вид") {
                Text("Тема", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeChip("Светлая", currentTheme == PrefsHelper.THEME_LIGHT) {
                        onThemeChanged(PrefsHelper.THEME_LIGHT)
                    }
                    ThemeChip("Темная", currentTheme == PrefsHelper.THEME_DARK) {
                        onThemeChanged(PrefsHelper.THEME_DARK)
                    }
                    ThemeChip("Системная", currentTheme == PrefsHelper.THEME_SYSTEM) {
                        onThemeChanged(PrefsHelper.THEME_SYSTEM)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Вид карт", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeChip("Сетка", viewMode == PrefsHelper.VIEW_GRID) {
                        viewMode = PrefsHelper.VIEW_GRID
                        PrefsHelper.setViewMode(context, viewMode)
                    }
                    ThemeChip("Список", viewMode == PrefsHelper.VIEW_LIST) {
                        viewMode = PrefsHelper.VIEW_LIST
                        PrefsHelper.setViewMode(context, viewMode)
                    }
                }
            }

            // Блок: Запуск
            SettingsAccordion(title = "Поведение при запуске") {
                Text("Открывать:", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeChip("Список карт", startScreen == PrefsHelper.START_LIST) {
                        startScreen = PrefsHelper.START_LIST
                        PrefsHelper.setStartScreen(context, startScreen)
                    }
                    ThemeChip("Последняя карта", startScreen == PrefsHelper.START_LAST_CARD) {
                        startScreen = PrefsHelper.START_LAST_CARD
                        PrefsHelper.setStartScreen(context, startScreen)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Если в профиле карты выбрано «Открывать при запуске», эта настройка игнорируется.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Блок: О приложении
            SettingsAccordion(title = "Приложение") {
                Text("GusCard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Ваш цифровой кошелек для дисконтных карт. Всегда под рукой, даже без интернета.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    "Версия: ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun SettingsAccordion(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "arrow")

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.rotate(rotation),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(Modifier.padding(top = 16.dp)) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        shape = RoundedCornerShape(12.dp)
    )
}