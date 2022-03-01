package com.example.group_d.ui.main.recentGames

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.*
import com.example.group_d.data.model.Game
import com.example.group_d.data.model.UserDataViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RecentGamesViewModel : ViewModel() {
    var userDataViewModel: UserDataViewModel? = null
    private var recentGames: ArrayList<Game> = ArrayList()
    var recentGamesLive: MutableLiveData<ArrayList<Game>> = MutableLiveData(ArrayList())
    private lateinit var listener: ListenerRegistration
    private val db = Firebase.firestore
    private var doc: DocumentReference? = null
    private var pastGamesQuerry: Query? = null


    //.whereNotEqualTo(GAME_COMPLETION_DATE, 0)
    fun startListeningToRecentGames() {
        if (doc == null) {
            db.collection(COL_USER).document(FirebaseAuth.getInstance().uid.toString()).get()
                .addOnSuccessListener {
                    doc = it.reference
                    pastGamesQuerry = db.collection(COL_GAMES).whereArrayContains(GAME_PLAYERS, doc!!).whereNotEqualTo(
                        GAME_COMPLETION_DATE, 0)
                    //pastGamesQuerry = db.collection(COL_GAMES).whereEqualTo(GAME_TYPE, GAME_TYPE_TIC_TAC_TOE)
                    startOldGamesListener()
                }
        } else {
            startOldGamesListener()
        }
    }

    fun startOldGamesListener() {
        if (pastGamesQuerry != null){
            listener = pastGamesQuerry!!.addSnapshotListener { docs, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (docs != null) {
                    loadRecentGames(docs)
                }
            }
        }

    }

    private fun loadRecentGames(docs: QuerySnapshot) {

        val newList = ArrayList<Game>()
        for (doc in docs) {
            userDataViewModel?.gameDocToGameObj(doc)?.let { newList.add(it) }
        }
        newList.sortBy { it.completionDate }
        updateRecentGamesList(newList)
    }

    fun updateRecentGamesList(newList: ArrayList<Game>) {
        recentGames = newList
        recentGamesLive.value = newList

    }

    fun addUserDataViewModel(viewModel: UserDataViewModel) {
        if (userDataViewModel == null) {
            userDataViewModel = viewModel
        }
    }

    fun stopListeningToRecentGames() {
        listener.remove()
    }
}