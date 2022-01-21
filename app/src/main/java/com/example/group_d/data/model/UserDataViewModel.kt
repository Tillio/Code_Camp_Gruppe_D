package com.example.group_d.data.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.ui.main.ui.friends.FriendRequestFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserDataViewModel : ViewModel(){

    lateinit var  auth : FirebaseAuth
    val db = Firebase.firestore

    val friends = ArrayList<User>()
    val friendRequests = ArrayList<User>()

    val games = ArrayList<Game>()
    val challenges = ArrayList<User>()

    fun sendFriendRequest(){
        print("send friend Request")
    }

    fun acceptFriendRequest(){
        print("accept friend Request")
    }

    fun loadFriends(){
        print("load friends")
    }

    fun loadGames(){
        print("load games")
    }

    fun createGame(){
        print("create Game")
    }

    fun loadChallenges(){
        print("load challenges")
    }

    fun challengeFriend(){
        print("challenge friend")
    }

    fun getUserInfo(){
        /*takes user id and returns:
            name
            status*/
    }
}