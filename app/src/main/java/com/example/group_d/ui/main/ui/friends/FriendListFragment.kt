package com.example.group_d.ui.main.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.data.model.User
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
        friendAdapter.friendItems = ArrayList(userDataViewModel.friends)
        friendList.adapter = friendAdapter
        friendList.layoutManager = LinearLayoutManager(context)



        binding.addFriendButton.setOnClickListener{
            val parentFrag: FriendsFragment =
                this.parentFragment as FriendsFragment
                parentFrag.showAddFriendScreen()
        }



        return root
    }

    fun createFriends(): List<User> {
        val arrayList = ArrayList<User>()
        arrayList.add(User(name ="user1" , id = "0", online = true))
        arrayList.add(User(name = "user2", id = "1", online = true))
        arrayList.add(User(name = "user3", id = "2", online = true))
        arrayList.add(User(name = "user4", id = "3", online = false))
        arrayList.add(User(name = "user1", id = "0", online = true))
        arrayList.add(User(name = "user2", id = "1", online = true))
        arrayList.add(User(name = "user3", id = "2", online = true))
        arrayList.add(User(name = "user4", id = "3", online = false))
        arrayList.add(User(name = "user1", id = "0", online = true))
        arrayList.add(User(name = "user2", id = "1", online = true))
        arrayList.add(User(name = "user3", id = "2", online = true))
        arrayList.add(User(name = "user4", id = "3", online = false))
        arrayList.add(User(name = "user1", id = "0", online = true))
        arrayList.add(User(name = "user2", id = "1", online = true))
        arrayList.add(User(name = "user3", id = "2", online = true))
        arrayList.add(User(name = "user4", id = "88", online = false))
        arrayList.add(User(name = "user1", id = "0", online = true))
        arrayList.add(User(name = "user02", id = "1", online = true))
        arrayList.add(User(name = "user3654", id = "2", online = true))
        arrayList.add(User(name = "user499", id = "99", online = false))

        return arrayList
    }


}