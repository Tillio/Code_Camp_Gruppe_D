package com.example.group_d.ui.main.ui.challenges

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.data.model.Challenge
import com.example.group_d.data.model.GameType
import com.example.group_d.data.model.User

class ChallengesViewModel : ViewModel() {

    private val _challenges: MutableLiveData<List<Challenge>> by lazy {
        MutableLiveData<List<Challenge>>().apply {
            value = exampleChallenges()
        }
    }
    val challenges: LiveData<List<Challenge>> = _challenges

    private fun exampleChallenges(): MutableList<Challenge> {
        val challenges: MutableList<Challenge> = ArrayList()
        for (i in 1..10) {
            challenges.add(Challenge(User("$i", "User $i", true), GameType.TIC_TAC_TOE))
        }
        return challenges
    }

    fun decline(challenge: Challenge) {
        val mutChallenges: MutableList<Challenge> = _challenges.value!!.toMutableList()
        mutChallenges.remove(challenge)
        _challenges.value = mutChallenges
    }
}