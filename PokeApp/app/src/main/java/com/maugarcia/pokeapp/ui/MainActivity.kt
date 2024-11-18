package com.maugarcia.pokeapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
        private const val PREFS_NAME = "app_preferences"
        private const val KEY_PERMISSION_REQUESTED = "permission_requested"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
        setupViewModel()
        checkAndStartPokemonService()
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
                        viewModel.loadMorePokemons()
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

    private fun checkAndStartPokemonService() {
        // Verificar si ya se ha solicitado el permiso previamente
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val permissionRequested = sharedPreferences.getBoolean(KEY_PERMISSION_REQUESTED, false)

        // Si el permiso nunca ha sido solicitado o no ha sido concedido
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionRequested) {
            // Verificar si el permiso ha sido concedido
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Si el permiso no ha sido concedido, solicitarlo
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE)
            } else {
                // Si el permiso ya está concedido, actualizar SharedPreferences
                sharedPreferences.edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply()
                startPokemonService()
            }
        } else {
            // Si el permiso ya fue concedido anteriormente o si el dispositivo está en una versión anterior
            startPokemonService()
        }
    }

    // Manejar la respuesta de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso fue concedido, actualizar SharedPreferences y empezar el servicio
                val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                sharedPreferences.edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply()
                startPokemonService()
            } else {
                // Si el permiso fue denegado, informar al usuario
                Toast.makeText(this, "El permiso de notificaciones es necesario para el servicio.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startPokemonService() {
        val serviceIntent = Intent(this, PokemonUpdateService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}