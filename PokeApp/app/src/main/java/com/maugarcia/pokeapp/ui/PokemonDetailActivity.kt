package com.maugarcia.pokeapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.maugarcia.pokeapp.R
import com.maugarcia.pokeapp.data.local.entities.PokemonDetail
import com.maugarcia.pokeapp.databinding.ActivityPokemonDetailBinding
import com.maugarcia.pokeapp.ui.viewmodel.PokemonDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class PokemonDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPokemonDetailBinding
    private val viewModel: PokemonDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pokemonId = intent.getIntExtra(EXTRA_POKEMON_ID, -1)
        if (pokemonId == -1) {
            finish()
            return
        }

        observeViewModel()
        viewModel.loadPokemonDetail(pokemonId)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.pokemonDetail.collectLatest { detail ->
                detail?.let { updateUI(it) }
            }
        }
    }

    private fun updateUI(detail: PokemonDetail) {
        binding.apply {
            pokemonName.text = detail.name.capitalize(Locale.ROOT)
            pokemonHeight.text = "Height: ${detail.height / 10.0}m"
            pokemonWeight.text = "Weight: ${detail.weight / 10.0}kg"

            // Mostrar tipos
            val types = viewModel.getTypes(detail)
            typeChipGroup.removeAllViews()
            types.forEach { type ->
                val chip = Chip(this@PokemonDetailActivity).apply {
                    text = type
                    setChipBackgroundColorResource(getTypeColor(type))
                }
                typeChipGroup.addView(chip)
            }

            // Mostrar stats
            val stats = viewModel.getStats(detail)
            statsLayout.removeAllViews()
            stats.forEach { (statName, value) ->
                val statView = layoutInflater.inflate(
                    R.layout.item_stat,
                    statsLayout,
                    false
                )
                statView.findViewById<TextView>(R.id.statName).text = statName
                statView.findViewById<ProgressBar>(R.id.statBar).progress = value
                statView.findViewById<TextView>(R.id.statValue).text = value.toString()
                statsLayout.addView(statView)
            }

            // Cargar imagen del Pokémon con Glide
            Glide.with(this@PokemonDetailActivity)
                .load(detail.imageUrl)  // Usar detail.imageUrl directamente
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground) //imagene placeholder
                .error(R.drawable.baseline_notifications_24) //imgen de error
                .into(pokemonImage)
        }
    }

    private fun getTypeColor(type: String): Int {
        return when (type.lowercase()) {
            "fire" -> R.color.fire_type
            "water" -> R.color.water_type
            "grass" -> R.color.grass_type
            "electric" -> R.color.electric_type
            else -> R.color.default_type
        }
    }

    companion object {
        const val EXTRA_POKEMON_ID = "extra_pokemon_id"

        // Método para generar un Intent con el Pokémon ID
        fun createIntent(context: Context, pokemonId: Int): Intent {
            return Intent(context, PokemonDetailActivity::class.java).apply {
                putExtra(EXTRA_POKEMON_ID, pokemonId)
            }
        }
    }
}
