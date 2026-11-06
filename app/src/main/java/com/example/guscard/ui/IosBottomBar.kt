package com.example.guscard.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height

@Composable
fun IosBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.height(80.dp)
    ) {
        NavigationBarItem(
            selected = currentRoute == "list",
            onClick = { onNavigate("list") },
            icon = {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = "Карты",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Карты", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") },
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Настройки",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Настройки", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = Color.Transparent
            )
        )
    }
}