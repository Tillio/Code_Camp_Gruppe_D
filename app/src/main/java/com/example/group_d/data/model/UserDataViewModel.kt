package com.example.group_d.data.model

import android.content.ContentValues.TAG
import android.util.Log
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

    fun getUserId(username: String): String{
        var uid = ""
        /*takes username and returns uid*/
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "uid: ${document.id} of user: ${document["name"]} found")
                    uid = document.id
                }
            }
            .addOnFailureListener { exeption ->
                Log.w(TAG, "Error getting documents: ", exeption)
            }
        return uid
    }

}