package com.maugarcia.pokeapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maugarcia.pokeapp.data.local.entities.Pokemon
import com.maugarcia.pokeapp.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val count = repository.getPokemonCount()
                if (count == 0) {
                    repository.fetchAndStorePokemons(pageSize, 0)
                }
                searchPokemons()
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

                _filteredPokemons.value = when {
                    query.isNotEmpty() -> repository.searchPokemons(query)
                    type != null -> repository.getPokemonsByType(type)
                    else -> repository.getPokemons(currentOffset + pageSize)
                }
            } catch (e: Exception) {
                _error.value = "Error searching pokemons: ${e.message}"
                Log.e("PokemonViewModel", "Error searching pokemons", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMorePokemon() {
        if (_isLoading.value) return
        if (_searchQuery.value.isNotEmpty() || _selectedType.value != null) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                currentOffset += pageSize
                repository.fetchAndStorePokemons(pageSize, currentOffset)
                searchPokemons()
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
        searchPokemons()
    }

    fun setSelectedType(type: String?) {
        _selectedType.value = type
        currentOffset = 0
        searchPokemons()
    }
}