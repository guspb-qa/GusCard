package com.example.guscard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    // Сортировка: сначала избранные, потом по времени открытия (сначала новые)
    @Query("SELECT * FROM cards ORDER BY isFavorite DESC, lastOpenedAt DESC")
    fun getAll(): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getById(id: Long): Card?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: Card)

    @Update
    suspend fun update(card: Card)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Снять флаг приоритета со всех карт (для чекбокса "Карта при открытии")
    @Query("UPDATE cards SET isDefaultForStart = 0")
    suspend fun clearDefaultForStart()

    // Обновить только время последнего открытия (чтобы не затирать остальные поля)
    @Query("UPDATE cards SET lastOpenedAt = :time WHERE id = :id")
    suspend fun updateLastOpened(id: Long, time: Long)
}