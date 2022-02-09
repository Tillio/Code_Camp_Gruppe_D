package com.example.group_d.ui.main.ui.games

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.USER_NAME
import com.example.group_d.data.model.Challenge
import com.example.group_d.data.model.Game

class GamesAdapter(private val games: MutableList<Game>) :
    RecyclerView.Adapter<GamesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ownVsEnemyName: TextView = view.findViewById(R.id.own_vs_enemy_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_game_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        games[position].players[1].get().addOnCompleteListener{
            holder.ownVsEnemyName.text = "loaded"
        }
        holder.ownVsEnemyName.text = "You vs. " + "loading"

    }


    override fun getItemCount(): Int {
        return games.size
    }
}