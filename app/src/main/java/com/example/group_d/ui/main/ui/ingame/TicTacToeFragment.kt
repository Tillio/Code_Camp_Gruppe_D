package com.example.group_d.ui.main.ui.ingame

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.group_d.R
import com.example.group_d.data.model.GameEnding
import com.example.group_d.data.model.UserDataViewModel
import com.example.group_d.data.model.tictactoe.TicTacToeGame
import com.example.group_d.databinding.TicTacToeFragmentBinding

class TicTacToeFragment : Fragment() {

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
            fieldButtons = Array(TicTacToeGame.NUM_FIELDS) { i ->
                val id = fieldIDs.getResourceId(i, -1)
                root.findViewById(id)
            }
            fieldIDs.recycle()
        }

        var currPlayerIcon = R.drawable.ic_baseline_panorama_fish_eye_96
        ticTacToeViewModel.nextField.observe(viewLifecycleOwner) {new_val ->
            fieldButtons[new_val].setImageResource(currPlayerIcon)
        }

        ticTacToeViewModel.isOnTurn.observe(viewLifecycleOwner) {new_val ->
            if (new_val) {
                waitSymbol.visibility = View.INVISIBLE
                textPlayerAction.setText(R.string.action_your_turn)
                currPlayerIcon = R.drawable.ic_baseline_panorama_fish_eye_96
            } else {
                waitSymbol.visibility = View.VISIBLE
                val actionText = getString(R.string.action_wait_text1) +
                        ticTacToeViewModel.opponentName +
                        getString(R.string.action_wait_text2)
                textPlayerAction.text = actionText
                currPlayerIcon = R.drawable.ic_baseline_close_96
            }
        }

        ticTacToeViewModel.ending.observe(viewLifecycleOwner) {ending ->
            when (ending) {
                GameEnding.WIN -> {
                    // TODO Show winScreen
                    Toast.makeText(activity, "Congrats! You win", Toast.LENGTH_SHORT).show()
                    textPlayerAction.text = ""
                }
                GameEnding.LOSE -> {
                    Toast.makeText(activity, "Booh! You lose", Toast.LENGTH_SHORT).show()
                    textPlayerAction.text = ""
                }
                GameEnding.DRAW -> {
                    Toast.makeText(activity, "It's a draw", Toast.LENGTH_SHORT).show()
                    textPlayerAction.text = ""
                }
                else -> {}
            }
        }

        for ((clickedField, fieldButton) in fieldButtons.withIndex()) {
            fieldButton.setImageDrawable(null)
            fieldButton.setOnClickListener {
                if (!ticTacToeViewModel.isOnTurn()) {
                    Toast.makeText(activity, R.string.not_your_turn, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!ticTacToeViewModel.fieldIsEmpty(clickedField)) {
                    Toast.makeText(activity, R.string.field_not_empty, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                ticTacToeViewModel.move(clickedField)
            }
        }

        giveUp.setOnClickListener {
            GiveUpDialogFragment(this).show(parentFragmentManager, "give_up")
        }

        ticTacToeViewModel.loadGame(args.gameID)
        textOpName.text = ticTacToeViewModel.opponentName
        // TODO Show profile pictures

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun onGiveUp() {
        ticTacToeViewModel.giveUp()
    }
}

class GiveUpDialogFragment(
    private val fragment: TicTacToeFragment
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.dialog_give_up_msg)
                .setPositiveButton(R.string.dialog_yes) {_, _ ->
                    fragment.onGiveUp()
                }
                .setNegativeButton(R.string.dialog_no) {dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        }
    }
}