package com.example.group_d.ui.main.ui.games

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.data.model.User

class NewGameSetup : Fragment() {

    //private var layoutManager: RecyclerView.LayoutManager? = null
    //private var adapter: RecyclerView.Adapter<PlayerAdapter.ViewHolder>? = null

    companion object {
        fun newInstance() = NewGameSetup()
    }

    private lateinit var viewModel: NewGameSetupViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_new_game_setup, container, false)

        val playerItems: RecyclerView = view.findViewById(R.id.player_list)
        val adapter = PlayerAdapter()
        adapter.playerItems = ArrayList(createPlayers())
        playerItems.adapter = adapter
        playerItems.layoutManager = LinearLayoutManager(context)

        val buttonCancel: Button = view.findViewById(R.id.buttonCancel)
        val buttonStart: Button = view.findViewById(R.id.buttonStart)
        buttonCancel.setOnClickListener { view -> view.findNavController().navigate(R.id.navigation_friends) }
        buttonStart.setOnClickListener { """ToDo: create and open new game""" }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NewGameSetupViewModel::class.java)
        // TODO: Use the ViewModel
    }

    fun createPlayers(): List<User> {
        val arrayList = ArrayList<User>()
        arrayList.add(User( name = "you", id = "42", online = true))
        arrayList.add(User( name = "Opponent", id = "13", online = true))
        return arrayList
    }

}