package com.maugarcia.pokeapp.data.repository

import com.maugarcia.pokeapp.data.local.entities.Pokemon

interface PkemonRepositoryInterface {
    suspend fun searchPokemonByName(query: String): List<Pokemon>
    suspend fun searchPokemonByType(query: String): List<Pokemon>
    // Otras funciones que tu repositorio implementa, como:
    suspend fun getPokemonCount(): Int
    suspend fun fetchAndStorePokemons(limit: Int, offset: Int)
    suspend fun getPokemons(limit: Int): List<Pokemon>
}