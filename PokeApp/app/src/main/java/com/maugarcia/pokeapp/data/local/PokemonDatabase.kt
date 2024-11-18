package com.maugarcia.pokeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.maugarcia.pokeapp.data.local.entities.Pokemon
import com.maugarcia.pokeapp.data.local.entities.PokemonDetail

@Database(
    entities = [Pokemon::class, PokemonDetail::class], //  entidades
    version = 1,
    exportSchema = false
)
abstract class PokemonDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
}