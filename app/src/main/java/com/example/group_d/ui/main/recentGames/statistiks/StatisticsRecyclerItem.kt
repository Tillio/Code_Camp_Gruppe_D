package com.example.group_d.ui.main.recentGames.statistiks

sealed class StatisticsRecyclerItem {
    var wins = 0
    var totalGames = 0

    //General
    class Steps_Challenge(
        val best_steps: HashMap<Int, Int>,
        val total_steps: Int
    ) : StatisticsRecyclerItem()

    class Mental_Arithmetic(
        var best_time_in_s: String
    ) : StatisticsRecyclerItem()

    class Compass(
        val best_time_in_s: String
    ) : StatisticsRecyclerItem()

    class TicTacToe(
    ) : StatisticsRecyclerItem()

}