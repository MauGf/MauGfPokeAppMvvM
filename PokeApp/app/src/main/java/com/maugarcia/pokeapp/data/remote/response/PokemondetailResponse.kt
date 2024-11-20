package com.maugarcia.pokeapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<PokemonType>,
    val stats: List<PokemonStat>,
    val abilities: List<PokemonAbility>,
    val sprites: Sprites  // para la URL de la imagen
)

data class Sprites(
    val front_default: String?, // URL de la imagen por defecto del Pokémon val
    val  other: OtherSprites?
)
data class OtherSprites(
    val home: HomeSprites?  // La propiedad 'home' dentro de 'other'
)
data class HomeSprites(
    val front_default: String?  // URL de la imagen en 'home'
)

data class PokemonType(
    val slot: Int,
    val type: Type
)

data class Type(
    val name: String,
    val url: String
)

data class PokemonStat(
    @SerializedName("base_stat")
    val baseStat: Int,
    val stat: Stat
)

data class Stat(
    val name: String
)

data class PokemonAbility(
    val ability: Ability,
    @SerializedName("is_hidden")
    val isHidden: Boolean
)

data class Ability(
    val name: String
)
