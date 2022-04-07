package com.example.group_d.ui.main.recentGames.statistiks

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.github.mikephil.charting.charts.HorizontalBarChart

sealed class StatisticsRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    class TTTViewHolder(private val view: View) : StatisticsRecyclerViewHolder(view) {
        fun bind(ttt: StatisticsRecyclerItem.TicTacToe) {
            val total_games_Textview: TextView = view.findViewById(R.id.total_ttt_wins)
            val total_wins_texatview: TextView = view.findViewById(R.id.total_ttt_games)

            total_games_Textview.text= ttt.totalGames.toString()
            total_wins_texatview.text= ttt.wins.toString()
        }
    }

    class StepsViewHolder(private val view: View) : StatisticsRecyclerViewHolder(view) {
        fun bind(steps: StatisticsRecyclerItem.Steps_Challenge) {
            val total_steps: TextView = view.findViewById(R.id.total_steps)
            val total_wins: TextView= view.findViewById(R.id.total_step_wins)
            val total_games:TextView = view.findViewById(R.id.total_step_games)
            val max_steps:HorizontalBarChart = view.findViewById(R.id.max_steps_chart)
            total_wins.text = steps.wins.toString()
            total_steps.text = steps.total_steps.toString()
            total_games.text = steps.totalGames.toString()

        }
    }

    class CompassViewHolder(private val view: View) : StatisticsRecyclerViewHolder(view) {
        fun bind(compass: StatisticsRecyclerItem.Compass) {
            val total_compass_games: TextView = view.findViewById(R.id.total_compass_games)
            val total_compass_wins: TextView = view.findViewById(R.id.total_compass_wins)
            val best_compass_time: TextView = view.findViewById(R.id.best_compass_time)

            total_compass_games.text = compass.totalGames.toString()
            total_compass_wins.text = compass.wins.toString()
            best_compass_time.text = compass.best_time_in_s
        }
    }

    class ArithmeticViewHolder(private val view: View) : StatisticsRecyclerViewHolder(view) {
        fun bind(arithmetic: StatisticsRecyclerItem.Mental_Arithmetic) {
            val best_arithmetic_time: TextView = view.findViewById(R.id.best_arithmetic_time)
            val total_arithmetics_games: TextView = view.findViewById(R.id.total_arithmetics_games)
            val total_arithmetics_wins: TextView = view.findViewById(R.id.total_arithmetics_wins)

            best_arithmetic_time.text = arithmetic.best_time_in_s
            total_arithmetics_games.text = arithmetic.totalGames.toString()
            total_arithmetics_wins.text = arithmetic.wins.toString()

        }
    }
}

