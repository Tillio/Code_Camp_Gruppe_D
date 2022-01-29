package com.example.group_d.data.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.*
import com.example.group_d.ui.main.ui.friends.FriendRequestFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserDataViewModel : ViewModel() {

    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    var test = false

    val friends = ArrayList<User>()
    val friendsLiveData = MutableLiveData<ArrayList<User>>()
    val friendRequests = ArrayList<User>()

    val games = ArrayList<Game>()
    val challenges = ArrayList<User>()

    fun sendFriendRequest() {
        print("send friend Request")
    }

    fun acceptFriendRequest() {
        print("accept friend Request")
    }

    fun loadFriends() {
        print("load friends")
    }

    fun loadGames() {
        print("load games")
    }

    fun createGame() {
        print("create Game")
    }

    fun loadChallenges() {
        print("load challenges")
    }

    fun challengeFriend() {
        print("challenge friend")
    }

    fun getUserInfo() {
        /*takes user id and returns:
            name
            status*/
    }

    fun uidListToUser(uidList: ArrayList<String>) {

    }

    fun setupFireBaseSnapshots() {        //listen to own document

        val dataRef = db.collection(COL_USER).document(auth.uid.toString()).collection(USER_DATA)

        dataRef.document(USER_FRIEND_REQUESTS).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                //succes
                val get = snapshot.data?.get(USER_FRIEND_REQUESTS)
            } else
                print("pass")
        }

        dataRef.document(USER_GAMES).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                print("pass")
            } else
                print("pass")
        }

        dataRef.document(USER_CHALLENGES).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                print("pass")
            } else
                print("pass")
        }
        //listen to games
    }

    private fun friendRequestListener() {

    }

}