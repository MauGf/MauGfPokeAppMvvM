package com.maugarcia.pokeapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.maugarcia.pokeapp.R
import com.maugarcia.pokeapp.data.local.entities.Pokemon
import com.maugarcia.pokeapp.databinding.ItemPokemonBinding
import java.util.Locale

class PokemonAdapter(
    private val onPokemonClick: (Pokemon) -> Unit
) : ListAdapter<Pokemon, PokemonAdapter.PokemonViewHolder>(PokemonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        return PokemonViewHolder(
            ItemPokemonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onPokemonClick
        )
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PokemonViewHolder(
        private val binding: ItemPokemonBinding,
        private val onPokemonClick: (Pokemon) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pokemon: Pokemon) {
            with(binding) {
                pokemonName.text = pokemon.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                    else it.toString()
                }

                Glide.with(root.context)
                    .load(pokemon.imageUrl)
                    .centerCrop()
                    .into(pokemonImage)

                root.setOnClickListener {
                    onPokemonClick(pokemon)
                }
            }
        }
    }

    private class PokemonDiffCallback : DiffUtil.ItemCallback<Pokemon>() {
        override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem == newItem
        }
    }
}