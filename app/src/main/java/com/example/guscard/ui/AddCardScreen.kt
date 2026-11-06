package com.example.guscard.ui

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.guscard.PopularStores
import com.example.guscard.PrefsHelper
import com.example.guscard.StoreInfo
import com.example.guscard.data.AppDatabase
import com.example.guscard.data.Card
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.util.Calendar

private val CATEGORIES = listOf(
    "Продукты", "Шопинг", "Дом/ремонт", "АЗС", "Электроника", "Другое"
)

private val FORMATS = listOf(
    "CODE_128" to "Универсальный (буквы и цифры)",
    "EAN_13" to "Стандартный товарный (13 цифр)",
    "QR_CODE" to "Квадратный QR-код",
    "EAN_8" to "Короткий товарный (8 цифр)",
    "UPC_A" to "Американский (12 цифр)",
    "CODE_39" to "Старый промышленный (буквы и цифры)"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddCardScreen(
    navController: NavController,
    cardId: Long? = null,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.get(context).cardDao() }
    val isEditing = cardId != null

    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var barcodeFormat by remember { mutableStateOf("CODE_128") }
    var color by remember { mutableStateOf(0xFF3F51B5.toInt()) }
    var discountStr by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var expiryDateValue by remember { mutableStateOf(TextFieldValue("")) }
    var isDefaultForStart by remember { mutableStateOf(false) }
    var isDateError by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Другое") }
    var logoName by remember { mutableStateOf<String?>(null) }
    var fullLogoName by remember { mutableStateOf<String?>(null) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var formatExpanded by remember { mutableStateOf(false) }

    // Состояния для автокомплита
    var showSuggestions by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<StoreInfo>>(emptyList()) }

    LaunchedEffect(cardId) {
        if (cardId != null) {
            val card = dao.getById(cardId)
            if (card != null) {
                name = card.name
                code = card.code
                barcodeFormat = card.format
                color = card.color
                discountStr = card.discount?.toString() ?: ""
                comment = card.comment ?: ""
                expiryDateValue = TextFieldValue(
                    text = card.expiryDate?.let {
                        java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(java.util.Date(it))
                    } ?: "",
                    selection = TextRange(0)
                )
                isDefaultForStart = card.isDefaultForStart
                selectedCategory = card.category
                logoName = card.logoName
                fullLogoName = card.fullLogoName
            }
        }
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            code = result.contents
            val content = result.contents
            barcodeFormat = when {
                content.startsWith("http") -> "QR_CODE"
                content.length == 13 && content.all { it.isDigit() } -> "EAN_13"
                else -> "CODE_128"
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val scannedCode = com.example.guscard.util.BarcodeGenerator.decodeFromBitmap(bitmap)
                    if (scannedCode != null) {
                        code = scannedCode
                        if (name.isBlank()) name = "Магазин"
                        Toast.makeText(context, "Штрих-код распознан!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Штрих-код не найден", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка чтения", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val discountError = discountStr.isNotEmpty() && (discountStr.toIntOrNull() == null || discountStr.toInt() > 100)

    fun formatDate(input: TextFieldValue): TextFieldValue {
        val text = input.text
        val selection = input.selection
        val digits = text.filter { it.isDigit() }

        if (digits.length > 8) return input.copy(text = text.substring(0, text.length - 1), selection = TextRange(text.length - 1))

        val formatted = buildString {
            for (i in digits.indices) {
                append(digits[i])
                if ((i == 1 || i == 3) && i < digits.length - 1) {
                    append('.')
                }
            }
        }

        if (formatted.length == 10) {
            try {
                val parts = formatted.split(".")
                val day = parts[0].toInt()
                val month = parts[1].toInt()
                if (day !in 1..31 || month !in 1..12) {
                    isDateError = true
                } else {
                    isDateError = false
                }
            } catch (e: Exception) { isDateError = true }
        } else {
            isDateError = false
        }

        val newCursorPosition = if (selection.start <= formatted.length) {
            if (formatted.length > text.length && formatted.length - text.length == 1) selection.start + 1 else selection.start
        } else formatted.length

        return input.copy(
            text = formatted,
            selection = TextRange(newCursorPosition.coerceIn(0, formatted.length))
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Редактировать" else "Добавить карту") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Отмена") } },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            scope.launch {
                                dao.deleteById(cardId!!)
                                if (PrefsHelper.getLastCardId(context) == cardId) {
                                    PrefsHelper.setLastCardId(context, -1L)
                                }
                                navController.navigate("list") { popUpTo(0) }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Red)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scanLauncher.launch(
                            ScanOptions().apply {
                                setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                                setPrompt("Наведите камеру")
                                setBeepEnabled(false)
                                setOrientationLocked(true)
                                setCaptureActivity(CustomScannerActivity::class.java)
                            }
                        )
                    },
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Сканер", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Из фото", fontSize = 13.sp)
                }
            }

            // Поле "Название" с автокомплитом
            Box {
                OutlinedTextField(
                    value = name,
                    onValueChange = { input ->
                        name = input.replaceFirstChar { it.uppercase() }
                        // Используем новую умную функцию поиска
                        if (input.length >= 2) {
                            suggestions = PopularStores.searchStores(input)
                            showSuggestions = suggestions.isNotEmpty()
                        } else {
                            showSuggestions = false
                        }
                    },
                    label = { Text("Название магазина") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Выпадающий список подсказок
                DropdownMenu(
                    expanded = showSuggestions,
                    onDismissRequest = { showSuggestions = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    suggestions.forEach { store ->
                        DropdownMenuItem(
                            text = { Text(store.name) },
                            onClick = {
                                name = store.name
                                selectedCategory = store.category
                                logoName = store.logoName
                                fullLogoName = store.fullLogoName
                                showSuggestions = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Номер карты / Штрих-код") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = discountStr,
                    onValueChange = { if (it.length <= 3) discountStr = it },
                    label = { Text("Скидка (%)") },
                    modifier = Modifier.weight(0.7f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = discountError,
                    singleLine = true
                )
                OutlinedTextField(
                    value = expiryDateValue,
                    onValueChange = { input ->
                        if (input.text.length <= 10) {
                            expiryDateValue = formatDate(input)
                        }
                    },
                    label = { Text("Срок действия") },
                    placeholder = { Text("дд.мм.гггг") },
                    modifier = Modifier.weight(1.3f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isDateError,
                    trailingIcon = {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            try {
                                val parts = expiryDateValue.text.split(".")
                                if (parts.size == 3 && expiryDateValue.text.length == 10) {
                                    cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                                }
                            } catch (_: Exception) {}

                            DatePickerDialog(context, { _, y, m, d ->
                                val newCal = Calendar.getInstance()
                                newCal.set(y, m, d)
                                val formatted = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(newCal.time)
                                expiryDateValue = TextFieldValue(formatted, TextRange(formatted.length))
                                isDateError = false
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                        }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Выбрать дату")
                        }
                    }
                )
            }

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it.replaceFirstChar { char -> char.uppercase() } },
                label = { Text("Комментарий") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Категория") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    CATEGORIES.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = formatExpanded,
                onExpandedChange = { formatExpanded = !formatExpanded }
            ) {
                OutlinedTextField(
                    value = barcodeFormat,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Формат штрих-кода") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = formatExpanded,
                    onDismissRequest = { formatExpanded = false }
                ) {
                    FORMATS.forEach { (formatName, description) ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(formatName, fontWeight = FontWeight.Medium)
                                    Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            },
                            onClick = {
                                barcodeFormat = formatName
                                formatExpanded = false
                            }
                        )
                    }
                }
            }

            Text("Цвет карточки", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(0xFF3F51B5, 0xFFE91E63, 0xFF009688, 0xFFFF9800, 0xFF795548, 0xFF607D8B).map { it.toInt() }.forEach { c ->
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .border(
                                width = if (color == c) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                            .clickable { color = c }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isDefaultForStart,
                    onCheckedChange = { isDefaultForStart = it }
                )
                Text("Открывать при запуске")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && code.isNotBlank() && !isDateError) {
                        scope.launch {
                            try {
                                val discount = if (discountStr.isBlank()) null else discountStr.toIntOrNull()
                                val expiryTimestamp = try {
                                    val parts = expiryDateValue.text.split(".")
                                    if (parts.size == 3 && expiryDateValue.text.length == 10) {
                                        val cal = Calendar.getInstance()
                                        cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                                        cal.timeInMillis
                                    } else null
                                } catch (_: Exception) { null }

                                if (isDefaultForStart) {
                                    dao.clearDefaultForStart()
                                }

                                val newCard = Card(
                                    id = cardId ?: 0,
                                    name = name,
                                    code = code,
                                    format = barcodeFormat,
                                    color = color,
                                    isFavorite = false,
                                    lastOpenedAt = System.currentTimeMillis(),
                                    discount = discount,
                                    expiryDate = expiryTimestamp,
                                    comment = if (comment.isBlank()) null else comment,
                                    customImagePath = null,
                                    isDefaultForStart = isDefaultForStart,
                                    category = selectedCategory,
                                    logoName = logoName,
                                    fullLogoName = fullLogoName
                                )

                                if (isEditing) {
                                    dao.update(newCard)
                                } else {
                                    dao.insert(newCard)
                                }
                                onSaved()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = name.isNotBlank() && code.isNotBlank() && !discountError && !isDateError
            ) { Text("Сохранить") }
        }
    }
}