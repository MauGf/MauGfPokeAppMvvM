package com.maugarcia.pokeapp.data.repository

import android.util.Log
import com.google.gson.Gson
import com.maugarcia.pokeapp.data.local.PokemonDao
import com.maugarcia.pokeapp.data.local.entities.Pokemon
import com.maugarcia.pokeapp.data.local.entities.PokemonDetail
import com.maugarcia.pokeapp.data.remote.PokeApiService
import com.maugarcia.pokeapp.data.remote.response.PokemonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

class PokemonRepository @Inject constructor(
    private val api: PokeApiService,
    private val dao: PokemonDao,
    private val gson: Gson
) {
    suspend fun getPokemons(limit: Int = 15, offset: Int = 0): List<Pokemon> {
        Log.d("PokemonRepository", "Getting Pokemons: limit=$limit, offset=$offset")
        return dao.getPokemons(limit, offset)
    }

    suspend fun fetchAndStorePokemons(limit: Int, offset: Int = 0) {
        try {
            val response = api.getPokemons(limit, offset)
            Log.d("PokemonRepository", "API response: ${response.results}")

            val pokemons = mapApiResultsToPokemons(response.results)
            dao.insertPokemons(pokemons)
            Log.d("PokemonRepository", "Pokémon inserted successfully")
        } catch (e: Exception) {
            Log.e("PokemonRepository", "Error fetching and storing pokemons", e)
            throw Exception("Error fetching and storing pokemons: ${e.message}")
        }
    }

    suspend fun searchPokemonByName(query: String): List<Pokemon> {
        return withContext(Dispatchers.IO) {
            dao.searchByName(query)
        }
    }

    suspend fun searchPokemonByType(query: String): List<Pokemon> {
        return withContext(Dispatchers.IO) {
            dao.searchByType(query)
        }
    }

    suspend fun getPokemonCount(): Int {
        return dao.getPokemonCount()
    }

    suspend fun getPokemonDetail(id: Int): PokemonDetail {
        return dao.getPokemonDetail(id) ?: fetchAndStorePokemonDetail(id)
    }

    private suspend fun fetchAndStorePokemonDetail(id: Int): PokemonDetail {
        val response = api.getPokemonDetail(id)

        // Asegúrate de obtener la URL de la imagen desde la respuesta
        val imageUrl = response.sprites.front_default ?: ""  // Obtener la URL de la imagen

        return PokemonDetail(
            id = response.id,
            name = response.name,
            height = response.height,
            weight = response.weight,
            types = gson.toJson(response.types),
            stats = gson.toJson(response.stats),
            abilities = gson.toJson(response.abilities),
            imageUrl = imageUrl  // Pasar la URL de la imagen aquí
        ).also {
            dao.insertPokemonDetail(it)
        }
    }

    private suspend fun mapApiResultsToPokemons(results: List<PokemonResult>): List<Pokemon> {
        return results.map { result ->
            try {
                // Verificamos que la URL tenga el formato esperado
                val id = result.url.split("/").dropLast(1).lastOrNull()?.toIntOrNull()
                if (id == null) {
                    throw Exception("Invalid Pokémon URL: ${result.url}")
                }

                // Hacer la solicitud para obtener los detalles del Pokémon usando el ID
                val response = api.getPokemonDetail(id)

                // Concatenar los tipos del Pokémon
                val types = response.types?.joinToString(", ") { it.type.name } ?: "Unknown"

                // Crear el objeto Pokémon con los detalles
                Pokemon(
                    id = response.id,
                    name = result.name,
                    type = types,
                    url = result.url,
                    imageUrl = response.sprites.front_default ?: ""
                )
            } catch (e: Exception) {
                // Manejar cualquier excepción que ocurra
                Log.e("PokemonRepository", "Error fetching Pokémon detail for ${result.name}: ${e.message}")
                null  // En caso de error, devolvemos null o puedes manejarlo de otra forma
            }
        }.filterNotNull() // Filtramos los nulos si alguna petición falla
    }
}