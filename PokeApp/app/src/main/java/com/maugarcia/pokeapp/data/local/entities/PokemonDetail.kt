package com.maugarcia.pokeapp.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon_details")
data class PokemonDetail(
    @PrimaryKey val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: String,  // JSON string
    val stats: String,  // JSON string
    val abilities: String,  // JSON string
    val imageUrl: String, // Aquí agregamos la URL de la imagen
)