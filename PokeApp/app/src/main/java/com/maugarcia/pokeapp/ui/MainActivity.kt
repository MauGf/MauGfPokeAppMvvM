package com.maugarcia.pokeapp.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.manager.Lifecycle
import com.google.android.material.switchmaterial.SwitchMaterial
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
    private lateinit var pokemonViewModel: PokemonViewModel

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
        private const val PREFS_NAME = "app_preferences"
        private const val KEY_PERMISSION_REQUESTED = "permission_requested"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa el ViewModel
        pokemonViewModel = ViewModelProvider(this).get(PokemonViewModel::class.java)

        // Iniciar el servicio de actualización de Pokémon
        pokemonViewModel.startPokemonUpdateService()

        // Observar el LiveData para mostrar el Toast cuando se agreguen Pokémon nuevos
        pokemonViewModel.toastMessage.observe(this, Observer { message ->
            message?.let {
                showToast(it)
            }
        })

        lifecycleScope.launchWhenStarted {
            pokemonViewModel.pokemons.collect { pokemons ->
                // Actualiza la lista del adaptador
                adapter.submitList(pokemons)
            }
        }

        setupRecyclerView()
        setupViewModel()
        checkAndStartPokemonService()
        setupSearchBar()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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

            // Configurar el LayoutManager
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(PokemonViewModel::class.java)

        // Observar cambios en la lista de Pokémon
        lifecycleScope.launchWhenStarted {
            viewModel.pokemons.collect { pokemons ->
                adapter.submitList(pokemons)
            }
        }

        // Mostrar u ocultar un indicador de carga
        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collect { isLoading ->
                findViewById<ProgressBar>(R.id.progressBar).visibility =
                    if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Manejar errores
        lifecycleScope.launchWhenStarted {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkAndStartPokemonService() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val permissionRequested = sharedPreferences.getBoolean(KEY_PERMISSION_REQUESTED, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionRequested) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            } else {
                sharedPreferences.edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply()
                startPokemonService()
            }
        } else {
            startPokemonService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                sharedPreferences.edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply()
                startPokemonService()
            } else {
                Toast.makeText(
                    this,
                    "El permiso de notificaciones es necesario para el servicio.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startPokemonService() {
        val serviceIntent = Intent(this, PokemonUpdateService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun setupSearchBar() {
        findViewById<EditText>(R.id.et_search).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    pokemonViewModel.searchPokemons(query, searchByType = false) // Cambiar a true para buscar por tipo
                } else {
                    adapter.submitList(viewModel.pokemons.value) // Restaurar lista completa
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
