package com.maugarcia.pokeapp.data.remote

import com.maugarcia.pokeapp.data.remote.response.PokemonDetailResponse
import com.maugarcia.pokeapp.data.remote.response.PokemonResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemons(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): PokemonResponse
    // Agregar nuevo endpoint
    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") id: Int): PokemonDetailResponse
}