package com.example.group_d.ui.main.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.GAME_TYPE_COMPASS
import com.example.group_d.GAME_TYPE_MENTAL_ARITHMETICS
import com.example.group_d.GAME_TYPE_STEPS_GAME
import com.example.group_d.GAME_TYPE_TIC_TAC_TOE
import com.example.group_d.data.model.Game
import com.example.group_d.data.model.UserDataViewModel
import com.example.group_d.databinding.FragmentGamesBinding
import com.example.group_d.ui.main.ingame.CompassFragmentDirections
import com.example.group_d.ui.main.ingame.MentalArithmeticsFragmentDirections
import com.example.group_d.ui.main.ingame.StepsGameFragmentDirections
import com.example.group_d.ui.main.ingame.TicTacToeFragmentDirections

class GamesFragment : Fragment(), GamesAdapter.GameStarter{

    private lateinit var gamesViewModel: GamesViewModel
    private var _binding: FragmentGamesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        gamesViewModel =
            ViewModelProvider(this).get(GamesViewModel::class.java)

        _binding = FragmentGamesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView: RecyclerView = binding.recyclerViewGames
        recyclerView.adapter = userDataViewModel.games.value?.let { GamesAdapter(it, this) }
        recyclerView.layoutManager = LinearLayoutManager(context)
        userDataViewModel.games.observe(viewLifecycleOwner) { newGames ->
            val gamesAdapter = GamesAdapter(newGames, this)
            recyclerView.adapter = gamesAdapter
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun startGame(game: Game) {
        if (game.gameType == GAME_TYPE_TIC_TAC_TOE) {
            val action =
                TicTacToeFragmentDirections.actionGlobalIngameTicTacToeFragment(game.id)
            findNavController().navigate(action)
        } else if (game.gameType == GAME_TYPE_COMPASS) {
            val action =
                CompassFragmentDirections.actionGlobalCompassFragment(game.id)
            findNavController().navigate(action)
        } else if(game.gameType == GAME_TYPE_MENTAL_ARITHMETICS) {
            findNavController().navigate(
                MentalArithmeticsFragmentDirections.actionGlobalMentalArithmeticsFragment(game.id)
            )
        } else if(game.gameType == GAME_TYPE_STEPS_GAME) {
            findNavController().navigate(
                StepsGameFragmentDirections.actionGlobalStepsGameFragment(game.id)
            )
        }
    }
}