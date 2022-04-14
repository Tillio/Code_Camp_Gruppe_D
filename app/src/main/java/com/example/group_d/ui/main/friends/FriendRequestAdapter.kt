package com.example.group_d.ui.main.friends

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.data.model.FriendRequest
import com.example.group_d.data.model.UserDataViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
        val db = Firebase.firestore
        val panel = view.findViewById(R.id.friend_request_name_textView) as TextView
        val buttonAcc = view.findViewById(R.id.accept_button) as Button
        fun bind(request: FriendRequest){
            //panel.text = request.friendID
            db.collection("user").document(request.friendID)
                .get()
                .addOnSuccessListener { document ->
                    panel.text = document["name"].toString()
                    Log.d("FriendRequestAdapter", "successfully got name of user ID: $request.friendID")
                }
                .addOnFailureListener { exception ->
                    Log.w("FriendRequestAdapter", "error getting name of user ID: $request.friendID")
                }
            buttonAcc.setOnClickListener { view ->
                userDataViewModel.acceptFriendRequest(request.friendID, view.resources)
            }
        }
    }
}