package com.example.group_d.ui.main.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.GAME_TYPE_TIC_TAC_TOE
import com.example.group_d.data.model.*
import com.example.group_d.databinding.FragmentGamesBinding
import com.google.firebase.firestore.DocumentReference

class GamesFragment : Fragment() {

    private lateinit var gamesViewModel: GamesViewModel
    private var _binding: FragmentGamesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private  val userDataViewModel: UserDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        gamesViewModel =
            ViewModelProvider(this).get(GamesViewModel::class.java)

        _binding = FragmentGamesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textGames
        /*gamesViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = it
        })*/
        val recyclerView: RecyclerView = binding.recyclerViewGames
        recyclerView.adapter = GamesAdapter(userDataViewModel.games.value)
        recyclerView.layoutManager = LinearLayoutManager(context)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun exampleGames(): MutableList<Game> {
        val games: MutableList<Game> = ArrayList()
        val gameData: MutableList<Long> = ArrayList()
        val players: ArrayList<DocumentReference> = ArrayList()
        for (friends in userDataViewModel.friends){
            Game(beginner = 1, gameData = gameData, gameType = GAME_TYPE_TIC_TAC_TOE,players)        }
        return games
    }
}