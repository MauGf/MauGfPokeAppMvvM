package com.maugarcia.pokeapp.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.maugarcia.pokeapp.data.local.PokemonDao
import com.maugarcia.pokeapp.data.local.PokemonDatabase
import com.maugarcia.pokeapp.data.remote.PokeApiService
import com.maugarcia.pokeapp.data.remote.RetrofitClient
import com.maugarcia.pokeapp.data.repository.PokemonRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun providePokeApiService(): PokeApiService {
        return RetrofitClient.create()
    }

    @Provides
    @Singleton
    fun providePokemonDatabase(@ApplicationContext context: Context): PokemonDatabase {
        return Room.databaseBuilder(
            context,
            PokemonDatabase::class.java,
            "pokemon_database"
        ).build()
    }

    @Provides
    @Singleton
    fun providePokemonDao(database: PokemonDatabase): PokemonDao {
        return database.pokemonDao()
    }

    @Provides
    @Singleton
    fun providePokemonRepository(
        api: PokeApiService,
        dao: PokemonDao,
        gson: Gson
    ): PokemonRepository {
        return PokemonRepository(api, dao, gson)
    }
}