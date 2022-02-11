package com.example.group_d.ui.main.ui.challenges

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.*
import com.example.group_d.data.model.Challenge
import com.example.group_d.data.model.Game
import com.example.group_d.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.random.Random
import com.example.group_d.data.model.UserDataViewModel

class ChallengesViewModel : ViewModel() {

    val db = Firebase.firestore

    private fun removeChallenge(challenge: Challenge) {
        db.collection(COL_USER)
            .document(Firebase.auth.currentUser!!.uid).collection(USER_DATA)
            .document(USER_CHALLENGES).update(USER_CHALLENGES, FieldValue.arrayRemove(challenge))
    }

    fun decline(challenge: Challenge) {
        removeChallenge(challenge)
    }

    fun createGame(challenge: Challenge): Task<DocumentReference> {
        removeChallenge(challenge)
        val players = listOf(
            db.collection(COL_USER).document(Firebase.auth.currentUser!!.uid),
            db.collection(COL_USER).document(challenge.user.id)
        )
        val game = Game(Random.nextLong(players.size.toLong()), ArrayList(), challenge.gameType, players)
        return db.collection(COL_GAMES).add(game).addOnSuccessListener { gameRef ->
            db.collection(COL_USER)
                .document(Firebase.auth.currentUser!!.uid).collection(USER_DATA)
                .document(USER_GAMES).update(USER_GAMES, FieldValue.arrayUnion(gameRef.id))
            db.collection(COL_USER)
                .document(challenge.user.id).collection(USER_DATA)
                .document(USER_GAMES).update(USER_GAMES, FieldValue.arrayUnion(gameRef.id))
        }
    }
}