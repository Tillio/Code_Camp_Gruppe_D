package com.example.group_d.ui.main.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.data.model.User
import com.example.group_d.data.model.UserDataViewModel

import com.example.group_d.databinding.FragmentFriendsListBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase


class FriendsListFragment : Fragment() {
    private var _binding: FragmentFriendsListBinding? = null
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private val binding get() = _binding!!

    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsListBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val friendList: RecyclerView = binding.friendList
        val friendAdapter = FriendAdapter()
        friendAdapter.friendItems = ArrayList(
            userDataViewModel.friends
        )
        friendList.adapter = friendAdapter
        friendList.layoutManager = LinearLayoutManager(context)

        binding.addFriendButton.setOnClickListener{
            val parentFrag: FriendsFragment =
                this.parentFragment as FriendsFragment
                parentFrag.showAddFriendScreen()
        }
        binding.rngButton.setOnClickListener{
            val ownID: String = userDataViewModel.getOwnUserID()
            val listUser: MutableList<User> = mutableListOf()
            db.collection("user").get().addOnCompleteListener {
                if (it.isSuccessful){
                    for (document in it.result!!){
                        if (document.id != ownID){
                            listUser.add(User(name = document.get("name").toString(), id = document.id, online = document.get("status").toString().toBoolean()))
                        }
                    }
                    val rngUser: User = listUser.asSequence().shuffled().first()
                    findNavController().navigate(R.id.action_global_newGameSetup, bundleOf("userID" to rngUser.id, "userName" to rngUser.name, "userStatus" to rngUser.online))
                }
            }
        }

        val addFriendButton = root.findViewById(R.id.addFriendButton) as Button
        val newFriendUsername = root.findViewById(R.id.editTextTextPersonName2) as TextView
        // set on-click listener for sending friend requests
        addFriendButton.setOnClickListener {
            userDataViewModel.sendFriendRequest(newFriendUsername.text.toString())
        }
        userDataViewModel.testAcceptFriendRequest()

        return root
    }
}