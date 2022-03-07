package com.example.group_d.data.model

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.group_d.*
import com.example.group_d.data.NotificationData
import com.example.group_d.data.PushNotification
import com.example.group_d.data.RetrofitInstance
import com.example.group_d.data.handler.NotificationHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject


class UserDataViewModel : ViewModel() {

    lateinit var applicationContext: Context
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

    private fun addGame(game: Game) {
        val gameCopy = games.value
        gameCopy?.add(game)
        games.value = gameCopy!!

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
                    //sendMessageToTopic(document.id, "New Friend request!")
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
            //send notification
            prepNotification("New Friend", "you have a new friendrequest", userID)
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
        //send notification
        prepNotification("Accepted Friend", "a friendrequest was accepted", newFriendUid)
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

    fun challengeFriend(userid: String, challange: Challenge) {
        Log.d(TAG, "creating challange request")
        // adding me to challanges of other user
        db.collection(COL_USER).document(userid).collection(USER_DATA).document(USER_CHALLENGES)
            .update(USER_CHALLENGES, FieldValue.arrayUnion(challange))
        // send notification
        prepNotification("new challenge", "you have been challenged!", userid)
        //sendMessageToTopic(userid, "New Challenge!")
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
        var yourTurn = 0
        //attach listener if game is unknown
        for (game in actualGames) {
            val localGameData = gameIdIsLocal(game)
            if (localGameData == null) {
                attachGameListener(game)
                yourTurn++
            }
        }
        if (yourTurn != 0){
            var textStr = "It is your Turn in " + yourTurn + " games!"
            //notificationHandler.sendNotification(textStr)
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
        val listener =
            db.collection(COL_GAMES).document(gameId).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val localGame = gameIdIsLocal(snapshot.id)
                    if (localGame == null) {
                        //create game
                        val beginner = snapshot[GAME_BEGINNER] as String
                        val gameData = snapshot[GAME_DATA] as ArrayList<String>
                        val gameType = snapshot[GAME_TYPE] as String
                        val players = snapshot[GAME_PLAYERS] as ArrayList<DocumentReference>
                        val newGame = Game(beginner, gameData, gameType, players)
                        newGame.id = gameId
                        addGame(newGame)
                    } else {
                        localGame.gameData = snapshot[GAME_DATA] as ArrayList<String>
                    }

                } else {
                    print("pass")
                }
            }
        gameListeners[gameId] = listener
    }

    private fun challengeListener(snapshot: DocumentSnapshot) {
        val actualChallenges = ArrayList<Challenge>()
        val currentChallenges = ArrayList<Challenge>()

        challenges.value!!.forEach {
            currentChallenges.add(it)
        }

        var yourChallenge = 0
        for (chall in snapshot.data?.get(USER_CHALLENGES) as ArrayList<HashMap<*, *>>) {
            val type = chall["gameType"]
            val userMap = chall["user"] as HashMap<*, *>
            val uid = userMap["id"]
            val name = userMap["name"]
            val status = userMap["online"]
            var userObj =
                User(id = uid as String, name = name as String, online = status as Boolean)
            if (userIsFriend(uid)) {
                for (user in friends.value!!) {
                    if (uid == user.id) {
                        userObj = user
                        break
                    }
                }
            }
            var newChallenge = Challenge(user = userObj, gameType = type as String)
            actualChallenges.add(newChallenge)
            if (newChallenge !in currentChallenges){
                yourChallenge++
            }
        }
        if (yourChallenge != 0){
            var textStr = "You have " + yourChallenge + " Challenges!"
            //notificationHandler.sendNotification(textStr)
        }
        //add new challenges
        challenges.value = actualChallenges

    }

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
                prepNotification("No Friend", "you were removed from a friendlist!", friendId)
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
        friendRequests.value!!.forEach {
            currentFriendRequests.add(it.friendID)
        }
        var yourFriends = 0
        for (request in actualFriendRequests) {
            if (request !in currentFriendRequests) {
                addFriendRequests.add(request as String)
                yourFriends++
            }
        }
        for (request in currentFriendRequests) {
            if (request !in actualFriendRequests) {
                removeFriendRequests.add(request)
            }
        }
        if (yourFriends != 0){
            var textStr = "You have " + yourFriends + " Friendrequests!"
            //notificationHandler.sendNotification(textStr)
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
        for (friendRequest in friendRequests.value!!) {
            if (friendRequest.friendID == request.friendID) {
                return false
            }
        }
        friendRequests.value!!.add(request)
        friendRequests.value = friendRequests.value
        return true
    }

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

    fun deleteFriend(friend: User) {
        if (userIsFriend(friend.id)){
            db.collection(COL_USER).document(FirebaseAuth.getInstance().uid.toString()).collection(USER_DATA).document(
                USER_FRIENDS).update(USER_FRIENDS, FieldValue.arrayRemove(friend.id))
            db.collection(COL_USER).document(friend.id).collection(USER_DATA).document(
                USER_FRIENDS).update(USER_FRIENDS, FieldValue.arrayRemove(auth.uid.toString()))
        }
    }

    private fun prepNotification(title: String, msg: String, topic: String){
        if(title.isNotEmpty() && msg.isNotEmpty()){
            val topicStr: String = "/topics/" + topic
            PushNotification(NotificationData(title, msg), topicStr).also {
                sendNotification(it)
            }
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful){
                /*
                 for some reason this line crashes the app
                 and since it is only used for debugging I just put it in a Comment
                 now everything should work fine
                */
                //Log.d(ContentValues.TAG, "Response: ${Gson().toJson(response)}")
            }
            else {
                Log.e(ContentValues.TAG, response.errorBody().toString())
            }
        } catch (e: Exception){
            Log.e(ContentValues.TAG, e.toString())
        }
    }
}
