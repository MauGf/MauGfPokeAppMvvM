package com.maugarcia.pokeapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maugarcia.pokeapp.data.local.entities.Pokemon
import com.maugarcia.pokeapp.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    private val _filteredPokemons = MutableStateFlow<List<Pokemon>>(emptyList())
    val filteredPokemons: StateFlow<List<Pokemon>> = _filteredPokemons.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Variables para paginación
    private var currentOffset = 0
    private val pageSize = 15

    init {
        loadInitialData()
        startPokemonUpdateService()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val count = repository.getPokemonCount()
                if (count == 0) {
                    // Si no hay pokemones en la base de datos, obtener los primeros
                    repository.fetchAndStorePokemons(pageSize, 0)
                }
                searchPokemons() // Ejecutar búsqueda inicial
            } catch (e: Exception) {
                _error.value = "Error loading initial data: ${e.message}"
                Log.e("PokemonViewModel", "Error loading initial data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun searchPokemons() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val query = _searchQuery.value
                val type = _selectedType.value

                // Realizar búsqueda o cargar más pokemones según el filtro
                _filteredPokemons.value = when {
                    query.isNotEmpty() -> repository.searchPokemons(query)
                    type != null -> repository.getPokemonsByType(type)
                    else -> repository.getPokemons(currentOffset, pageSize)
                }
            } catch (e: Exception) {
                _error.value = "Error searching pokemons: ${e.message}"
                Log.e("PokemonViewModel", "Error searching pokemons", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMorePokemons() {
        if (_isLoading.value) return
        if (_searchQuery.value.isNotEmpty() || _selectedType.value != null) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                currentOffset += pageSize
                repository.fetchAndStorePokemons(pageSize, currentOffset)
                searchPokemons() // Vuelve a realizar la búsqueda después de cargar más pokemones
            } catch (e: Exception) {
                _error.value = "Error loading more pokemon: ${e.message}"
                Log.e("PokemonViewModel", "Error loading more pokemon", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        currentOffset = 0
        searchPokemons() // Realizar búsqueda cada vez que cambie la búsqueda
    }

    fun setSelectedType(type: String?) {
        _selectedType.value = type
        currentOffset = 0
        searchPokemons() // Realizar búsqueda cada vez que cambie el tipo
    }

    // Iniciar el servicio que actualizará los pokemones automáticamente cada 30 segundos
    private fun startPokemonUpdateService() {
        // Esto puede ser hecho con un Worker o un JobScheduler para ejecutar el servicio en segundo plano cada 30 segundos
        viewModelScope.launch {
            while (true) {
                delay(30000) // Esperar 30 segundos
                addNewPokemons()
            }
        }
    }

    // Función para obtener y agregar nuevos pokemones periódicamente
    private suspend fun addNewPokemons() {
        try {
            // Incrementa el offset para obtener pokemones más allá de los ya cargados
            currentOffset += 10
            repository.fetchAndStorePokemons(10, currentOffset)
            searchPokemons() // Actualiza los pokemones filtrados
        } catch (e: Exception) {
            _error.value = "Error adding new pokemons: ${e.message}"
            Log.e("PokemonViewModel", "Error adding new pokemons", e)
        }
    }
}