package com.example.group_d.ui.main.ui.ingame

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.group_d.R

class TicTacToeFragment : Fragment() {

    companion object {
        fun newInstance() = TicTacToeFragment()
    }

    private lateinit var viewModel: TicTacToeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tic_tac_toe_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TicTacToeViewModel::class.java)
        // TODO: Use the ViewModel
    }

}