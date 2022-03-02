package com.example.group_d.ui.main.recentGames.statistiks

sealed class StatisticsRecyclerItem {
    //General
    class Steps_Challenge(
        val totalGames: Int,
        val wins: Int,
        val best_steps: HashMap<Int,Int>,
        val total_steps: Int
    ) : StatisticsRecyclerItem()

    class Mental_Arithmetic(
        val totalGames: Int,
        val wins: Int,
        val best_time_in_s:String
    ): StatisticsRecyclerItem()

    class Compass(
        val totalGames: Int,
        val wins: Int,
        val best_time_in_s: String
    ): StatisticsRecyclerItem()

    class TicTacToe(
        val totalGames: Int,
        val wins: Int
    ): StatisticsRecyclerItem()

}