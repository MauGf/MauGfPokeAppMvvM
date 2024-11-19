package com.maugarcia.pokeapp.ui.viewmodel

import android.app.Notification
import android.app.NotificationManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maugarcia.pokeapp.R
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

    // Paginación
    private var currentOffset = 0
    private val initialPageSize = 15
    private val additionalPageSize = 10

    init {
        loadInitialData()
        startPokemonUpdateService()
    }

    // Carga inicial de 15 Pokémon
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (repository.getPokemonCount() == 0) {
                    repository.fetchAndStorePokemons(initialPageSize, currentOffset)
                }
                _pokemons.value = repository.getPokemons(initialPageSize)
            } catch (e: Exception) {
                _error.value = "Error loading initial Pokémon: ${e.message}"
                Log.e("PokemonViewModel", "Error loading initial Pokémon", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Servicio para agregar más Pokémon automáticamente cada 30 segundos
    internal fun startPokemonUpdateService() {
        viewModelScope.launch {
            while (true) {
                delay(30000) // Esperar 30 segundos
                addPokemonsInBackground()
            }
        }
    }

    // Agregar 10 Pokémon en segundo plano
    private suspend fun addPokemonsInBackground() {
        try {
            _isLoading.value = true
            currentOffset += additionalPageSize
            repository.fetchAndStorePokemons(additionalPageSize, currentOffset)
            val newPokemons = repository.getPokemons(currentOffset + additionalPageSize)

            // Actualizar lista visible
            _pokemons.value = _pokemons.value + newPokemons

            // Mostrar notificación al usuario
            // Notificar que se han agregado Pokémon nuevos
            // Notificar que se han agregado Pokémon nuevos
            _toastMessage.postValue("Se han agregado ${additionalPageSize} nuevos Pokémon, ya son ${newPokemons.size} en total ")
        } catch (e: Exception) {
            _error.value = "Error adding more Pokémon: ${e.message}"
            Log.e("PokemonViewModel", "Error adding more Pokémon", e)
        } finally {
            _isLoading.value = false
        }
    }
}
