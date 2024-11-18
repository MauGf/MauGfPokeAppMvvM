package com.maugarcia.pokeapp.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pokemons",
    indices = [Index(value = ["name"], unique = false)]
)
data class Pokemon(
    @PrimaryKey val id: Int,
    val name: String,
    val url: String,
    val imageUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)