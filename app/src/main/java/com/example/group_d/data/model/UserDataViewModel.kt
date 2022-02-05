package com.example.group_d.data.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.group_d.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
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
        Log.d(TAG, "sending friend request to: $username")
        Log.d(TAG, "finding userID of $username ...")
        /*val otherUid = getUserIdByUsername(username)*/
        val ownUid = getOwnUserID()

        /*var otherUid = ""*/
        /*takes username and returns uid*/
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
            .addOnFailureListener { exeption ->
                Log.w(TAG, "Error getting documents: ", exeption)
            }
        return true
    }

    /*fun acceptFriendRequest(newFriendUsername: String) {
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
        db.collection("user").document(ownUid)
            .set(ownData, SetOptions.merge())


        //pushing data of the newly added friend to the database
        val newFriendData = hashMapOf(
            "friends" to newFriendFriends
        )
        db.collection("user").document(newFriendUid)
            .set(newFriendData, SetOptions.merge())
    }*/

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
            .addOnFailureListener { exeption ->
                Log.w(TAG, "error getting name of user ID: $uId")
            }

        db.collection("user").document(uId)
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

    /*fun getUserIdByUsername(username: String, methodToCall: (input: ) -> Unit): String {
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
            .addOnFailureListener { exeption ->
                Log.w(TAG, "Error getting documents: ", exeption)
            }
        return otherUid
    }*/

    fun getOwnUserID(): String {
        return Firebase.auth.currentUser!!.uid
    }

    private fun getFriendRequestsOfUid(uId: String): ArrayList<String> {
        var friendRequestsOfUid = arrayListOf<String>()
        db.collection("user").document(uId)
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
        db.collection("user").document(uId)
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
                print("pass")
            } else
                print("pass")
        }

        dataCol.document(USER_FRIENDS).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            friendListListener(snapshot)
        }
        //listen to games
    }

    /**
     * receives the Users Friends Document and synchronizes it with the local friendList
     */

    private fun friendListListener(snapshot: DocumentSnapshot?) {
        if (snapshot != null && snapshot.exists()) {
            val actualFriends = snapshot.data?.get(USER_FRIENDS) as ArrayList<String>
            val addFriends = ArrayList<String>()
            val removeFriends = ArrayList<String>()
            val currentFriends = ArrayList<String>()
            friends.forEach {
                currentFriends.add(it.id)
            }
            for (user in actualFriends) {
                if (user !in currentFriends) {
                    addFriends.add(user)
                }
            }
            for (user in currentFriends) {
                if (user !in currentFriends) {
                    addFriends.add(user)
                }
            }
            removeFriends(removeFriends)
            addFriends(addFriends)

        } else
            print("pass")

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
    private fun addFriend(friend: User) {
        for (user in friends) {
            if (user.id == friend.id) {
                return
            }
        }
        friends.add(friend)
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
