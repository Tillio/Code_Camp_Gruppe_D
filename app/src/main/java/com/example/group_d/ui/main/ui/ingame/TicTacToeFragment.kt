package com.example.group_d.ui.main.ui.ingame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.group_d.R
import com.example.group_d.data.model.tictactoe.TicTacToeGame
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
        val waitSymbol = binding.wait
        val textPlayerAction = binding.textPlayerAction
        textPlayerAction.setText(R.string.action_your_turn)
        ticTacToeViewModel.loadGame(args.gameID)
        textOpName.text = ticTacToeViewModel.opponentName

        val fieldIDs = resources.obtainTypedArray(R.array.tic_tac_toe_fields)
        val fieldButtons: Array<ImageView> = Array(TicTacToeGame.NUM_FIELDS) { i ->
            val id = fieldIDs.getResourceId(i, -1)
            root.findViewById(id)
        }
        fieldIDs.recycle()
        for ((i, fieldButton) in fieldButtons.withIndex()) {
            // TODO
            fieldButton.setImageDrawable(null)
            fieldButton.setOnClickListener {
                if (!ticTacToeViewModel.fieldIsEmpty(i)) {
                    Toast.makeText(activity, R.string.field_not_empty, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                ticTacToeViewModel.move(i)
                fieldButton.setImageResource(R.drawable.ic_baseline_panorama_fish_eye_96)
                if (ticTacToeViewModel.checkWin(i)) {
                    // TODO Show winScreen
                    Toast.makeText(activity, "Congrats! You win", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                waitSymbol.visibility = View.VISIBLE
                val actionText = getString(R.string.action_wait_text1) +
                        ticTacToeViewModel.opponentName +
                        getString(R.string.action_wait_text2)
                textPlayerAction.text = actionText
                val opField = ticTacToeViewModel.getOpponentMove()
                ticTacToeViewModel.moveOpponent(opField)
                fieldButtons[opField].setImageResource(R.drawable.ic_baseline_close_96)
                waitSymbol.visibility = View.INVISIBLE
                if (ticTacToeViewModel.checkWin(opField)) {
                    // TODO Show winScreen
                    Toast.makeText(activity, "Booh! You lose", Toast.LENGTH_SHORT).show()
                    textPlayerAction.text = ""
                    return@setOnClickListener
                }
                textPlayerAction.setText(R.string.action_your_turn)
            }
        }
        // TODO Show profile pictures

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}