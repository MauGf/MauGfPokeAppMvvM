package com.maugarcia.pokeapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.maugarcia.pokeapp.data.local.entities.Pokemon
import com.maugarcia.pokeapp.data.local.entities.PokemonDetail

@Dao
interface PokemonDao {
    @Query("SELECT * FROM pokemons LIMIT :limit")
    suspend fun getPokemons(limit: Int): List<Pokemon>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemons(pokemons: List<Pokemon>)

    @Query("SELECT COUNT(*) FROM pokemons")
    suspend fun getPokemonCount(): Int

    @Query("SELECT * FROM pokemons")
    suspend fun getAllPokemons(): List<Pokemon>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pokemons: List<Pokemon>)

    @Query("SELECT * FROM pokemons WHERE id = :pokemonId")
    suspend fun getPokemonById(pokemonId: Int): Pokemon?

    @Query("SELECT * FROM pokemons WHERE name LIKE :query")
    suspend fun searchPokemons(query: String): List<Pokemon>

    @Query("SELECT * FROM pokemon_details WHERE id = :pokemonId")
    suspend fun getPokemonDetail(pokemonId: Int): PokemonDetail?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonDetail(detail: PokemonDetail)

    @Query("""
        SELECT pd.* FROM pokemon_details pd
        WHERE pd.types LIKE '%' || :type || '%'
    """)
    suspend fun getPokemonsByType(type: String): List<PokemonDetail>
}