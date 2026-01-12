package com.karychel.app.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.karychel.app.database.MangaEntity
import com.karychel.app.database.MangaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Biblioteca.
 * Conecta la UI con la base de datos Room.
 */
class LibraryViewModel(private val repository: MangaRepository) : ViewModel() {

    private val _mangas = MutableStateFlow<List<MangaEntity>>(emptyList())
    val mangas: StateFlow<List<MangaEntity>> = _mangas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadMangas()
    }

    /**
     * Carga los mangas desde la base de datos automáticamente.
     */
    private fun loadMangas() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.getAllMangas().collect { mangaList ->
                    _mangas.value = mangaList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar mangas: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Actualiza el estado de lectura de un manga.
     */
    fun updateReadingStatus(mangaId: String, status: String) {
        viewModelScope.launch {
            try {
                repository.updateReadingStatus(mangaId, status)
            } catch (e: Exception) {
                _error.value = "Error al actualizar estado: ${e.message}"
            }
        }
    }

    /**
     * Limpia el error.
     */
    fun clearError() {
        _error.value = null
    }
}

/**
 * Factory para crear LibraryViewModel con dependencias.
 */
class LibraryViewModelFactory(private val repository: MangaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
