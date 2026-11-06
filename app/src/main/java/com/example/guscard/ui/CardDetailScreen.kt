package com.example.guscard.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guscard.data.AppDatabase
import com.example.guscard.data.Card
import com.example.guscard.util.BarcodeGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(cardId: Long, onBack: () -> Unit, onEditClick: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).cardDao() }
    var card by remember { mutableStateOf<Card?>(null) }
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val params = window?.attributes
        val old = params?.screenBrightness
        params?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        window?.attributes = params
        onDispose {
            params?.screenBrightness = old ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window?.attributes = params
        }
    }

    LaunchedEffect(cardId) {
        val loaded = dao.getById(cardId)
        card = loaded
        if (loaded != null) {
            dao.updateLastOpened(loaded.id, System.currentTimeMillis())
            bitmap = BarcodeGenerator.generate(loaded.code, loaded.format)
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card?.name ?: "Загрузка...") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } },
                actions = {
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Card Number", card?.code ?: "")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Номер карты скопирован", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Копировать")
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (card == null) {
                CircularProgressIndicator()
            } else {
                Spacer(Modifier.height(16.dp))

                // Штрих-код
                val isQr = card!!.format == "QR_CODE"
                Box(
                    Modifier
                        .then(if (isQr) Modifier.size(280.dp) else Modifier.fillMaxWidth())
                        .background(Color.White, shape = MaterialTheme.shapes.medium)
                        .padding(if (isQr) 16.dp else 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Штрих-код",
                            modifier = if (isQr) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
                            contentScale = if (isQr) ContentScale.Fit else ContentScale.FillWidth
                        )
                    } else {
                        Text("Не удалось сгенерировать штрих-код", color = Color.Red)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Логотип (как было, просто картинка)
                val fullLogoResId = if (card!!.fullLogoName != null) {
                    context.resources.getIdentifier(card!!.fullLogoName, "drawable", context.packageName)
                } else 0

                if (fullLogoResId != 0) {
                    Image(
                        painter = painterResource(id = fullLogoResId),
                        contentDescription = null,
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth(0.7f),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = card!!.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Скидка
                if (card!!.discount != null) {
                    Text(
                        text = "Скидка: ${card!!.discount}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Срок действия
                if (card!!.expiryDate != null) {
                    val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(card!!.expiryDate!!))
                    val isExpired = card!!.expiryDate!! < System.currentTimeMillis()
                    Text(
                        text = if (isExpired) "Срок действия истек ($dateStr)" else "Действительна до: $dateStr",
                        fontSize = 16.sp,
                        color = if (isExpired) Color(0xFFFF9800) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                // Комментарий
                if (!card!!.comment.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = card!!.comment!!,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}