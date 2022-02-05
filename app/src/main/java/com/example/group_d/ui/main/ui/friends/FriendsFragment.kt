package com.example.group_d.ui.main.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.data.model.User

import com.example.group_d.databinding.FragmentFriendScreenBinding


class FriendsFragment : Fragment() {
    private lateinit var friendList: FriendsListFragment
    private lateinit var friendRequest: FriendRequestFragment
    private var _binding: FragmentFriendScreenBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendScreenBinding.inflate(inflater, container, false)

        val root: View = binding.root

        friendList = FriendsListFragment()
        friendRequest = FriendRequestFragment()
        childFragmentManager.beginTransaction().apply {
            replace(R.id.fl_friend_screen, friendList)
            commit()
        }

        return root
    }

    fun showAddFriendScreen(){
        childFragmentManager.beginTransaction().apply {
            replace(R.id.fl_friend_screen, friendRequest)
            commit()
        }
    }

    fun showFriendListScreen(){
        childFragmentManager.beginTransaction().apply {
            replace(R.id.fl_friend_screen, friendList)
            commit()
        }
    }



}