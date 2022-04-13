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
        // set status to true so the challenge can be found in database
        challenge.user.status = true
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
        val gameData = ArrayList<String>()
        when (challenge.gameType) {
            GAME_TYPE_COMPASS -> {
                gameData.add(Random.nextLong().toString())
            }
            GAME_TYPE_MENTAL_ARITHMETICS -> {
                val seed = Random.nextInt(1000000, 10000000)
                gameData.add(seed.toString())
            }
            GAME_TYPE_STEPS_GAME -> {
                gameData.add(Firebase.auth.currentUser!!.email + "=" + "gameTime" + "=" + challenge.stepGameTime.toString())
            }
        }
        val game = Game(
            Random.nextLong(players.size.toLong()).toString(),
            gameData,
            challenge.gameType,
            players
        )
        game.lastPlayer = when (challenge.gameType) {
            GAME_TYPE_TIC_TAC_TOE -> {
                // In Tic Tac Toe the last player is the player who is not the beginner
                players[1 - game.beginner.toInt()].id
            }
            else -> {
                // In other games this attribute isn't used
                ""
            }
        }
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