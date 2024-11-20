package com.maugarcia.pokeapp.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.maugarcia.pokeapp.data.local.PokemonDao
import com.maugarcia.pokeapp.data.local.entities.Pokemon
import com.maugarcia.pokeapp.data.local.entities.PokemonDetail
import com.maugarcia.pokeapp.data.remote.PokeApiService
import com.maugarcia.pokeapp.data.remote.response.PokemonResponse
import com.maugarcia.pokeapp.data.remote.response.PokemonResult
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.*


@OptIn(ExperimentalCoroutinesApi::class)
class PokemonRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule() // Esto asegura que las pruebas se ejecuten de forma sincrónica.

    private lateinit var pokemonDao: PokemonDao
    private lateinit var pokemonRepository: PokemonRepository
    private lateinit var pokeApiService: PokeApiService
    private lateinit var gson: Gson

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined) // Para usar el Dispatcher en pruebas

        // Creamos mocks de las dependencias
        pokemonDao = mockk()
        pokeApiService = mockk() // Mock del servicio API
        gson = mockk() // Mock de Gson

        // Inicializamos el repositorio con el Dao mockeado
        pokemonRepository = PokemonRepository(pokeApiService, pokemonDao, gson)
    }

    @Test
    fun `insert and retrieve pokemon from repository`() = runTest {
        val pokemon = Pokemon(id = 1, name = "Bulbasaur", type = "grass, poison", url = "https://example.com/bulbasaur", imageUrl = "https://example.com/bulbasaur")

        val pokemonResponse = PokemonResponse(
            count = 1,
            next = null,
            previous = null,
            results = listOf(
                PokemonResult(name = "Bulbasaur", url = "https://example.com/bulbasaur")
            )
        )

        // Mockear la respuesta del servicio API
        coEvery { pokeApiService.getPokemons(limit = 10, offset = 0) } returns pokemonResponse

        // Mockear la inserción en el Dao
        coEvery { pokemonDao.insertPokemons(any()) } just Runs

        // Mockear la recuperación de Pokémon desde el Dao
        coEvery { pokemonDao.getPokemons(limit = 10, offset = 0) } returns listOf(pokemon)

        // Llamar al repositorio para insertar y obtener Pokémon
        pokemonRepository.fetchAndStorePokemons(limit = 10)

        // Verificar que la inserción se haya realizado correctamente
        coVerify { pokemonDao.insertPokemons(any()) }

        // Llamar al repositorio para obtener los Pokémon
        val pokemons = pokemonRepository.getPokemons(limit = 10, offset = 0)

        // Verificar que la lista de Pokémon obtenida sea la misma que la insertada
        assertEquals(1, pokemons.size)
        assertEquals("Bulbasaur", pokemons[0].name)
        assertEquals("grass, poison", pokemons[0].type)
    }

    @Test
    fun `get pokemon details from repository`() = runTest {
        val pokemonDetail = PokemonDetail(
            id = 1,
            name = "Bulbasaur",
            height = 7,
            weight = 69,
            types = "grass, poison",
            stats = "{}",
            abilities = "{}",
            imageUrl = "https://example.com/image.png"
        )

        // Mockeamos la recuperación de detalles desde el Dao
        coEvery { pokemonDao.getPokemonDetail(1) } returns pokemonDetail

        // Usamos el repositorio para obtener el detalle del Pokémon
        val retrievedPokemonDetail = pokemonRepository.getPokemonDetail(1)

        // Verificamos que los detalles obtenidos son los mismos que los esperados
        assertNotNull(retrievedPokemonDetail)
        assertEquals(pokemonDetail.id, retrievedPokemonDetail?.id)
        assertEquals(pokemonDetail.name, retrievedPokemonDetail?.name)
        assertEquals(pokemonDetail.height, retrievedPokemonDetail?.height)
        assertEquals(pokemonDetail.weight, retrievedPokemonDetail?.weight)
        assertEquals(pokemonDetail.types, retrievedPokemonDetail?.types)
        assertEquals(pokemonDetail.imageUrl, retrievedPokemonDetail?.imageUrl)
    }
}