package com.maugarcia.pokeapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maugarcia.pokeapp.data.local.entities.Pokemon
import com.maugarcia.pokeapp.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonViewModel @Inject constructor(
    private val repository: PokemonRepository,
) : ViewModel() {
    private val _pokemons = MutableStateFlow<List<Pokemon>>(emptyList())
    val pokemons: StateFlow<List<Pokemon>> = _pokemons.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    // Background update state
    private val _isBackgroundUpdateEnabled = MutableLiveData(true)
    val isBackgroundUpdateEnabled: LiveData<Boolean> = _isBackgroundUpdateEnabled

    // Pagination
    private var currentOffset = 0
    private val initialPageSize = 15
    private val additionalPageSize = 10

    // Job to manage background update coroutine
    private var backgroundUpdateJob: Job? = null

    init {
        loadInitialData()
        startPokemonUpdateService()
    }

    // Initial load of 15 Pokémon
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (repository.getPokemonCount() == 0) {
                    repository.fetchAndStorePokemons(initialPageSize, 0)
                }
                _pokemons.value = repository.getPokemons(initialPageSize)
                currentOffset = initialPageSize
            } catch (e: Exception) {
                _error.value = "Error loading initial Pokémon: ${e.message}"
                Log.e("PokemonViewModel", "Error loading initial Pokémon", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Service to add more Pokémon automatically every 30 seconds
    internal fun startPokemonUpdateService() {
        // Asegúrate de no iniciar un nuevo Job si ya hay uno activo
        if (backgroundUpdateJob?.isActive == true) return

        backgroundUpdateJob = viewModelScope.launch {
            while (isActive) { // Verifica si el Job está activo
                if ( !_isLoading.value && _isBackgroundUpdateEnabled.value == true) {
                    addPokemonsInBackground()
                }
                delay(30000) // Espera 30 segundos
            }
        }
    }

    // Toggle background update
    fun toggleBackgroundUpdate() {
        _isBackgroundUpdateEnabled.value = !(_isBackgroundUpdateEnabled.value ?: true)

        // Update toast message based on new state
        val message = if (_isBackgroundUpdateEnabled.value == true)
            "Carga de Pokémon en segundo plano reanudada"
        else
            "Carga de Pokémon en segundo plano detenida"

        _toastMessage.value = message
    }

    // Add 10 Pokémon in background
    private suspend fun addPokemonsInBackground() {
        try {
            _isLoading.value = true

            // Fetch new Pokémon at the current offset
            repository.fetchAndStorePokemons(additionalPageSize, currentOffset)

            // Retrieve the new Pokémon
            val newPokemons = repository.getPokemons(additionalPageSize, currentOffset)

            // Update visible list
            val currentPokemonsList = _pokemons.value
            val updatedPokemonsList = currentPokemonsList + newPokemons

            _pokemons.value = updatedPokemonsList

            // Update offset for next batch
            currentOffset += additionalPageSize

            // Notify about added Pokémon
            _toastMessage.postValue("Se han agregado ${additionalPageSize} nuevos Pokémon, ya son ${updatedPokemonsList.size} en total")
        } catch (e: Exception) {
            _error.value = "Error adding more Pokémon: ${e.message}"
            Log.e("PokemonViewModel", "Error adding more Pokémon", e)
        } finally {
            _isLoading.value = false
        }
    }

    // Pokémon search functionality
    fun searchPokemons(query: String, searchByType: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                if (searchByType) {
                    // If searchByType is true, search by type
                    val typeResult = repository.searchPokemonByType(query)
                    _pokemons.value = typeResult
                } else {
                    // If searchByType is false, search by name
                    val nameResult = repository.searchPokemonByName(query)
                    _pokemons.value = nameResult
                }

            } catch (e: Exception) {
                _error.value = "Error searching Pokémon: ${e.message}"
                Log.e("PokemonViewModel", "Error searching Pokémon", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Method to stop Pokemon update
    fun stopPokemonUpdate() {
        backgroundUpdateJob?.cancel()
        _isBackgroundUpdateEnabled.value = false
        _toastMessage.value = "Carga de Pokémon en segundo plano detenida"
    }

    // Method to start Pokemon update
    fun startPokemonUpdate() {
        if (backgroundUpdateJob?.isActive != true) {
            startPokemonUpdateService()
            _isBackgroundUpdateEnabled.value = true
            _toastMessage.value = "Carga de Pokémon en segundo plano reanudada"
        }
    }

    // Clean up job when ViewModel is cleared
    override fun onCleared() {
        backgroundUpdateJob?.cancel()
        super.onCleared()
    }
}