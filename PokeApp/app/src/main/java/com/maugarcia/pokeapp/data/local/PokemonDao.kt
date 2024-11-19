package com.maugarcia.pokeapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.maugarcia.pokeapp.data.local.entities.Pokemon
import com.maugarcia.pokeapp.data.local.entities.PokemonDetail

@Dao
interface PokemonDao {
    @Query("SELECT * FROM pokemons LIMIT :limit OFFSET :offset")
    suspend fun getPokemons(limit: Int, offset: Int): List<Pokemon>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemons(pokemons: List<Pokemon>)

    @Query("SELECT COUNT(*) FROM pokemons")
    suspend fun getPokemonCount(): Int

    @Query("SELECT * FROM pokemons WHERE id = :pokemonId")
    suspend fun getPokemonById(pokemonId: Int): Pokemon?

    @Query("SELECT * FROM pokemon_details WHERE id = :pokemonId")
    suspend fun getPokemonDetail(pokemonId: Int): PokemonDetail?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonDetail(detail: PokemonDetail)

    @Query("SELECT * FROM pokemons WHERE name LIKE '%' || :query || '%'")
    suspend fun searchByName(query: String): List<Pokemon>

    @Query("SELECT * FROM pokemons WHERE type LIKE '%' || :query || '%'")
    suspend fun searchByType(query: String): List<Pokemon>

}