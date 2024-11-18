package com.maugarcia.pokeapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.manager.Lifecycle
import com.maugarcia.pokeapp.R
import com.maugarcia.pokeapp.data.local.entities.Pokemon
import com.maugarcia.pokeapp.service.PokemonUpdateService
import com.maugarcia.pokeapp.ui.adapter.PokemonAdapter
import com.maugarcia.pokeapp.ui.viewmodel.PokemonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: PokemonViewModel
    private lateinit var adapter: PokemonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
        setupViewModel()
        startPokemonService()
    }

    private fun setupRecyclerView() {
        // Función para manejar el click del Pokémon
        val onPokemonClick: (Pokemon) -> Unit = { pokemon ->
            startActivity(PokemonDetailActivity.createIntent(this, pokemon.id))
        }


        // Inicializar el adaptador con la función de click
        adapter = PokemonAdapter(onPokemonClick)

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            // Asignar el adaptador
            adapter = this@MainActivity.adapter

            // Configurar el LayoutManager si aún no lo has hecho
            layoutManager = LinearLayoutManager(this@MainActivity)

            // Listener para la paginación
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                        viewModel.loadMorePokemon()
                    }
                }
            })
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(PokemonViewModel::class.java)

        // Usar launchWhenStarted para observar StateFlow
        lifecycleScope.launchWhenStarted {
            viewModel.filteredPokemons.collect { pokemons ->
                adapter.submitList(pokemons)
            }
        }

        // Si necesitas mostrar un loading:
        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collect { isLoading ->
                findViewById<ProgressBar>(R.id.progressBar).visibility =
                    if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Si necesitas manejar errores:
        lifecycleScope.launchWhenStarted {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun startPokemonService() {
        val serviceIntent = Intent(this, PokemonUpdateService::class.java)
        startService(serviceIntent)
    }
}