package com.example.group_d.ui.main.ui.challenges

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.*
import com.example.group_d.data.model.Challenge
import com.example.group_d.data.model.Game
import com.example.group_d.data.model.GameType
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


    private val _challenges: MutableLiveData<List<Challenge>> by lazy {
        MutableLiveData<List<Challenge>>().apply {
            value = exampleChallenges()
        }
    }
    var challenges: LiveData<List<Challenge>> = _challenges

    fun updateChallenges(challenges :ArrayList<Challenge>){

        val _challenges: MutableLiveData<List<Challenge>> by lazy {
            MutableLiveData<List<Challenge>>().apply {
                value = challenges
            }
        }
        this.challenges = _challenges

    }

    private fun exampleChallenges(): MutableList<Challenge> {
        val challenges: MutableList<Challenge> = ArrayList()
        for (i in 1..10) {
            challenges.add(Challenge(User("YkqWGRLnRUTm1MaGivvcwb4Mn5s1", "n@m.de", true), GAME_TYPE_TIC_TAC_TOE))

        }
        return challenges
    }

    private fun removeChallenge(challenge: Challenge) {
        db.collection(COL_USER)
            .document(Firebase.auth.currentUser!!.uid).collection(USER_DATA)
            .document(USER_CHALLENGES).update(USER_CHALLENGES, FieldValue.arrayRemove(challenge))
        val mutChallenges: MutableList<Challenge> = _challenges.value!!.toMutableList()
        mutChallenges.remove(challenge)
        _challenges.value = mutChallenges
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
        return db.collection(COL_GAMES).add(game)
    }
}