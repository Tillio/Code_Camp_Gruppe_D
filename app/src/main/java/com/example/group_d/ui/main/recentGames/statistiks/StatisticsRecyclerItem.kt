package com.example.group_d.ui.main.recentGames.statistiks

sealed class StatisticsRecyclerItem {
    //General
    class Steps_Challenge(
        var totalGames: Int,
        val wins: Int,
        val best_steps: HashMap<Int,Int>,
        val total_steps: Int
    ) : StatisticsRecyclerItem()

    class Mental_Arithmetic(
        var totalGames: Int,
        val wins: Int,
        var best_time_in_s:String
    ): StatisticsRecyclerItem()

    class Compass(
        val totalGames: Int,
        val wins: Int,
        val best_time_in_s: String
    ): StatisticsRecyclerItem()

    class TicTacToe(
        var totalGames: Int,
        var wins: Int
    ): StatisticsRecyclerItem()

}