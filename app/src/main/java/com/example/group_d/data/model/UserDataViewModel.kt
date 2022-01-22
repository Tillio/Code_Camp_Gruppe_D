package com.example.group_d.data.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.group_d.COL_GAMES
import com.example.group_d.COL_USER
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserDataViewModel : ViewModel() {

    val TAG = "UserDataViewModel"

    lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    var friends = ArrayList<String>()
    var friendRequests = ArrayList<String>()

    val games = ArrayList<Game>()
    val challenges = ArrayList<User>()

    fun sendFriendRequest(username: String): Boolean {
        Log.d(TAG, "sending friend request to: $username")
        Log.d(TAG, "finding userID of $username ...")
        val otherUid = getUserIdByUsername(username)
        val ownUid = getOwnUserID()
        val ownUserInfo = getUserInfo(ownUid)

        if (otherUid != "") {
            val otherFriendRequests = getFriendRequestsOfUid(otherUid)

            otherFriendRequests.add(ownUid)
            val data = hashMapOf("friendRequests" to otherFriendRequests)

            db.collection("users").document(otherUid)
                .set(data, SetOptions.merge())
            return true
        }
        return false
    }

    fun acceptFriendRequest(newFriendUsername: String) {
        Log.d(TAG, "accepting friend request of $newFriendUsername")

        val ownUid = getOwnUserID()
        val ownUserInfo = getUserInfo(ownUid)
        val newFriendUid = getUserIdByUsername(newFriendUsername)
        val newFriendFriends = getFriendsOfUid(newFriendUid)

        var newFriend = ""

        friendRequests = getFriendRequestsOfUid(ownUid)
        friends = getFriendsOfUid(ownUid)

        for(uId in friendRequests) {
            if(uId == getUserIdByUsername(newFriendUsername)) {
                newFriend = uId
            }
        }

        friends.add(newFriend)
        friendRequests.remove(newFriend)
        newFriendFriends.add(ownUid)


        //pushing own data to the database
        val ownData = hashMapOf(
            "friends" to friends,
            "friendRequests" to friendRequests
        )
        db.collection("users").document(ownUid)
            .set(ownData, SetOptions.merge())


        //pushing data of the newly added friend to the database
        val newFriendData = hashMapOf(
            "friends" to newFriendFriends
        )
        db.collection("users").document(newFriendUid)
            .set(newFriendData, SetOptions.merge())
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

    fun loadRunningGame(gameID: String) {
        val docref = db.collection(COL_GAMES).document(gameID)
        //docref.addSnapshotListener()
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

        db.collection(COL_USER).document(uId)
            .get()
            .addOnSuccessListener { document ->
                returnName = document["name"].toString()
                Log.d(TAG, "successfully got name of user ID: $uId")
            }
            .addOnFailureListener { exeption ->
                Log.w(TAG, "error getting name of user ID: $uId")
            }

        db.collection("users").document(uId)
            .get()
            .addOnSuccessListener { document ->
                returnStatus = document["status"].toString().toBoolean()
                Log.d(TAG, "successfully got status of user ID: $uId")
            }
            .addOnFailureListener { exeption ->
                Log.w(TAG, "error getting status of user ID: $uId")
            }
        return hashMapOf(
            "name" to returnName,
            "status" to returnStatus
        )
    }

    fun getUserIdByUsername(username: String): String {
        var otherUid = ""
        /*takes username and returns uid*/
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "uid: ${document.id} of user: ${document["name"]} found")
                    otherUid = document.id
                }
            }
            .addOnFailureListener { exeption ->
                Log.w(TAG, "Error getting documents: ", exeption)
            }
        return otherUid
    }

    fun getOwnUserID(): String {
        val ownUid = Firebase.auth.currentUser!!.uid
        return ownUid
    }

    private fun getFriendRequestsOfUid(uId: String): ArrayList<String> {
        var friendRequestsOfUid = arrayListOf<String>()
        db.collection("users").document(uId)
            .get()
            .addOnSuccessListener { document ->
                friendRequestsOfUid = document["friendRequests"] as ArrayList<String>
                Log.d(TAG, "successfully got friendRequests of user ID: $uId")
            }
            .addOnFailureListener { exeption ->
                Log.w(TAG, "error getting friendRequests of user ID: $uId")
            }
        return friendRequestsOfUid
    }

    private fun getFriendsOfUid(uId: String): ArrayList<String> {
        var friendsOfUid = arrayListOf<String>()
        db.collection("users").document(uId)
            .get()
            .addOnSuccessListener { document ->
                friendsOfUid = document["friends"] as ArrayList<String>
                Log.d(TAG, "successfully got friends of user ID: $uId")
            }
            .addOnFailureListener { exeption ->
                Log.w(TAG, "error getting friends of user ID: $uId")
            }
        return friendsOfUid
    }


}