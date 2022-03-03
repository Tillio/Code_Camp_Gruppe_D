package com.example.group_d.ui.main.challenges

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.GAME_TYPE_MENTAL_ARITHMETICS
import com.example.group_d.GAME_TYPE_STEPS_GAME
import com.example.group_d.GAME_TYPE_TIC_TAC_TOE
import com.example.group_d.R
import com.example.group_d.data.model.Challenge
import com.example.group_d.data.model.UserDataViewModel
import com.example.group_d.databinding.FragmentChallengesBinding
import com.example.group_d.ui.main.games.GamesFragmentDirections
import com.example.group_d.ui.main.ingame.MentalArithmeticsFragmentDirections
import com.example.group_d.ui.main.ingame.StepsGameFragmentDirections
import com.example.group_d.ui.main.ingame.TicTacToeFragmentDirections

class ChallengesFragment : Fragment() {

    private lateinit var challengesViewModel: ChallengesViewModel
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private var _binding: FragmentChallengesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        challengesViewModel =
            ViewModelProvider(this).get(ChallengesViewModel::class.java)

        _binding = FragmentChallengesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView: RecyclerView = binding.recyclerViewChallenges
        userDataViewModel.challenges.observe(viewLifecycleOwner) {
            recyclerView.adapter = ChallengeAdapter(it, this)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun onAccept(challenge: Challenge) {
        Log.d(null, "Start new game with ${challenge.user.name}")
        challengesViewModel.createGame(challenge).addOnSuccessListener { docref ->
            if(challenge.gameType == GAME_TYPE_TIC_TAC_TOE) {
                findNavController().navigate(
                    TicTacToeFragmentDirections.actionGlobalIngameTicTacToeFragment(docref.id))
            } else if (challenge.gameType == GAME_TYPE_MENTAL_ARITHMETICS) {
                findNavController().navigate(
                    MentalArithmeticsFragmentDirections.actionGlobalMentalArithmeticsFragment(docref.id))
            } else if (challenge.gameType == GAME_TYPE_STEPS_GAME) {
                findNavController().navigate(
                    StepsGameFragmentDirections.actionGlobalStepsGameFragment(docref.id)
                )
            }

        }
    }

    fun onDecline(challenge: Challenge, dontAsk: Boolean) {
        challengesViewModel.decline(challenge)
        if (dontAsk) {
            // TODO Update Settings
            Log.d(challenge.user.name, "Don't show the decline dialog again!")
        }
    }
}

class ChallengeDeclineDialogFragment(
    private val challenge: Challenge,
    private val fragment: ChallengesFragment
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            var dontAsk = false
            val builder = AlertDialog.Builder(it)
            val msg = getString(R.string.dialog_decline_challenge_msg1) +
                    challenge.user.name +
                    getString(R.string.dialog_decline_challenge_msg2)
            builder.setTitle(msg)
                .setPositiveButton(R.string.dialog_yes) { _, _ ->
                    fragment.onDecline(challenge, dontAsk)
                }
                .setNegativeButton(R.string.dialog_no) { dialog, _ ->
                    dialog.cancel()
                }
                .setMultiChoiceItems(
                    arrayOf(getString(R.string.dialog_decline_challenge_dont_ask)),
                    null
                ) { _, _, checked ->
                    dontAsk = checked
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}