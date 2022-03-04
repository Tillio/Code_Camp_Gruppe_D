package com.example.group_d.ui.main.ingame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.group_d.R
import com.example.group_d.data.model.GameEnding
import com.example.group_d.data.model.TicTacToeModel
import com.example.group_d.data.model.UserDataViewModel
import com.example.group_d.databinding.TicTacToeFragmentBinding

class TicTacToeFragment : Fragment(), GiveUpReceiver {

    private lateinit var ticTacToeViewModel: TicTacToeViewModel
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private var _binding: TicTacToeFragmentBinding? = null
    private val args: TicTacToeFragmentArgs by navArgs()
    private lateinit var waitSymbol: ProgressBar
    private lateinit var textPlayerAction: TextView
    private lateinit var fieldButtons: Array<ImageView>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ticTacToeViewModel =
            ViewModelProvider(this)[TicTacToeViewModel::class.java]

        _binding = TicTacToeFragmentBinding.inflate(inflater, container, false)
        val root = binding.root
        val textOpName = binding.textOpName
        waitSymbol = binding.wait
        textPlayerAction = binding.textPlayerAction
        val giveUp = binding.buttonGiveUp
        run {
            val fieldIDs = resources.obtainTypedArray(R.array.tic_tac_toe_fields)
            fieldButtons = Array(TicTacToeModel.NUM_FIELDS) { i ->
                val id = fieldIDs.getResourceId(i, -1)
                root.findViewById(id)
            }
            fieldIDs.recycle()
        }

        ticTacToeViewModel.nextField.observe(viewLifecycleOwner) { new_val ->
            val symID =
                if (ticTacToeViewModel.isOnTurn()) R.drawable.ic_baseline_panorama_fish_eye_96
                else R.drawable.ic_baseline_close_96
            fieldButtons[new_val].setImageResource(symID)
        }

        ticTacToeViewModel.showOnTurn.observe(viewLifecycleOwner) { isOnTurn ->
            if (isOnTurn) {
                waitSymbol.visibility = View.INVISIBLE
                textPlayerAction.setText(R.string.action_your_turn)
                giveUp.visibility = View.VISIBLE
            } else {
                waitSymbol.visibility = View.VISIBLE
                val actionText = getString(R.string.action_wait_text1) +
                        ticTacToeViewModel.opponentName +
                        getString(R.string.action_wait_text2)
                textPlayerAction.text = actionText
                giveUp.visibility = View.INVISIBLE
            }
        }

        ticTacToeViewModel.ending.observe(viewLifecycleOwner) {ending ->
            val msgID = when (ending) {
                GameEnding.WIN -> R.string.ending_win
                GameEnding.LOSE -> R.string.ending_lose
                GameEnding.DRAW -> R.string.ending_draw
                else -> 0
            }
            giveUp.visibility = View.INVISIBLE
            waitSymbol.visibility = View.INVISIBLE
            removeLiveDataObservers()
            Toast.makeText(activity, msgID, Toast.LENGTH_SHORT).show()
            textPlayerAction.setText(msgID)
            ticTacToeViewModel.deleteLoadedGame()
        }

        for ((clickedField, fieldButton) in fieldButtons.withIndex()) {
            fieldButton.setImageDrawable(null)
            fieldButton.setOnClickListener {
                if (ticTacToeViewModel.ending.value != null) {
                    Toast.makeText(activity, R.string.game_is_over, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!ticTacToeViewModel.isOnTurn()) {
                    Toast.makeText(activity, R.string.not_your_turn, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!ticTacToeViewModel.fieldIsEmpty(clickedField)) {
                    Toast.makeText(activity, R.string.field_not_empty, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                ticTacToeViewModel.playerMove(clickedField)
            }
        }

        giveUp.setOnClickListener {
            GiveUpDialogFragment(this).show(parentFragmentManager, "give_up")
        }

        ticTacToeViewModel.runGame.observe(viewLifecycleOwner) { game ->
            textOpName.text = ticTacToeViewModel.opponentName
            // TODO Show profile pictures
        }

        ticTacToeViewModel.loadRunningGame(args.gameID)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onGiveUp() {
        ticTacToeViewModel.giveUp()
    }

    private fun removeLiveDataObservers() {
        ticTacToeViewModel.nextField.removeObservers(viewLifecycleOwner)
        ticTacToeViewModel.runGame.removeObservers(viewLifecycleOwner)
        ticTacToeViewModel.showOnTurn.removeObservers(viewLifecycleOwner)
    }
}