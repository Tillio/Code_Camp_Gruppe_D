package com.example.group_d.ui.main.ui.ingame

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.group_d.R
import com.example.group_d.databinding.TicTacToeFragmentBinding

class TicTacToeFragment : Fragment() {

    private lateinit var ticTacToeViewModel: TicTacToeViewModel
    private var _binding: TicTacToeFragmentBinding? = null
    private val args: TicTacToeFragmentArgs by navArgs()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ticTacToeViewModel =
            ViewModelProvider(this).get(TicTacToeViewModel::class.java)

        _binding = TicTacToeFragmentBinding.inflate(inflater, container, false)
        val root = binding.root
        val textOpName = binding.textOpName
        textOpName.text = args.userID
        // TODO Show profile pictures

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // TODO Save game
        Log.d(null, "Destroyed view of TicTacToeFragment")
        _binding = null
    }
}