package com.example.group_d.ui.main.recentGames

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.COL_GAMES
import com.example.group_d.GAME_PLAYERS
import com.example.group_d.R
import com.example.group_d.data.model.Game
import com.example.group_d.databinding.RecentGamesFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RecentGamesFragment : Fragment(), RecentGamesAdapter.GameStarter {

    private lateinit var viewModel: RecentGamesViewModel
    private var _binding: RecentGamesFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = RecentGamesFragmentBinding.inflate(inflater, container, false)

        val root: View = binding.root

        //open statistics fragment on button click
        val statsButton:Button = root.findViewById(R.id.statistik_button)
        statsButton.setOnClickListener { view ->
            val action =
                RecentGamesFragmentDirections.actionRecentGamesFragmentToStatistiksFragment()
            findNavController().navigate(action)
        }

        //fill recentGamesRecycler

        val recentGamesRecycler: RecyclerView = binding.recyclerView
        recentGamesRecycler.layoutManager = LinearLayoutManager(context)



        return root
    }

    fun loadFirsXRecentGames(gamesToLoad: Int){
        val db = Firebase.firestore
        db.collection(COL_GAMES).whereArrayContains(GAME_PLAYERS, FirebaseAuth.getInstance().uid.toString())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RecentGamesViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun startGame(game: Game) {
        TODO("Not yet implemented")
    }

}