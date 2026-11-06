package com.example.guscard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guscard.PrefsHelper
import com.example.guscard.data.AppDatabase
import com.example.guscard.data.Card
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(
    onAddClick: () -> Unit,
    onCardClick: (Long) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.get(context).cardDao() }
    val allCards by dao.getAll().collectAsState(initial = emptyList())

    var query by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Все") }

    val viewMode = remember { PrefsHelper.getViewMode(context) }

    val cards = allCards
        .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
        .filter { selectedCategory == "Все" || it.category == selectedCategory }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (searchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Поиск") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        TextButton(onClick = { searchActive = false; query = "" }) {
                            Text("Отмена", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            } else {
                IosTopBar(
                    title = "Мои карты",
                    onSearchClick = { searchActive = true },
                    onSettingsClick = onSettingsClick
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Панель категорий
            val categories = listOf(
                "Все" to Icons.Default.Star,
                "Продукты" to Icons.Default.ShoppingCart,
                "Шопинг" to Icons.Default.ShoppingBag,
                "Дом/ремонт" to Icons.Default.Home,
                "АЗС" to Icons.Default.LocalGasStation,
                "Электроника" to Icons.Default.PhoneAndroid,
                "Другое" to Icons.Default.Category
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { (name, icon) ->
                    val isSelected = selectedCategory == name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedCategory = name }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = name,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            if (cards.isEmpty()) {
                EmptyState(Modifier.fillMaxSize(), query.isNotBlank() || selectedCategory != "Все", onAddClick)
            } else {
                if (viewMode == PrefsHelper.VIEW_GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(cards, key = { it.id }) { card ->
                            CardTile(
                                card = card,
                                onClick = { onCardClick(card.id) },
                                onFavoriteToggle = {
                                    scope.launch {
                                        dao.update(card.copy(isFavorite = !card.isFavorite))
                                    }
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(cards, key = { it.id }) { card ->
                            CardRow(
                                card = card,
                                onClick = { onCardClick(card.id) },
                                onFavoriteToggle = {
                                    scope.launch {
                                        dao.update(card.copy(isFavorite = !card.isFavorite))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IosTopBar(title: String, onSearchClick: () -> Unit, onSettingsClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 34.sp
            )
            Row {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, contentDescription = "Поиск", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Настройки", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun CardTile(
    card: Card,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onSurface
    val accentColor = Color(card.color)
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Иконка (круглая, увеличенная)
            Box(
                Modifier
                    .size(44.dp) // Увеличили
                    .clip(CircleShape) // Круглая
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val imageResId = if (card.logoName != null) {
                    context.resources.getIdentifier(card.logoName, "drawable", context.packageName)
                } else 0

                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(6.dp), // Отступ, чтобы картинка не касалась краев
                        contentScale = ContentScale.Fit // Вписываем в круг
                    )
                } else {
                    Text(
                        text = card.name.firstOrNull()?.uppercase() ?: "?",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (card.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Избранное",
                    tint = if (card.isFavorite) Color(0xFFFFCC00) else onBg.copy(alpha = 0.3f)
                )
            }
        }

        Column {
            // Скидка
            if (card.discount != null) {
                Text(
                    text = "-${card.discount}%",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(Modifier.height(4.dp))
            }

            Text(
                text = card.name,
                color = onBg,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun CardRow(
    card: Card,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val bg = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onSurface
    val accentColor = Color(card.color)
    val context = LocalContext.current

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(48.dp) // Увеличили для списка
                .clip(CircleShape) // Круглая
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            val imageResId = if (card.logoName != null) {
                context.resources.getIdentifier(card.logoName, "drawable", context.packageName)
            } else 0

            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = card.name.firstOrNull()?.uppercase() ?: "?",
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = card.name,
                    color = onBg,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                if (card.discount != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "-${card.discount}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = card.code,
                color = onBg.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
        }
        IconButton(onClick = onFavoriteToggle) {
            Icon(
                imageVector = if (card.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = "Избранное",
                tint = if (card.isFavorite) Color(0xFFFFCC00) else onBg.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier, isSearchOrFilter: Boolean, onAddClick: () -> Unit) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (isSearchOrFilter) "Ничего не найдено" else "Пока нет карт",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (!isSearchOrFilter) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Добавьте первую карту, чтобы она всегда была под рукой",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Добавить карту")
            }
        }
    }
}