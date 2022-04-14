package com.example.group_d.ui.main.ingame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.group_d.GAME_DRAW
import com.example.group_d.R
import com.example.group_d.data.model.GameEnding
import com.example.group_d.data.model.TicTacToeModel
import com.example.group_d.data.model.UserDataViewModel
import com.example.group_d.databinding.FragmentTicTacToeBinding
import com.example.group_d.ui.main.recentGames.RecentGamesViewModel

class TicTacToeFragment : Fragment(), GiveUpReceiver {

    private lateinit var ticTacToeViewModel: TicTacToeViewModel
    private val recentGamesViewModel: RecentGamesViewModel by activityViewModels()
    private var _binding: FragmentTicTacToeBinding? = null
    private val args: TicTacToeFragmentArgs by navArgs()
    private lateinit var waitSymbol: ProgressBar
    private lateinit var textPlayerAction: TextView
    private lateinit var fieldButtons: Array<ImageView>
    private lateinit var giveUpButton: Button

    private val userDataViewModel: UserDataViewModel by activityViewModels()

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ticTacToeViewModel =
            ViewModelProvider(this)[TicTacToeViewModel::class.java]

        _binding = FragmentTicTacToeBinding.inflate(inflater, container, false)
        val root = binding.root
        val textOpName = binding.textOpName
        waitSymbol = binding.wait
        textPlayerAction = binding.textPlayerAction
        giveUpButton = binding.buttonGiveUp
        loadFieldButtons(root)

        // Set LiveData observers
        ticTacToeViewModel.nextField.observe(viewLifecycleOwner, this::onNextFieldObserved)
        ticTacToeViewModel.showOnTurn.observe(viewLifecycleOwner, this::onPlayerChanged)
        ticTacToeViewModel.ending.observe(viewLifecycleOwner, this::onGameEnding)

        for ((clickedField, fieldButton) in fieldButtons.withIndex()) {
            // Clear icon
            fieldButton.setImageDrawable(null)
            if (!args.showEndstate) {
                // Set on click listener for every field
                fieldButton.setOnClickListener {
                    onFieldClicked(clickedField)
                }
            }
        }

        if (!args.showEndstate) {
            giveUpButton.setOnClickListener {
                GiveUpDialogFragment(this).show(parentFragmentManager, "give_up")
            }
        }

        ticTacToeViewModel.runGame.observe(viewLifecycleOwner) {
            textOpName.text = ticTacToeViewModel.opponentName
        }

        if (!args.showEndstate) {
            ticTacToeViewModel.loadRunningGame(args.gameID)
        } else {
            ticTacToeViewModel.recentGamesViewModel = recentGamesViewModel
            ticTacToeViewModel.showEndstate(args.gameID)
        }

        return root
    }

    private fun loadFieldButtons(root: View) {
        // Retrieve IDs for field buttons from a typed array
        val fieldIDs = resources.obtainTypedArray(R.array.tic_tac_toe_fields)
        fieldButtons = Array(TicTacToeModel.NUM_FIELDS) { i ->
            val id = fieldIDs.getResourceId(i, -1)
            root.findViewById(id)
        }
        fieldIDs.recycle()
    }

    private fun onFieldClicked(clickedField: Int) {
        if (ticTacToeViewModel.ending.value != null) {
            // Don't let the user move if the game is already over
            Toast.makeText(activity, R.string.game_is_over, Toast.LENGTH_SHORT).show()
            return
        }
        if (!ticTacToeViewModel.isOnTurn()) {
            // Don't let the user move if the user is not on turn
            Toast.makeText(activity, R.string.not_your_turn, Toast.LENGTH_SHORT).show()
            return
        }
        if (!ticTacToeViewModel.fieldIsEmpty(clickedField)) {
            // Don't let the user move is the field is not empty
            Toast.makeText(activity, R.string.field_not_empty, Toast.LENGTH_SHORT).show()
            return
        }
        ticTacToeViewModel.playerMove(clickedField)
        // send Notification to next player
        userDataViewModel.prepNotification(
            getString(R.string.notify_your_turn_title),
            getString(R.string.notify_your_turn_tic_tac_toe_msg, userDataViewModel.getOwnDisplayName()),
            ticTacToeViewModel.otherID
        )
    }

    private fun onNextFieldObserved(nextFieldID: Int) {
        val symID = if (ticTacToeViewModel.isOnTurn()) {
            // User is on turn -> draw a circle
            R.drawable.ic_baseline_panorama_fish_eye_96
        } else {
            // Opponent is on turn -> draw a X
            R.drawable.ic_baseline_close_96
        }
        fieldButtons[nextFieldID].setImageResource(symID)
    }

    private fun onPlayerChanged(isOnTurn: Boolean) {
        if (isOnTurn) {
            waitSymbol.visibility = View.INVISIBLE
            textPlayerAction.setText(R.string.action_your_turn)
            giveUpButton.visibility = View.VISIBLE
        } else {
            waitSymbol.visibility = View.VISIBLE
            textPlayerAction.text =
                getString(R.string.action_wait_text, ticTacToeViewModel.opponentName)
            giveUpButton.visibility = View.INVISIBLE
        }
    }

    private fun onGameEnding(ending: GameEnding) {
        // send notification, that the game is over
        userDataViewModel.prepNotification(
            getString(R.string.notify_game_ended_title),
            getString(
                R.string.notify_game_ended_msg,
                getString(R.string.title_tic_tac_toe),
                userDataViewModel.getOwnDisplayName()
            ),
            ticTacToeViewModel.otherID
        )
        // Get right message
        val msgID = when (ending) {
            GameEnding.WIN -> R.string.ending_win
            GameEnding.LOSE -> R.string.ending_lose
            GameEnding.DRAW -> R.string.ending_draw
        }
        setWinner(ending)
        giveUpButton.visibility = View.INVISIBLE
        waitSymbol.visibility = View.INVISIBLE
        removeLiveDataObservers()
        // Show user the message
        Toast.makeText(activity, msgID, Toast.LENGTH_SHORT).show()
        textPlayerAction.setText(msgID)
        if (!args.showEndstate) {
            ticTacToeViewModel.deleteLoadedGame()
        }
    }

    private fun setWinner(ending: GameEnding) {
        val thisGame = ticTacToeViewModel.runGameRaw
        val ownId = userDataViewModel.getOwnUserID()
        var opponentId = ""
        for (player in thisGame.players) {
            if (ownId != player.id) {
                opponentId = player.id
                break
            }
        }
        thisGame.winner = when (ending) {
            GameEnding.WIN -> ownId
            GameEnding.LOSE -> opponentId
            else -> GAME_DRAW
        }
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