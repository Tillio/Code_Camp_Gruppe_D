package com.example.group_d.ui.main.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.*
import com.example.group_d.data.model.Challenge
import com.example.group_d.data.model.User
import com.example.group_d.data.model.UserDataViewModel

class NewGameSetup : Fragment() {

    private val args: NewGameSetupArgs by navArgs()
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private lateinit var spinnerGameSelect: Spinner
    private lateinit var stepGameTimeSelect: Spinner
    private lateinit var playerItems: RecyclerView

    companion object {
        fun newInstance() = NewGameSetup()
    }

    private lateinit var viewModel: NewGameSetupViewModel

    // when the NewGameSetupFragment is opened
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_game_setup, container, false)
        // get Spinners and RecyclerView
        spinnerGameSelect = view.findViewById(R.id.game_select)
        stepGameTimeSelect = view.findViewById(R.id.stepGameLengthSpinner)
        playerItems = view.findViewById(R.id.player_list)
        // get the playeradapter
        val adapter = PlayerAdapter()
        adapter.playerItems = ArrayList(createPlayers())
        playerItems.adapter = adapter
        playerItems.layoutManager = LinearLayoutManager(context)

        // get buttons
        val buttonCancel: Button = view.findViewById(R.id.buttonCancel)
        val buttonStart: Button = view.findViewById(R.id.buttonStart)
        // when cancel-button is pressed, close the NewGamesFragment
        buttonCancel.setOnClickListener { view ->
            view.findNavController().navigate(R.id.navigation_friends)
        }
        // when the StartButton is pressed
        buttonStart.setOnClickListener {
            startButtonOnClickListener(it)
        }

        // set up spinner
        spinnerGameSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // if an option is selected, the corresponding gametype is displayed
                val strSelected: String = parent?.getItemAtPosition(position).toString()
                //The Step game has an extra time option in order to select the game duration
                if (strSelected == GAME_TYPE_STEPS_GAME) {
                    stepGameTimeSelect.visibility = VISIBLE
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        return view
    }

    /**
     * Uses the data given by the user and packages it in order to store it in Firestore
     */
    private fun startButtonOnClickListener(view: View) {
        val gameType = when (spinnerGameSelect.selectedItem.toString()) {
            "TicTacToe" -> GAME_TYPE_TIC_TAC_TOE
            "Compass" -> GAME_TYPE_COMPASS
            "Mental Arithmetics" -> GAME_TYPE_MENTAL_ARITHMETICS
            "Steps Game" -> GAME_TYPE_STEPS_GAME
            else -> {
                GAME_TYPE_TIC_TAC_TOE
            }
        }

        val mainUser = User(
            name = userDataViewModel.getOwnEmail(),
            id = userDataViewModel.getOwnUserID(),
            status = true,
            displayName = userDataViewModel.getOwnDisplayName()
        )

        val challenge = Challenge(mainUser, gameType)

        if (gameType == GAME_TYPE_STEPS_GAME){
            challenge.stepGameTime =
                (stepGameTimeSelect.selectedItem.toString().toLong()) * 60000
        }
        userDataViewModel.challengeFriend(args.userID, challenge)

        // then close the NewGamesFragment
        findNavController().navigate(R.id.action_global_friendList)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NewGameSetupViewModel::class.java)
    }

    // create the PlayerList (you and the player you challenge)
    private fun createPlayers(): List<User> {
        val arrayList = ArrayList<User>()
        // set data for yourself
        arrayList.add(
            User(
                name = userDataViewModel.getOwnEmail(),
                id = userDataViewModel.getOwnUserID(),
                status = true,
                displayName = "you"
            )
        )
        // set data for the other player
        arrayList.add(
            User(
                name = args.userName,
                id = args.userID,
                status = args.userStatus,
                displayName = args.userDisplayName
            )
        )
        return arrayList
    }

}