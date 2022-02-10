package com.example.group_d.data.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class UserDataViewModel : ViewModel() {

    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    val TAG = "UserDataViewModel"
    val db = Firebase.firestore

    var friends = ArrayList<User>()
    var friendRequests = ArrayList<FriendRequest>()
    val games = MutableLiveData<ArrayList<Game>>()
    val gameListeners: HashMap<String, ListenerRegistration> = HashMap()
    var challenges = ArrayList<Challenge>()

    private fun addGame(game: Game) {
        var gameCopy = games.value
        gameCopy?.add(game)
        games.value = gameCopy!!

    }

    private fun removeGame(game: Game) {
        var gameList = ArrayList<Game>()
        gameList.remove(game)
        games.value = gameList

    }

    fun sendFriendRequest(username: String): Boolean {
        /*takes username and returns uid*/
        Log.d(TAG, "sending friend request to: $username")
        Log.d(TAG, "finding userID of $username ...")
        /*val otherUid = getUserIdByUsername(username)*/

        db.collection("user")
            .whereEqualTo("name", username)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "uid: ${document.id} of user: ${document["name"]} found")
                    sendFriendRequestToID(document.id)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
        return true
    }

    fun sendFriendRequestToID(userID: String) {
        if (userID != getOwnUserID()) {
            db.collection(COL_USER).document(userID).collection(USER_DATA)
                .document(USER_FRIEND_REQUESTS)
                .update(USER_FRIEND_REQUESTS, FieldValue.arrayUnion(getOwnUserID()))
        }
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
                    print(friendRequestsArray) //debug
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

    fun challengeFriend(userid: String, challange: Challenge) {
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
                friendRequestsListListener(snapshot)
            } else
                print("pass")
        }

        dataCol.document(USER_GAMES).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                if (games.value == null) {
                    val gameList = ArrayList<Game>()
                    games.value = gameList
                }
                gamesListener(snapshot)
            } else
                print("pass")
        }

        dataCol.document(USER_CHALLENGES).addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                challengeListener(snapshot)
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

    /**
     * listens to the users active games List and manages
     * attaching listeners to games or deletin them
     */
    private fun gamesListener(snapshot: DocumentSnapshot) {
        val actualGames = snapshot.data?.get(USER_GAMES) as ArrayList<String>
        //attach listener if game ist unknown
        for (game in actualGames) {
            val localGameData = gameIdIsLocal(game)
            if (localGameData == null) {
                attachGameListener(game)
            }
        }
        //delete old games
        for (game in games.value!!) {
            if (game.id !in actualGames) {
                gameListeners.remove(game.id)
                val value2 = games.value
                value2?.remove(game)
                games.value = value2!!
                removeGame(game)
                return
            }
        }
    }

    /**
     * takes a games id and returns game with the same id or null if the id is unknown
     */
    private fun gameIdIsLocal(gameId: String): Game? {
        for (game in games.value!!) {
            if (gameId == game.id) {
                return game
            }
        }
        return null
    }

    /**
     * Adds a snapshotListener to a gameDocument which loads the game and updates it
     */
    private fun attachGameListener(gameId: String) {
        var listener =
            db.collection(COL_GAMES).document(gameId).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val localGame = gameIdIsLocal(snapshot.id)
                    if (localGame == null) {
                        //create game
                        val beginner = snapshot[GAME_BEGINNER] as Long
                        val gameData = snapshot[GAME_DATA] as ArrayList<Long>
                        val gameType = snapshot[GAME_TYPE] as String
                        val players = snapshot[GAME_PLAYERS] as ArrayList<DocumentReference>
                        val newGame = Game(beginner, gameData, gameType, players)
                        newGame.id = gameId
                        addGame(newGame)
                    } else {
                        localGame.gameData = snapshot[GAME_DATA] as ArrayList<Long>
                    }

                } else {
                    print("pass")
                }
            }
        gameListeners[gameId] = listener
    }

    private fun challengeListener(snapshot: DocumentSnapshot) {
        val actualChallenges = ArrayList<Challenge>()

        for (chall in snapshot.data?.get(USER_CHALLENGES) as ArrayList<HashMap<*, *>>) {
            val type = chall["gameType"]
            val userMap = chall["user"] as HashMap<*, *>
            val uid = userMap["id"]
            val name = userMap["name"]
            val status = userMap["online"]
            var userObj =
                User(id = uid as String, name = name as String, online = status as Boolean)
            if (userIsFriend(uid as String)) {
                for (user in friends) {
                    if (uid == user.id) {
                        userObj = user
                        break;
                    }
                }
            }
            actualChallenges.add(Challenge(user = userObj, gameType = type as String))
        }
        //add new challenges
        challenges = actualChallenges

    }

    private fun userIsFriend(id: String): Boolean {
        for (friend in friends) {
            if (friend.id == id) {
                return true
            }
        }
        return false
    }


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
            if (user !in actualFriends) {
                removeFriends.add(user)
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

    private fun friendRequestsListListener(snapshot: DocumentSnapshot?) {

        val actualFriendRequests = snapshot?.data?.get(USER_FRIEND_REQUESTS) as ArrayList<*>
        val addFriendRequests = ArrayList<String>()
        val removeFriendRequests = ArrayList<String>()
        val currentFriendRequests = ArrayList<String>()
        friendRequests.forEach {
            currentFriendRequests.add(it.friendID)
        }
        for (request in actualFriendRequests) {
            if (request !in currentFriendRequests) {
                addFriendRequests.add(request as String)
            }
        }
        for (request in currentFriendRequests) {
            if (request !in actualFriendRequests) {
                removeFriendRequests.add(request)
            }
        }
        removeFriendRequests(removeFriendRequests)
        addFriendRequests(addFriendRequests)
    }

    private fun removeFriendRequests(removeFriendRequests: ArrayList<String>) {
        for (friendId in removeFriendRequests) {
            removeFriendRequest(friendId)
        }
    }

    private fun addFriendRequests(uidList: ArrayList<String>) {
        for (uid in uidList) {
            db.collection(COL_USER).document(uid).get().addOnSuccessListener { doc ->
                val friendRequest = FriendRequest(friendID = uid)
                addFriendRequest(friendRequest)
            }
        }
    }

    private fun addFriendRequest(request: FriendRequest): Boolean {
        for (friendRequest in friendRequests) {
            if (friendRequest.friendID == request.friendID) {
                return false
            }
        }
        friendRequests.add(request)
        return true
    }

    private fun removeFriendRequest(friendId: String): Boolean {
        for (friendRequest in friendRequests) {
            if (friendRequest.friendID == friendId) {
                friendRequests.remove(friendRequest)
                return true
            }
        }
        return false
    }
}
