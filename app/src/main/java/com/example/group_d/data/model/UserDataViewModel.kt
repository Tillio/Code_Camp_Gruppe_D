package com.example.group_d.data.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class UserDataViewModel : ViewModel() {

    val TAG = "UserDataViewModel"

    lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    var friends = ArrayList<String>()
    var friendRequests = ArrayList<String>()

    val games = ArrayList<Game>()
    val challenges = ArrayList<User>()

    fun sendFriendRequest(username: String): Boolean {
        /*takes username and returns uid*/
        Log.d(TAG, "sending friend request to: $username")
        Log.d(TAG, "finding userID of $username ...")
        /*val otherUid = getUserIdByUsername(username)*/
        val ownUid = getOwnUserID()

        db.collection("user")
            .whereEqualTo("name", username)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "uid: ${document.id} of user: ${document["name"]} found")
                    val otherUid = document.id
                    db.collection("user").document(otherUid)
                        .update("friendRequests", FieldValue.arrayUnion(ownUid))
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
        return true
    }

    fun acceptFriendRequest(newFriendUid: String) {
        Log.d(TAG, "accepting friend request of $newFriendUid")

        val ownUid = getOwnUserID()

        //adding new friend to my own friendlist
        db.collection("user").document(ownUid)
            .update("friends", FieldValue.arrayUnion(newFriendUid))

        //removing new friend from my friend requests
        db.collection("user").document(ownUid)
            .update("friendRequests", FieldValue.arrayRemove(newFriendUid))

        //adding me to my new friends friendlist
        db.collection("user").document(newFriendUid)
            .update("friends", FieldValue.arrayUnion(ownUid))
    }

    fun testAcceptFriendRequest() {
        db.collection("user").document(getOwnUserID())
            .get()
            .addOnSuccessListener { document ->
                val friendRequestsArray = document["friendRequests"] as ArrayList<String>
                if(friendRequestsArray.isNotEmpty()) {
                    acceptFriendRequest(friendRequestsArray.first())
                }
            }
            .addOnFailureListener { exception ->
                print(exception)
            }
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

    fun getUserInfo(uId: String): HashMap<String, Any> {
        /*takes user id and returns:
            name
            status*/

        var returnName = ""
        var returnStatus = false

        db.collection("user").document(uId)
            .get()
            .addOnSuccessListener { document ->
                returnName = document["name"].toString()
                Log.d(TAG, "successfully got name of user ID: $uId")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "error getting name of user ID: $uId")
            }

        db.collection("user").document(uId)
            .get()
            .addOnSuccessListener { document ->
                returnStatus = document["status"].toString().toBoolean()
                Log.d(TAG, "successfully got status of user ID: $uId")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "error getting status of user ID: $uId")
            }
        return hashMapOf(
            "name" to returnName,
            "status" to returnStatus
        )
    }

    /*fun getUserIdByUsername(username: String): String {
        var otherUid = ""
        /*takes username and returns uid*/
        db.collection("user")
            .whereEqualTo("name", username)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "uid: ${document.id} of user: ${document["name"]} found")
                    otherUid = document.id

                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
        return otherUid
    }*/

    fun getOwnUserID(): String {
        val ownUid = Firebase.auth.currentUser!!.uid
        return ownUid
    }

    private fun getFriendRequestsOfUid(uId: String): ArrayList<String> {
        var friendRequestsOfUid = arrayListOf<String>()
        db.collection("user").document(uId)
            .get()
            .addOnSuccessListener { document ->
                friendRequestsOfUid = document["friendRequests"] as ArrayList<String>
                Log.d(TAG, "successfully got friendRequests of user ID: $uId")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "error getting friendRequests of user ID: $uId")
            }
        return friendRequestsOfUid
    }

    private fun getFriendsOfUid(uId: String): ArrayList<String> {
        var friendsOfUid = arrayListOf<String>()
        db.collection("user").document(uId)
            .get()
            .addOnSuccessListener { document ->
                friendsOfUid = document["friends"] as ArrayList<String>
                Log.d(TAG, "successfully got friends of user ID: $uId")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "error getting friends of user ID: $uId")
            }
        return friendsOfUid
    }


}