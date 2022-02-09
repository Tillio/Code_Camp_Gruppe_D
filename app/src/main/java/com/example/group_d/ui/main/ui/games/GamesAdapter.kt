package com.example.group_d.ui.main.ui.games

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.USER_NAME
import com.example.group_d.data.model.Game
import com.google.firebase.auth.FirebaseAuth

class GamesAdapter(private val games: ArrayList<Game>) :
    RecyclerView.Adapter<GamesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ownVsEnemyName: TextView = view.findViewById(R.id.own_vs_enemy_name)
        init {
            this.itemView.setOnClickListener {

            }
        }
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_game_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        for(doc in games[position].players) {
            if(doc.id != FirebaseAuth.getInstance().uid){
                doc.get().addOnSuccessListener {
                    val get = it.data?.get(USER_NAME)
                    holder.ownVsEnemyName.text = "You vs. $get"
                }
            }
        }



        R.layout.fragment_game_item
    }


    override fun getItemCount(): Int {
        return games.size
    }
}