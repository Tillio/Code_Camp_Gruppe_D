package com.example.group_d.ui.main.challenges

import androidx.lifecycle.ViewModel
import com.example.group_d.*
import com.example.group_d.data.model.Challenge
import com.example.group_d.data.model.Game
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

class ChallengesViewModel : ViewModel() {

    val db = Firebase.firestore

    private fun removeChallenge(challenge: Challenge) {
        // set online status to online so the challenge can be found in database
        challenge.user.online = true
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
        val game = Game(Random.nextLong(players.size.toLong()).toString(), ArrayList(), challenge.gameType, players)
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