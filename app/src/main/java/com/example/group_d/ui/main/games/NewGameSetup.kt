package com.example.group_d.ui.main.games

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.GAME_TYPE_TIC_TAC_TOE
import com.example.group_d.R
import com.example.group_d.data.model.Challenge
import com.example.group_d.data.model.User
import com.example.group_d.data.model.UserDataViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class NewGameSetup : Fragment() {

    private val args: NewGameSetupArgs by navArgs()
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    companion object {
        fun newInstance() = NewGameSetup()
    }

    private lateinit var viewModel: NewGameSetupViewModel

    // when the NewGameSetupFragment is opened
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_new_game_setup, container, false)

        // get text and recycler views
        val selectedGameText: TextView = view.findViewById(R.id.selectedGameText)
        val playerItems: RecyclerView = view.findViewById(R.id.player_list)
        // get the playeradapter
        val adapter = PlayerAdapter()
        adapter.playerItems = ArrayList(createPlayers())
        playerItems.adapter = adapter
        playerItems.layoutManager = LinearLayoutManager(context)

        // get buttons
        val buttonCancel: Button = view.findViewById(R.id.buttonCancel)
        val buttonStart: Button = view.findViewById(R.id.buttonStart)
        // when cancel-button is pressed, close the NewGamesFragment
        buttonCancel.setOnClickListener { view -> view.findNavController().navigate(R.id.navigation_friends) }
        // when the StartButton is pressed
        buttonStart.setOnClickListener {
            // check the selected game and send the corresponding challenge
            if (selectedGameText.text.toString() == "TicTacToe") {
                userDataViewModel.challengeFriend(args.userID, Challenge(User(name = Firebase.auth.currentUser!!.email.toString(), id = userDataViewModel.getOwnUserID(), online = true), GAME_TYPE_TIC_TAC_TOE))
            }
            // then close the NewGamesFragment
            findNavController().navigate(R.id.action_global_friendList)
        }

        // get the spinner
        val spinnerGameSelect: Spinner = view.findViewById(R.id.game_select)
        // set up spinner
        spinnerGameSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // if an option is selected, the corresponding gametype is displayed
                val strSelected: String = parent?.getItemAtPosition(position).toString()
                selectedGameText.text = strSelected
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NewGameSetupViewModel::class.java)
        // TODO: Use the ViewModel
    }

    // create the PlayerList (you and the player you challenge)
    fun createPlayers(): List<User> {
        val arrayList = ArrayList<User>()
        arrayList.add(User( name = "you", id = userDataViewModel.getOwnUserID(), online = true))
        arrayList.add(User( name = args.userName, id = args.userID, online = args.userStatus))
        return arrayList
    }

}