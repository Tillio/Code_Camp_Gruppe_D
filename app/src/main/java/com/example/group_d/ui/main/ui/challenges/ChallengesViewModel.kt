package com.example.group_d.ui.main.ui.challenges

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.group_d.TIC_TAC_TOE
import com.example.group_d.data.model.Challenge
import com.example.group_d.data.model.GameType
import com.example.group_d.data.model.User
import com.example.group_d.data.model.UserDataViewModel

class ChallengesViewModel : ViewModel() {


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
            challenges.add(Challenge(User("$i", "User $i", true), TIC_TAC_TOE))
        }
        return challenges
    }

    fun decline(challenge: Challenge) {
        val mutChallenges: MutableList<Challenge> = _challenges.value!!.toMutableList()
        mutChallenges.remove(challenge)
        _challenges.value = mutChallenges
    }

    fun createGame(challenge: Challenge): Int {
        //TODO Create game
        return 42
    }
}