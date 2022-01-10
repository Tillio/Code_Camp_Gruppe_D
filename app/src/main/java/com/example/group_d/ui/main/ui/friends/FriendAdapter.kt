package com.example.group_d.ui.main.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.data.model.User

class FriendAdapter : RecyclerView.Adapter<FriendAdapter.ViewHolder>(){

    var friendItems: ArrayList<User> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.friend_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friendItems[position]
        holder.bind(friend.name)
    }

    override fun getItemCount(): Int {
        return friendItems.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val panel = view.findViewById(R.id.friend_name_textView) as TextView
        val buttonInv = view.findViewById(R.id.invite_button) as Button
        fun bind(name: String){
            panel.text = name
            buttonInv.setOnClickListener { view -> view.findNavController().navigate(R.id.action_global_newGameSetup) }
        }
    }
}