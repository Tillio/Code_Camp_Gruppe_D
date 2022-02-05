package com.example.group_d.ui.main.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.data.model.FriendRequest
import com.example.group_d.data.model.User
import com.example.group_d.data.model.UserDataViewModel

private val userDataViewModel =  UserDataViewModel()

class FriendRequestAdapter : RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>(){

    var friendRequestItems: ArrayList<FriendRequest> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.friend_request_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friendRequest = friendRequestItems[position]
        holder.bind(friendRequest)
    }

    override fun getItemCount(): Int {
        return friendRequestItems.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val panel = view.findViewById(R.id.friend_request_name_textView) as TextView
        val buttonAcc = view.findViewById(R.id.accept_button) as Button
        fun bind(request: FriendRequest){
            panel.text = request.friendID
            buttonAcc.setOnClickListener { view ->
                userDataViewModel.acceptFriendRequest(panel.text.toString())
            }
        }
    }
}