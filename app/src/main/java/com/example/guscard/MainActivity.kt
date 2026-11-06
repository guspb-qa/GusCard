package com.example.guscard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.guscard.data.AppDatabase
import com.example.guscard.ui.AddCardScreen
import com.example.guscard.ui.CardDetailScreen
import com.example.guscard.ui.CardListScreen
import com.example.guscard.ui.SettingsScreen
import com.example.guscard.ui.theme.GusCardTheme
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.LaunchedEffect


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var themeMode by remember { mutableIntStateOf(PrefsHelper.getTheme(context)) }

            val isDark = when (themeMode) {
                PrefsHelper.THEME_LIGHT -> false
                PrefsHelper.THEME_DARK -> true
                else -> isSystemInDarkTheme()
            }

            GusCardTheme(darkTheme = isDark) {
                MainApp(
                    themeMode = themeMode,
                    onThemeChanged = { newTheme ->
                        themeMode = newTheme
                        PrefsHelper.setTheme(context, newTheme)
                    }
                )
            }
        }
    }
}

@Composable
fun MainApp(themeMode: Int, onThemeChanged: (Int) -> Unit) {
    val context = LocalContext.current
    val nav = rememberNavController()

    var startDest by remember { mutableStateOf("list") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val dao = AppDatabase.get(context).cardDao()
            val cards = dao.getAll().first()

            val priorityCard = cards.find { it.isDefaultForStart }
            if (priorityCard != null) {
                startDest = "detail/${priorityCard.id}"
            } else {
                val startScreen = PrefsHelper.getStartScreen(context)
                val lastCardId = PrefsHelper.getLastCardId(context)

                if (startScreen == PrefsHelper.START_LAST_CARD) {
                    if (lastCardId != -1L && cards.any { it.id == lastCardId }) {
                        startDest = "detail/$lastCardId"
                    } else if (cards.isNotEmpty()) {
                        // Если карты есть, но ни одна не открывалась — берем самую свежую (первую в списке)
                        startDest = "detail/${cards.first().id}"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(navController = nav, startDestination = startDest) {
            composable("list") {
                CardListScreen(
                    onAddClick = { nav.navigate("add") },
                    onCardClick = { id ->
                        PrefsHelper.setLastCardId(context, id)
                        nav.navigate("detail/$id")
                    },
                    onSettingsClick = { nav.navigate("settings") }
                )
            }
            composable("add") {
                AddCardScreen(
                    navController = nav,
                    cardId = null,
                    onSaved = { nav.popBackStack() },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("edit/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull()
                AddCardScreen(
                    navController = nav,
                    cardId = id,
                    onSaved = { nav.popBackStack() },
                    onBack = { nav.popBackStack() }
                )
            }
            composable("detail/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                CardDetailScreen(
                    cardId = id,
                    onBack = {
                        if (!nav.popBackStack()) {
                            nav.navigate("list") { popUpTo(0) }
                        }
                    },
                    onEditClick = { nav.navigate("edit/$id") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onBack = { nav.popBackStack() },
                    currentTheme = themeMode,
                    onThemeChanged = onThemeChanged
                )
            }
        }
    }
}