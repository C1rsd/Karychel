package com.karychel.app.database

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para manejar operaciones con mangas.
 * Proporciona una capa de abstracción sobre el DAO.
 */
class MangaRepository(context: Context) {

    private val mangaDao: MangaDao = AppDatabase.getDatabase(context).mangaDao()

    /**
     * Inserta un bloque de mangas (optimizado para bloques de 100).
     */
    suspend fun insertMangasBlock(mangas: List<MangaEntity>) {
        mangaDao.insertMangasBlock(mangas)
    }

    /**
     * Inserta un solo manga.
     */
    suspend fun insertManga(manga: MangaEntity) {
        mangaDao.insertManga(manga)
    }

    /**
     * Obtiene todos los mangas como Flow.
     */
    fun getAllMangas(): Flow<List<MangaEntity>> = mangaDao.getAllMangas()

    /**
     * Obtiene todos los mangas como lista (suspending).
     */
    suspend fun getAllMangasList(): List<MangaEntity> = mangaDao.getAllMangasList()

    /**
     * Obtiene un manga por ID.
     */
    suspend fun getMangaById(id: String): MangaEntity? = mangaDao.getMangaById(id)

    /**
     * Obtiene mangas por estado de lectura.
     */
    fun getMangasByStatus(status: String): Flow<List<MangaEntity>> = 
        mangaDao.getMangasByStatus(status)

    /**
     * Busca mangas por título.
     */
    fun searchMangasByTitle(query: String): Flow<List<MangaEntity>> = 
        mangaDao.searchMangasByTitle(query)

    /**
     * Busca mangas por autor.
     */
    fun searchMangasByAuthor(query: String): Flow<List<MangaEntity>> = 
        mangaDao.searchMangasByAuthor(query)

    /**
     * Actualiza el estado de lectura de un manga.
     */
    suspend fun updateReadingStatus(id: String, status: String) {
        mangaDao.updateReadingStatus(id, status)
    }

    /**
     * Obtiene el conteo total de mangas.
     */
    suspend fun getMangaCount(): Int = mangaDao.getMangaCount()

    /**
     * Obtiene el conteo de mangas por estado.
     */
    suspend fun getMangaCountByStatus(status: String): Int = 
        mangaDao.getMangaCountByStatus(status)

    /**
     * Elimina un manga.
     */
    suspend fun deleteManga(id: String) {
        mangaDao.deleteManga(id)
    }

    /**
     * Elimina todos los mangas.
     */
    suspend fun deleteAllMangas() {
        mangaDao.deleteAllMangas()
    }
}
