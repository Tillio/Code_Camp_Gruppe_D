package com.example.group_d.ui.main.recentGames

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.group_d.R

class RecentGamesFragment : Fragment() {

    companion object {
        fun newInstance() = RecentGamesFragment()
    }

    private lateinit var viewModel: RecentGamesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.recent_games_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RecentGamesViewModel::class.java)
        // TODO: Use the ViewModel
    }

}