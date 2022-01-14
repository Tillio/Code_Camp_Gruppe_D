package com.example.group_d.ui.main.ui.games

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.data.model.User

class PlayerAdapter : RecyclerView.Adapter<PlayerAdapter.ViewHolder>() {

    var playerItems: ArrayList<User> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerAdapter.ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.player_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerAdapter.ViewHolder, position: Int) {
        holder.playerItemName.text = playerItems[position].name
    }

    override fun getItemCount(): Int {
        return playerItems.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var playerItemImage: ImageView
        var playerItemName: TextView
        var playerItemStatus: TextView

        init {
            playerItemImage = itemView.findViewById(R.id.player_item_image)
            playerItemName = itemView.findViewById(R.id.player_item_name)
            playerItemStatus = itemView.findViewById(R.id.player_item_status)
        }
    }
}