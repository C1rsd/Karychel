package com.karychel.app.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un manga en la base de datos.
 * Optimizada para manejar más de 5,000 registros sin lag.
 */
@Entity(
    tableName = "mangas",
    indices = [
        Index(value = ["title"], name = "idx_manga_title"),
        Index(value = ["author"], name = "idx_manga_author"),
        Index(value = ["readingStatus"], name = "idx_manga_status")
    ]
)
data class MangaEntity(
    @PrimaryKey
    val id: String,
    
    val title: String,
    
    val author: String?,
    
    val description: String?,
    
    val coverUrl: String?,
    
    /**
     * Estado de lectura del manga.
     * Valores posibles: "reading", "completed", "on_hold", "dropped", "plan_to_read", "not_started"
     */
    val readingStatus: String = "not_started",
    
    /**
     * Timestamp de creación del registro
     */
    val createdAt: Long = System.currentTimeMillis(),
    
    /**
     * Timestamp de última actualización
     */
    val updatedAt: Long = System.currentTimeMillis()
)
