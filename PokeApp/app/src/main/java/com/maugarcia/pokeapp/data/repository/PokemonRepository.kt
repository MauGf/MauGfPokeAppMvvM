package com.maugarcia.pokeapp.data.repository

import com.google.gson.Gson
import com.maugarcia.pokeapp.data.local.PokemonDao
import com.maugarcia.pokeapp.data.local.entities.Pokemon
import com.maugarcia.pokeapp.data.local.entities.PokemonDetail
import com.maugarcia.pokeapp.data.remote.PokeApiService
import com.maugarcia.pokeapp.data.remote.response.PokemonResult
import javax.inject.Inject
import javax.inject.Singleton

class PokemonRepository @Inject constructor(
    private val api: PokeApiService,
    private val dao: PokemonDao,
    private val gson: Gson
) {
    suspend fun getPokemons(limit: Int = 15): List<Pokemon> {
        return dao.getPokemons(limit)
    }

    suspend fun fetchAndStorePokemons(limit: Int, offset: Int = 0) {
        try {
            val response = api.getPokemons(limit, offset)
            val pokemons = mapApiResultsToPokemons(response.results)
            dao.insertPokemons(pokemons)
        } catch (e: Exception) {
            throw Exception("Error fetching and storing pokemons: ${e.message}")
        }
    }

    suspend fun getPokemonCount(): Int = dao.getPokemonCount()

    suspend fun searchPokemons(query: String): List<Pokemon> {
        return dao.searchPokemons(query)
    }

    suspend fun getPokemonsByType(type: String): List<Pokemon> {
        return dao.getPokemonsByType(type).map { detail ->
            Pokemon(
                id = detail.id,
                name = detail.name,
                url = "https://pokeapi.co/api/v2/pokemon/${detail.id}",
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${detail.id}.png"
            )
        }
    }

    suspend fun getPokemonDetail(id: Int): PokemonDetail {
        return dao.getPokemonDetail(id) ?: fetchAndStorePokemonDetail(id)
    }

    private suspend fun fetchAndStorePokemonDetail(id: Int): PokemonDetail {
        val response = api.getPokemonDetail(id)
        return PokemonDetail(
            id = response.id,
            name = response.name,
            height = response.height,
            weight = response.weight,
            types = gson.toJson(response.types),
            stats = gson.toJson(response.stats),
            abilities = gson.toJson(response.abilities)
        ).also {
            dao.insertPokemonDetail(it)
        }
    }

    private fun mapApiResultsToPokemons(results: List<PokemonResult>): List<Pokemon> {
        return results.map { result ->
            val id = result.url.split("/").dropLast(1).last().toInt()
            Pokemon(
                id = id,
                name = result.name,
                url = result.url,
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
            )
        }
    }
}