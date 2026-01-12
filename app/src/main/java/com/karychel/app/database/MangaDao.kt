package com.karychel.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para operaciones con la tabla de mangas.
 * Optimizado para inserción en bloque de 100 elementos.
 */
@Dao
interface MangaDao {

    /**
     * Inserta un bloque de mangas de forma rápida.
     * Usa REPLACE para evitar duplicados y actualizar registros existentes.
     * Optimizado para bloques de 100 elementos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMangas(mangas: List<MangaEntity>)

    /**
     * Inserta un solo manga.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(manga: MangaEntity)

    /**
     * Obtiene todos los mangas ordenados por título.
     * Usa Flow para observación reactiva.
     */
    @Query("SELECT * FROM mangas ORDER BY title ASC")
    fun getAllMangas(): Flow<List<MangaEntity>>

    /**
     * Obtiene todos los mangas ordenados por título (sin Flow, para uso síncrono).
     */
    @Query("SELECT * FROM mangas ORDER BY title ASC")
    suspend fun getAllMangasList(): List<MangaEntity>

    /**
     * Obtiene un manga por su ID.
     */
    @Query("SELECT * FROM mangas WHERE id = :id")
    suspend fun getMangaById(id: String): MangaEntity?

    /**
     * Obtiene mangas por estado de lectura.
     */
    @Query("SELECT * FROM mangas WHERE readingStatus = :status ORDER BY title ASC")
    fun getMangasByStatus(status: String): Flow<List<MangaEntity>>

    /**
     * Busca mangas por título (búsqueda parcial, case-insensitive).
     */
    @Query("SELECT * FROM mangas WHERE title LIKE '%' || :query || '%' COLLATE NOCASE ORDER BY title ASC")
    fun searchMangasByTitle(query: String): Flow<List<MangaEntity>>

    /**
     * Busca mangas por autor (búsqueda parcial, case-insensitive).
     */
    @Query("SELECT * FROM mangas WHERE author LIKE '%' || :query || '%' COLLATE NOCASE ORDER BY title ASC")
    fun searchMangasByAuthor(query: String): Flow<List<MangaEntity>>

    /**
     * Actualiza el estado de lectura de un manga.
     */
    @Query("UPDATE mangas SET readingStatus = :status, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateReadingStatus(id: String, status: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Obtiene el conteo total de mangas.
     */
    @Query("SELECT COUNT(*) FROM mangas")
    suspend fun getMangaCount(): Int

    /**
     * Obtiene el conteo de mangas por estado.
     */
    @Query("SELECT COUNT(*) FROM mangas WHERE readingStatus = :status")
    suspend fun getMangaCountByStatus(status: String): Int

    /**
     * Elimina un manga por ID.
     */
    @Query("DELETE FROM mangas WHERE id = :id")
    suspend fun deleteManga(id: String)

    /**
     * Elimina todos los mangas.
     */
    @Query("DELETE FROM mangas")
    suspend fun deleteAllMangas()

    /**
     * Inserta un bloque de mangas usando transacción para mejor rendimiento.
     * Esta función es más eficiente para bloques grandes.
     */
    @Transaction
    suspend fun insertMangasBlock(mangas: List<MangaEntity>) {
        insertMangas(mangas)
    }
}
