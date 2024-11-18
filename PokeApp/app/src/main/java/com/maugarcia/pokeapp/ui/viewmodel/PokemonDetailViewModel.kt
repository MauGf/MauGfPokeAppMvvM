package com.maugarcia.pokeapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.maugarcia.pokeapp.data.local.entities.PokemonDetail
import com.maugarcia.pokeapp.data.remote.response.PokemonStat
import com.maugarcia.pokeapp.data.remote.response.PokemonType
import com.maugarcia.pokeapp.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val repository: PokemonRepository,
    private val gson: Gson
) : ViewModel() {
    private val _pokemonDetail = MutableStateFlow<PokemonDetail?>(null)
    val pokemonDetail: StateFlow<PokemonDetail?> = _pokemonDetail.asStateFlow()

    fun loadPokemonDetail(id: Int) {
        viewModelScope.launch {
            try {
                _pokemonDetail.value = repository.getPokemonDetail(id)
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun getTypes(detail: PokemonDetail): List<String> {
        return try {
            val types = gson.fromJson<List<PokemonType>>(
                detail.types,
                object : TypeToken<List<PokemonType>>() {}.type
            )
            types.map { it.type.name }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getStats(detail: PokemonDetail): Map<String, Int> {
        return try {
            val stats = gson.fromJson<List<PokemonStat>>(
                detail.stats,
                object : TypeToken<List<PokemonStat>>() {}.type
            )
            stats.associate { it.stat.name to it.baseStat }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}