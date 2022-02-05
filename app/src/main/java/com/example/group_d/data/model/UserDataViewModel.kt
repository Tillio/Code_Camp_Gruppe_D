package com.example.group_d.data.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.group_d.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class UserDataViewModel : ViewModel() {

    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    val TAG = "UserDataViewModel"
    val db = Firebase.firestore

    var friends = ArrayList<User>()
    var friendRequests = ArrayList<User>()
    val games = ArrayList<Game>()
    val challenges = ArrayList<Challenge>()

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
                    db.collection("user").document(otherUid).collection("userData")
                        .document("friendRequests")
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
        db.collection("user").document(ownUid).collection("userData").document("friends")
            .update("friends", FieldValue.arrayUnion(newFriendUid))

        //removing new friend from my friend requests
        db.collection("user").document(ownUid).collection("userData").document("friendRequests")
            .update("friendRequests", FieldValue.arrayRemove(newFriendUid))

        //adding me to my new friends friendlist
        db.collection("user").document(newFriendUid).collection("userData").document("friends")
            .update("friends", FieldValue.arrayUnion(ownUid))
    }

    fun testAcceptFriendRequest() {
        db.collection("user").document(getOwnUserID()).collection("userData")
            .document("friendRequests")
            .get()
            .addOnSuccessListener { document ->
                val friendRequestsArray = document["friendRequests"] as ArrayList<String>
                if (friendRequestsArray.isNotEmpty()) {
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

    fun challengeFriend(userid:String, challange: Challenge) {
        Log.d(TAG, "creating challange request")
        // adding me to challanges of other user
        db.collection(COL_USER).document(userid).collection(USER_DATA).document(USER_CHALLENGES)
            .update(USER_CHALLENGES, FieldValue.arrayUnion(challange))
    }

    fun getOwnUserID(): String {
        return Firebase.auth.currentUser!!.uid
    }

    //listen to own document
    fun setupFireBaseSnapshots() {
        val userRef = db.collection(COL_USER).document(auth.uid.toString())
        val dataCol = userRef.collection(USER_DATA)

        dataCol.document(USER_FRIEND_REQUESTS).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                //success
                val get = snapshot.data?.get(USER_FRIEND_REQUESTS)
            } else
                print("pass")
        }

        dataCol.document(USER_GAMES).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                print("pass")
            } else
                print("pass")
        }

        dataCol.document(USER_CHALLENGES).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                //challengeListener(snapshot)
            } else
                print("pass")
        }

        dataCol.document(USER_FRIENDS).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                friendListListener(snapshot)

            } else {
                print("pass")
            }
        }
        //listen to games
    }

    /*private fun challengeListener(snapshot: DocumentSnapshot) {
        val actualChallenges = snapshot?.data?.get(USER_CHALLENGES) as ArrayList<*>
        val addChallengesBinding

    }*/

    /**
     * receives the Users Friends Document and synchronizes it with the local friendList
     */

    private fun friendListListener(snapshot: DocumentSnapshot?) {

        val actualFriends = snapshot?.data?.get(USER_FRIENDS) as ArrayList<*>
        val addFriends = ArrayList<String>()
        val removeFriends = ArrayList<String>()
        val currentFriends = ArrayList<String>()
        friends.forEach {
            currentFriends.add(it.id)
        }
        for (user in actualFriends) {
            if (user !in currentFriends) {
                addFriends.add(user as String)
            }
        }
        for (user in currentFriends) {
            if (user !in currentFriends) {
                addFriends.add(user)
            }
        }
        removeFriends(removeFriends)
        addFriends(addFriends)


    }

    /**
     * takes a list of user ids and deletes all user with matching ids
     */
    private fun removeFriends(removeFriends: ArrayList<String>) {
        for (friendId in removeFriends) {
            removeFriend(friendId)
        }
    }

    /**
     * takes a list of uIds
     * the it load the data from firestore for each id
     * and creates user objects when the call completes
     */
    private fun addFriends(uidList: ArrayList<String>) {
        for (uid in uidList) {
            db.collection(COL_USER).document(uid).get().addOnSuccessListener { doc ->
                val name = doc.data?.get(USER_NAME) as String
                val online = doc.data?.get(USER_STATUS) as Boolean
                val user = User(name = name, online = online, id = uid)
                addFriend(user)
            }
        }
    }

    /**
     * adds a user to the friend list if he is not already in the list
     */
    private fun addFriend(friend: User): Boolean {
        for (user in friends) {
            if (user.id == friend.id) {
                return false
            }
        }
        friends.add(friend)
        return true
    }

    /**
     * takes an uId and removes a matching user if possible
     */
    private fun removeFriend(friendId: String): Boolean {
        for (friend in friends) {
            if (friend.id == friendId) {
                friends.remove(friend)
                return true
            }
        }
        return false
    }
}
