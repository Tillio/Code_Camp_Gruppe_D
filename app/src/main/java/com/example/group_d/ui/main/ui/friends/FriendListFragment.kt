package com.example.group_d.ui.main.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.data.model.UserDataViewModel

import com.example.group_d.databinding.FragmentFriendsListBinding


class FriendsListFragment : Fragment() {
    private var _binding: FragmentFriendsListBinding? = null
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private val binding get() = _binding!!

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

        /*binding.addFriendButton.setOnClickListener{
            val parentFrag: FriendsFragment =
                this.parentFragment as FriendsFragment
                parentFrag.showAddFriendScreen()
        }*/

        val friendRequestList: RecyclerView = binding.friendRequestList
        val friendRequestAdapter = FriendRequestAdapter()
        friendRequestAdapter.friendRequestItems = ArrayList(
            userDataViewModel.friendRequests
        )
        friendRequestList.adapter = friendRequestAdapter
        friendRequestList.layoutManager = LinearLayoutManager(context)

        val addFriendButton = root.findViewById(R.id.addFriendButton) as Button
        val newFriendUsername = root.findViewById(R.id.editTextTextPersonName2) as TextView
        // set on-click listener for sending friend requests
        addFriendButton.setOnClickListener {
            userDataViewModel.sendFriendRequest(newFriendUsername.text.toString())
        }
        //userDataViewModel.testAcceptFriendRequest()

        return root
    }
}