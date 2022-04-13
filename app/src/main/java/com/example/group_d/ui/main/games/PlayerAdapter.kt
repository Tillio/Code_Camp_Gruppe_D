package com.example.group_d.ui.main.games

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.data.model.User

class PlayerAdapter : RecyclerView.Adapter<PlayerAdapter.ViewHolder>() {

    // list of players (you and the player you want to challenge)
    var playerItems: ArrayList<User> = ArrayList()

    // when the ViewHolder is created
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerAdapter.ViewHolder {
        // adds the player-item to the list
        var view = LayoutInflater.from(parent.context).inflate(R.layout.player_item, parent, false)
        return ViewHolder(view)
    }

    // writes the player-data onto the playeritem
    override fun onBindViewHolder(holder: PlayerAdapter.ViewHolder, position: Int) {
        // writes the name of the player
        holder.playerItemName.text = playerItems[position].displayName
    }

    // returns the amount of players
    override fun getItemCount(): Int {
        return playerItems.size
    }

    // holds the views for the player-items
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var playerItemImage: ImageView
        var playerItemName: TextView
        var playerItemStatus: TextView

        init {
            // gets the text-fields
            playerItemImage = itemView.findViewById(R.id.player_item_image)
            playerItemName = itemView.findViewById(R.id.player_item_name)
            playerItemStatus = itemView.findViewById(R.id.player_item_status)
        }
    }
}