package com.example.group_d.ui.main.recentGames.statistiks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R

class StatisticsAdapter : RecyclerView.Adapter<StatisticsRecyclerViewHolder>() {

    var data = listOf<StatisticsRecyclerItem>()
        set(value) {
            field = value
            notifyDataSetChanged ()
        }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StatisticsRecyclerViewHolder {
        return when (viewType) {
            R.layout.item_statistics_compas -> StatisticsRecyclerViewHolder.CompassViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_statistics_compas, parent, false)
            )
            R.layout.item_statistics_mind_arithmethics -> StatisticsRecyclerViewHolder.ArithmeticViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_statistics_mind_arithmethics, parent, false)
            )
            R.layout.item_statistics_step_challenge -> StatisticsRecyclerViewHolder.StepsViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_statistics_step_challenge, parent, false)
            )
            R.layout.item_statistics_tictactoe -> StatisticsRecyclerViewHolder.TTTViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_statistics_tictactoe, parent, false)
            )
            else -> throw IllegalArgumentException("Invalid ViewType Provided")

        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is StatisticsRecyclerItem.Mental_Arithmetic -> R.layout.item_statistics_mind_arithmethics
            is StatisticsRecyclerItem.TicTacToe -> R.layout.item_statistics_tictactoe
            is StatisticsRecyclerItem.Steps_Challenge -> R.layout.item_statistics_step_challenge
            is StatisticsRecyclerItem.Compass -> R.layout.item_statistics_compas

        }
    }


    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: StatisticsRecyclerViewHolder, position: Int) {
        when (holder) {
            is StatisticsRecyclerViewHolder.TTTViewHolder -> holder.bind(data[position] as StatisticsRecyclerItem.TicTacToe)
            is StatisticsRecyclerViewHolder.CompassViewHolder -> holder.bind(data[position] as StatisticsRecyclerItem.Compass)
            is StatisticsRecyclerViewHolder.ArithmeticViewHolder -> holder.bind(data[position] as StatisticsRecyclerItem.Mental_Arithmetic)
            is StatisticsRecyclerViewHolder.StepsViewHolder -> holder.bind(data[position] as StatisticsRecyclerItem.Steps_Challenge)
        }
    }
}