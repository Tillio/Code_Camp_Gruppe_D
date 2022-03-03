package com.example.group_d.ui.main.recentGames.statistiks

import androidx.lifecycle.ViewModel
import com.example.group_d.ui.main.recentGames.statistiks.StatisticsRecyclerItem.*

class StatisticsViewModel : ViewModel() {
    val pastGamesData = ArrayList<StatisticsRecyclerItem>()

    init {
        pastGamesData.addAll(getPastGameData())
    }

    private fun getPastGameData(): ArrayList<StatisticsRecyclerItem> {
        //steps
        var steps_games = 100
        var steps_wins = 50
        var total_steps = 10000
        var max_steps = hashMapOf<Int, Int>(
            5 to 500,
            10 to 1000,
            30 to 3000,
            60 to 6000
        )
        val stepsChallenge =
            Steps_Challenge(steps_games, steps_wins, max_steps, total_steps)

        //arithmetic
        var arithmetic_games = 1000
        var arithmetic_wins = 500
        var arithmetic_best_time = "0:45"
        val mentalArithmetic =
            Mental_Arithmetic(arithmetic_games, arithmetic_wins, arithmetic_best_time)


        //compass
        var compass_wins = 37
        var compass_games = 22
        var compass_best_time = "0:15"
        val compass = Compass(compass_games, compass_wins, compass_best_time)

        //ttt
        var ttt_wins = 200
        var ttt_games = 750
        val ticTacToe = TicTacToe(ttt_games, ttt_wins)

        return arrayListOf(stepsChallenge,mentalArithmetic,compass,ticTacToe)
    }
}