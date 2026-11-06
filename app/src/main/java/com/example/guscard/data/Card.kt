package com.example.guscard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val format: String = "CODE_128",
    val color: Int = 0xFF3F51B5.toInt(),

    val isFavorite: Boolean = false,
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val discount: Int? = null,
    val expiryDate: Long? = null,
    val comment: String? = null,
    val logoName: String? = null,       // Имя встроенной картинки (например, "pyaterochka")
    val customImagePath: String? = null, // Путь к кастомной картинке
    val isDefaultForStart: Boolean = false,
    val fullLogoName: String? = null, // ГОРИЗОНТАЛЬНЫЙ ЛОГОТИП
    val category: String = "Другое" // Категория магазина
)