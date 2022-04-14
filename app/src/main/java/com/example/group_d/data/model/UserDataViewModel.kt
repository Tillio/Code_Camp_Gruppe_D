package com.example.group_d.data.model

import android.content.ContentValues
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.*
import com.example.group_d.data.NotificationData
import com.example.group_d.data.PushNotification
import com.example.group_d.data.RetrofitInstance
import com.example.group_d.data.handler.NotificationHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserDataViewModel : ViewModel() {

    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    val TAG = "UserDataViewModel"
    val db = Firebase.firestore
    var notificationHandler: NotificationHandler = NotificationHandler()

    var friends = MutableLiveData<ArrayList<User>>().apply {
        value = ArrayList()
    }
    var friendRequests = MutableLiveData<ArrayList<FriendRequest>>().apply {
        value = ArrayList()
    }
    val games = MutableLiveData<ArrayList<Game>>()
    val gameListeners: HashMap<String, ListenerRegistration> = HashMap()
    var challenges = MutableLiveData<ArrayList<Challenge>>().apply {
        value = ArrayList()
    }

    /**
     * adds a game Object to the model
     */
    private fun addGame(game: Game) {
        val gameCopy = games.value
        gameCopy?.add(game)
        games.value = gameCopy!!
    }

    /**
     * sends a friend request to a given email by adding this users uid to the given users
     * friend request document in firestore
     */
    fun sendFriendRequest(email: String, resources: Resources): Boolean {
        /*takes username and returns uid*/
        Log.d(TAG, "sending friend request to: $email")
        Log.d(TAG, "finding userID of $email ...")
        /*val otherUid = getUserIdByUsername(username)*/

        db.collection("user")
            .whereEqualTo("name", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "uid: ${document.id} of user: ${document["name"]} found")
                    sendFriendRequestToID(document.id, resources)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }


        return true
    }

    /**
     * sends a friend request to a given uid by adding this users uid to the given users
     * friend request document in firestore
     */
    fun sendFriendRequestToID(userID: String, resources: Resources) {
        if (userID != getOwnUserID()) {
            db.collection(COL_USER).document(userID).collection(USER_DATA)
                .document(USER_FRIEND_REQUESTS)
                .update(USER_FRIEND_REQUESTS, FieldValue.arrayUnion(getOwnUserID()))
            //send notification
            prepNotification(
                resources.getString(R.string.notify_new_friend_title),
                resources.getString(R.string.notify_new_friend_msg),
                userID
            )
        }
    }

    /**
     * accepts the friend request of a given uid by
     * adding the new frind to the users firend list
     * adding the user to the new friends friend list
     * removing the friend request from the users friend requests
     */
    fun acceptFriendRequest(newFriendUid: String, resources: Resources) {
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
        //send notification
        prepNotification(
            resources.getString(R.string.notify_acc_friend_title),
            resources.getString(R.string.notify_acc_friend_msg),
            newFriendUid
        )
    }


    /**
     * sends the given challenge to the given uid
     * by adding the challenge to the uids challenge list
     */
    fun challengeFriend(userid: String, challenge: Challenge, resources: Resources) {
        Log.d(TAG, "creating challange request")
        // adding me to challanges of other user
        db.collection(COL_USER).document(userid).collection(USER_DATA).document(USER_CHALLENGES)
            .update(USER_CHALLENGES, FieldValue.arrayUnion(challenge))
        // send notification
        prepNotification(
            resources.getString(R.string.notify_new_challenge_title),
            resources.getString(R.string.notify_new_challenge_msg),
            userid
        )
    }

    /**
     * returns the user's uid
     */
    fun getOwnUserID(): String {
        return auth.currentUser!!.uid
    }

    /**
     *returns a user's email
     */
    fun getOwnEmail(): String {
        return auth.currentUser!!.email.toString()
    }

    /**
     * returns the user's  name
     */
    fun getOwnDisplayName(): String {
        return auth.currentUser!!.displayName.toString()
    }

    /**
     *adds listeners to all relevant user documents in order to track changes in firestore
     * and handle them
     */
    fun setupFireBaseSnapshots() {
        val userRef = db.collection(COL_USER).document(auth.uid.toString())
        val dataCol = userRef.collection(USER_DATA)
        val documentTypes = arrayListOf(
            USER_FRIEND_REQUESTS,
            USER_GAMES,
            USER_CHALLENGES,
            USER_FRIENDS
        )
        for (type in documentTypes) {
            dataCol.document(type).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    handleSnapshotTrigger(snapshot)

                }
            }
        }
    }

    private fun handleSnapshotTrigger(snapshot: DocumentSnapshot) {
        if (games.value == null) {
            val gameList = ArrayList<Game>()
            games.value = gameList
        }

        //success
        val id = snapshot.id
        when (id) {
            USER_FRIEND_REQUESTS -> friendRequestsListListener(snapshot)
            USER_GAMES -> gamesListener(snapshot)
            USER_CHALLENGES -> challengeListener(snapshot)
            USER_FRIENDS -> friendListListener(snapshot)
        }
    }

    /**
     * listens to the users active games List and manages
     * attaching listeners to games or deletin them
     */
    private fun gamesListener(snapshot: DocumentSnapshot) {
        val actualGames = snapshot.data?.get(USER_GAMES) as ArrayList<String>
        //attach listener if game is unknown
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
                return
            }
        }
    }

    /**
     * takes a games id and returns game with the same id or null if the id is unknown
     */
    fun gameIdIsLocal(gameId: String): Game? {
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
        val listener =
            db.collection(COL_GAMES).document(gameId).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val any = snapshot["completionDate"]
                    if (any == 0L) {
                        val localGame = gameIdIsLocal(snapshot.id)
                        if (localGame == null) {
                            val newGame = gameDocToGameObj(snapshot)
                            addGame(newGame)
                        } else {
                            // Update game data
                            localGame.gameData = snapshot[GAME_DATA] as ArrayList<String>
                            // Update last player
                            localGame.lastPlayer = snapshot[GAME_LAST_PLAYER] as String
                        }

                    }
                } else {
                    print("pass")
                }
            }
        gameListeners[gameId] = listener
    }

    fun gameDocToGameObj(snapshot: DocumentSnapshot): Game {
        val beginner = snapshot[GAME_BEGINNER] as String
        val gameData = snapshot[GAME_DATA] as ArrayList<String>
        val gameType = snapshot[GAME_TYPE] as String
        val players = snapshot[GAME_PLAYERS] as ArrayList<DocumentReference>
        val lastPlayer = (snapshot[GAME_LAST_PLAYER]?:"") as String
        val newGame = Game(beginner, gameData, gameType, players)
        newGame.id = snapshot.id
        newGame.lastPlayer = lastPlayer
        return newGame
    }

    private fun challengeListener(snapshot: DocumentSnapshot) {
        val actualChallenges = ArrayList<Challenge>()
        val currentChallenges = ArrayList<Challenge>()

        challenges.value!!.forEach {
            currentChallenges.add(it)
        }

        for (chall in snapshot.data?.get(USER_CHALLENGES) as ArrayList<HashMap<*, *>>) {
            val type = chall[GAME_TYPE]
            val stepGameTime = chall[STEP_GAME_TIME] as Long
            val userMap = chall[COL_USER] as HashMap<*, *>
            val uid = userMap[USER_ID]
            val name = userMap[USER_NAME]
            val status = userMap[USER_STATUS]
            val displayName = userMap[USER_DISPLAY_NAME]
            var userObj = User(
                id = uid as String,
                name = name as String,
                status = status as Boolean,
                displayName = displayName as String
            )
            if (userIsFriend(uid)) {
                for (user in friends.value!!) {
                    if (uid == user.id) {
                        userObj = user
                        break
                    }
                }
            }
            val newChallenge = Challenge(user = userObj, gameType = type as String)
            newChallenge.stepGameTime = stepGameTime
            actualChallenges.add(newChallenge)
        }
        //add new challenges
        challenges.value = actualChallenges

    }

    /**
     * returns true if a given uid is in the users friend list
     */
    private fun userIsFriend(id: String): Boolean {
        for (friend in friends.value!!) {
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
        friends.value!!.forEach {
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
     * then it load the data from firestore for each id
     * and creates user objects when the call completes
     */
    private fun addFriends(uidList: ArrayList<String>) {
        for (uid in uidList) {
            db.collection(COL_USER).document(uid).get().addOnSuccessListener { doc ->
                val name = doc.data?.get(USER_NAME) as String
                val online = doc.data?.get(USER_STATUS) as Boolean
                val displayName = doc.data?.get(USER_DISPLAY_NAME).toString()
                val user = User(name = name, status = online, id = uid, displayName = displayName)
                addFriend(user)
            }
        }
    }

    /**
     * adds a user to the friend list if he is not already in the list
     */
    private fun addFriend(friend: User): Boolean {
        for (user in friends.value!!) {
            if (user.id == friend.id) {
                return false
            }
        }
        friends.value!!.add(friend)
        friends.value = friends.value
        return true
    }

    /**
     * takes an uId and removes a matching user if possible
     */
    private fun removeFriend(friendId: String): Boolean {
        for (friend in friends.value!!) {
            if (friend.id == friendId) {
                friends.value!!.remove(friend)
                friends.value = friends.value
                return true
            }
        }
        return false
    }

    /**
     * Gets Triggered when a edit to the users Friend Request document in Firestore is made.
     * then the update gets applied to the model
     */
    private fun friendRequestsListListener(snapshot: DocumentSnapshot?) {

        val actualFriendRequests = snapshot?.data?.get(USER_FRIEND_REQUESTS) as ArrayList<*>
        val addFriendRequests = ArrayList<String>()
        val removeFriendRequests = ArrayList<String>()
        val currentFriendRequests = ArrayList<String>()
        friendRequests.value!!.forEach {
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

    /**
     * removes a list of friend requests from the model
     * the list has to contain the uid of the request sender
     */
    private fun removeFriendRequests(removeFriendRequests: ArrayList<String>) {
        for (friendId in removeFriendRequests) {
            removeFriendRequest(friendId)
        }
    }

    /**
     * adds a list of friend requests to the model
     * the list has to contain the uids of the request sender
     */
    private fun addFriendRequests(uidList: ArrayList<String>) {
        for (uid in uidList) {
            db.collection(COL_USER).document(uid).get().addOnSuccessListener { doc ->
                val friendRequest = FriendRequest(friendID = uid)
                addFriendRequest(friendRequest)
            }
        }
    }

    /**
     * adds a given FriendRequest object into the model
     * returns true if successful else false
     */
    private fun addFriendRequest(request: FriendRequest): Boolean {
        for (friendRequest in friendRequests.value!!) {
            if (friendRequest.friendID == request.friendID) {
                return false
            }
        }
        friendRequests.value!!.add(request)
        friendRequests.value = friendRequests.value
        return true
    }

    /**
     * uses a given uid in order to find any friend requests made from this uid and deletes them
     * if succesful returns true else false
     */
    private fun removeFriendRequest(friendId: String): Boolean {
        for (friendRequest in friendRequests.value!!) {
            if (friendRequest.friendID == friendId) {
                friendRequests.value!!.remove(friendRequest)
                friendRequests.value = friendRequests.value
                return true
            }
        }
        return false
    }

    /**
     * removes a given user object from Firestore
     */
    fun deleteFriend(friend: User) {
        if (userIsFriend(friend.id)) {
            db.collection(COL_USER).document(auth.uid.toString()).collection(USER_DATA).document(
                USER_FRIENDS
            ).update(USER_FRIENDS, FieldValue.arrayRemove(friend.id))
            db.collection(COL_USER).document(friend.id).collection(USER_DATA).document(
                USER_FRIENDS
            ).update(USER_FRIENDS, FieldValue.arrayRemove(auth.uid.toString()))
        }
    }

    // prepare a notification
    fun prepNotification(title: String, msg: String, topic: String) {
        // if a title and message exist
        if (title.isNotEmpty() && msg.isNotEmpty()) {
            // build the topic from the id
            val topicStr: String = "/topics/" + topic
            // send notification to topic
            PushNotification(NotificationData(title, msg), topicStr).also {
                sendNotification(it)
            }
        }
    }

    // send notification
    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            // try to upload the notification / send message to firebase
            try {
                val response = RetrofitInstance.api.postNotification(notification)
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, e.toString())
            }
        }
}
