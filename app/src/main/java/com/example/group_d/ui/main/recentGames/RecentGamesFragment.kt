package com.example.group_d.ui.main.recentGames

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.*
import com.example.group_d.data.model.Game
import com.example.group_d.data.model.UserDataViewModel
import com.example.group_d.databinding.FragmentRecentGamesBinding
import com.example.group_d.ui.main.games.GamesAdapter
import com.example.group_d.ui.main.games.GamesViewModel
import com.example.group_d.ui.main.ingame.MentalArithmeticsFragmentDirections
import com.example.group_d.ui.main.ingame.StepsGameFragmentDirections
import com.example.group_d.ui.main.ingame.TicTacToeFragmentDirections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RecentGamesFragment : Fragment(), RecentGamesAdapter.GameStarter {

    private var _binding: FragmentRecentGamesBinding? = null
    private val viewModel: RecentGamesViewModel by activityViewModels()
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecentGamesBinding.inflate(inflater, container, false)

        viewModel.addUserDataViewModel(userDataViewModel)
        viewModel.startListeningToRecentGames()
        val root: View = binding.root

        //open statistics fragment on button click
        val statsButton: Button = root.findViewById(R.id.statistik_button)
        statsButton.setOnClickListener {
            val action =
                RecentGamesFragmentDirections.actionRecentGamesFragmentToStatistiksFragment()
            findNavController().navigate(action)
        }

        //fill recentGamesRecycler

        val recentGamesRecycler: RecyclerView = binding.recyclerView
        recentGamesRecycler.adapter =
            viewModel.recentGamesLive.value?.let { RecentGamesAdapter(it, this) }
        recentGamesRecycler.layoutManager = LinearLayoutManager(context)

        viewModel.recentGamesLive.observe(viewLifecycleOwner) { newList ->
            val recentGamesAdapter = RecentGamesAdapter(newList, this)
            recentGamesRecycler.adapter = recentGamesAdapter
        }
        return root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun startGame(game: Game) {
        when (game.gameType) {
            GAME_TYPE_TIC_TAC_TOE -> findNavController().navigate(
                TicTacToeFragmentDirections.actionGlobalIngameTicTacToeFragment(game.id, true)
            )
            GAME_TYPE_MENTAL_ARITHMETICS -> findNavController().navigate(
                MentalArithmeticsFragmentDirections.actionGlobalMentalArithmeticsFragment(game.id)
            )
            GAME_TYPE_STEPS_GAME -> findNavController().navigate(
                StepsGameFragmentDirections.actionGlobalStepsGameFragment(game.id)
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopListeningToRecentGames()
    }
}