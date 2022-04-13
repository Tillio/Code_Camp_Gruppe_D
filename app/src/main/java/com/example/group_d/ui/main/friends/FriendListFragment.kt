package com.example.group_d.ui.main.friends

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.USER_DISPLAY_NAME
import com.example.group_d.USER_NAME
import com.example.group_d.USER_STATUS
import com.example.group_d.data.model.User
import com.example.group_d.data.model.UserDataViewModel

import com.example.group_d.databinding.FragmentFriendsListBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FriendsListFragment : Fragment(), FriendAdapter.FriendDeleter {
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
        var friendAdapter = FriendAdapter(this)
        userDataViewModel.friends.observe(viewLifecycleOwner) { newFriends ->
            friendAdapter = FriendAdapter(this)
            friendAdapter.friendItems = newFriends
            friendList.adapter = friendAdapter
        }

        friendList.adapter = friendAdapter
        friendList.layoutManager = LinearLayoutManager(context)
        val shareButton = binding.shareButton
        shareButton.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT,
                "http://groupd.example.com/addfriend/${userDataViewModel.getOwnUserID()}"
            )
            startActivity(Intent.createChooser(intent, "Share ID via"))
        }

        binding.rngButton.setOnClickListener{
            val ownID: String = userDataViewModel.getOwnUserID()
            val listUser: MutableList<User> = mutableListOf()
            db.collection("user").get().addOnCompleteListener {
                if (it.isSuccessful){
                    for (document in it.result!!){
                        if (document.id != ownID){
                            listUser.add(User(
                                name = document.get(USER_NAME).toString(),
                                id = document.id,
                                status = document.get(USER_STATUS).toString().toBoolean(),
                                displayName = document.get(USER_DISPLAY_NAME).toString()
                            ))
                        }
                    }
                    val rngUser: User = listUser.asSequence().shuffled().first()
                    findNavController().navigate(R.id.action_global_newGameSetup, bundleOf(
                        "userID" to rngUser.id,
                        "userName" to rngUser.name,
                        "userStatus" to rngUser.status,
                        "userDisplayName" to rngUser.displayName
                    ))
                }
            }
        }
        val friendRequestList: RecyclerView = binding.friendRequestList
        var friendRequestAdapter = FriendRequestAdapter()
        userDataViewModel.friendRequests.observe(viewLifecycleOwner) { newFriendRequests ->
            friendRequestAdapter = FriendRequestAdapter()
            friendRequestAdapter.friendRequestItems = newFriendRequests
            friendRequestList.adapter = friendRequestAdapter
        }
        friendRequestList.adapter = friendRequestAdapter
        friendRequestList.layoutManager = LinearLayoutManager(context)

        val addFriendButton = root.findViewById(R.id.addFriendButton) as Button
        val newFriendUsername = root.findViewById(R.id.editTextTextPersonName2) as TextView
        // set on-click listener for sending friend requests
        addFriendButton.setOnClickListener {
            userDataViewModel.sendFriendRequest(newFriendUsername.text.toString())
        }
        //userDataViewModel.testAcceptFriendRequest()

        arguments?.getString("userID")?.let {
            if (it == userDataViewModel.getOwnUserID()) {
                return@let
            }
            userDataViewModel.sendFriendRequestToID(it)
            val requestMsg = getString(R.string.friend_request_to_id1) +
                    it +
                    getString(R.string.friend_request_to_id2)
            Toast.makeText(activity, requestMsg, Toast.LENGTH_SHORT).show()
        }
        return root
    }

    override fun deleteFriend(friend: User) {
        userDataViewModel.deleteFriend(friend)
    }
}