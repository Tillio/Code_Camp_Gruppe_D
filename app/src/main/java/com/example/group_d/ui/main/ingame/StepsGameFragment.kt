package com.example.group_d.ui.main.ingame

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.group_d.COL_GAMES
import com.example.group_d.GAME_DATA
import com.example.group_d.GAME_DRAW
import com.example.group_d.R
import com.example.group_d.data.model.GameEnding
import com.example.group_d.data.model.UserDataViewModel
import com.example.group_d.databinding.FragmentStepsGameBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tbruyelle.rxpermissions2.RxPermissions


class StepsGameFragment : Fragment() {

    private var _binding: FragmentStepsGameBinding? = null
    private val db = Firebase.firestore
    private val args: StepsGameFragmentArgs by navArgs()
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    private lateinit var stepsOpponent: TextView
    private lateinit var stepsDone: TextView
    private lateinit var startStepsButton: Button
    private lateinit var stepsGameViewModel: StepsGameViewModel
    private lateinit var stepsTimer: TextView
    private lateinit var wonLost: TextView

    private var stepsBase: Int = 0

    private lateinit var viewModel: StepsGameViewModel

    private lateinit var sensorManager: SensorManager

    private lateinit var countdown_timer: CountDownTimer
    var isRunning: Boolean = false
    var time_in_milli_seconds = 0L

    companion object {
        fun newInstance() = MentalArithmeticsFragment()
    }

    private val binding get() = _binding!!

    var currentSteps = 0

    @SuppressLint("CheckResult")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        stepsGameViewModel =
            ViewModelProvider(this)[StepsGameViewModel::class.java]

        stepsGameViewModel.sensorManager =
            requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val rxPermissions = RxPermissions(this)

        rxPermissions.request(Manifest.permission.ACTIVITY_RECOGNITION)
            .subscribe { isGranted ->
                Log.d("TAG", "Is ACTIVITY_RECOGNITION permission granted: $isGranted")
            }

        // Inflate the layout for this fragment

        _binding = FragmentStepsGameBinding.inflate(inflater, container, false)
        val root: View = binding.root

        startStepsButton = root.findViewById(R.id.startStepsButton)

        wonLost = root.findViewById((R.id.wonLost))

        stepsTimer = root.findViewById(R.id.stepsTimer)

        stepsGameViewModel.gameID = args.gameID

        stepsDone = root.findViewById(R.id.stepsDone)

        stepsGameViewModel.stepsWinner.observe(viewLifecycleOwner) { stepsWinner ->
            val msgID = "The winner is: " + stepsWinner
            Toast.makeText(activity, msgID, Toast.LENGTH_LONG).show()
            // send Notification
            userDataViewModel.prepNotification(
                getString(R.string.notify_game_ended_title),
                getString(
                    R.string.notify_game_ended_msg,
                    "steps-game",
                    userDataViewModel.getOwnDisplayName()
                ),
                stepsGameViewModel.otherID
            )
            var ending: GameEnding
            if (stepsWinner == Firebase.auth.currentUser!!.email) {
                wonLost.text = "WON"
                ending = GameEnding.WIN
            } else {
                wonLost.text = "LOST"
                ending = GameEnding.LOSE
            }
            setWinner(ending)
            stepsGameViewModel.deleteLoadedGame()
        }

        stepsOpponent = root.findViewById(R.id.stepsTimeOpponent)

        //checks if there already is a final steps amount of the enemy an if so shows it to the player
        stepsGameViewModel.opponentSteps.observe(viewLifecycleOwner) { opponentSteps ->
            this.stepsOpponent.text = opponentSteps
        }

        stepsGameViewModel.actualSteps.observe(viewLifecycleOwner) { actualSteps ->
            stepsDone.text = actualSteps.toString()
            currentSteps = actualSteps
        }

        val game =
            db.collection(COL_GAMES).document(args.gameID).get().addOnSuccessListener { doc ->
                val gameData = doc.data!!.get(GAME_DATA) as ArrayList<String>
                var stepsStarted = false
                var finished = false
                var remainingTime: Long = 0
                var gameTime = 0L
                //checks the database for specific keywords and stores its values in local variables
                for (i in 0 until (gameData.size)) {
                    val dataItem = gameData[i].split("=")
                    if ((dataItem[0] == Firebase.auth.currentUser!!.email) && (dataItem[1] == "stepsStarted")) {
                        stepsStarted = true
                    }
                    if ((dataItem[0] == Firebase.auth.currentUser!!.email) && (dataItem[1] == "remainingTime")) {
                        remainingTime = dataItem[2].toLong()
                    }
                    if ((dataItem[0] == Firebase.auth.currentUser!!.email) && (dataItem[1] == "currentSteps")) {
                        currentSteps = dataItem[2].toInt()
                    }
                    if ((dataItem[0] == Firebase.auth.currentUser!!.email) && (dataItem[1] == "stepsBase")) {
                        stepsBase = dataItem[2].toInt()
                    }
                    if ((dataItem[0] == Firebase.auth.currentUser!!.email) && dataItem[1] == "finalStepsAmount") {
                        finished = true
                    }
                    if ((dataItem[0] == Firebase.auth.currentUser!!.email) && dataItem[1] == "gameTime") {
                        gameTime = dataItem[2].toLong()
                    }
                }

                //if the game started the button becomes invisible
                if (stepsStarted) {
                    startStepsButton.visibility = View.GONE

                    //if the player finished the final steps amount is shown or if not the current steps
                    if (finished) {
                        stepsDone.text = "FINISHED:\n" + currentSteps.toString()
                    } else {
                        stepsDone.text = currentSteps.toString()
                    }

                    startTimer(remainingTime)
                }

                startStepsButton.setOnClickListener {
                    if (startStepsButton.isVisible) {
                        //pushed to the database that the game started
                        db.collection(COL_GAMES).document(args.gameID).update(
                            GAME_DATA, FieldValue.arrayUnion(
                                Firebase.auth.currentUser!!.email + "=" + "stepsStarted"
                            )
                        )

                        startTimer(gameTime)

                        //Step Goal and walked steps (0) is put in the textview
                        stepsDone.text = currentSteps.toString()
                    }

                    stepsGameViewModel.startStepCounter()
                }
            }

        stepsGameViewModel.loadRunningGame(args.gameID)

        return root
    }

    private fun setWinner(ending: GameEnding) {
        val thisGame = stepsGameViewModel.runGameRaw
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

    private fun startTimer(time_in_milli_seconds: Long) {
        if (time_in_milli_seconds > 1000L) {
            countdown_timer = object : CountDownTimer(time_in_milli_seconds, 1000) {
                override fun onFinish() {
                    //stops sensor
                    stepsGameViewModel.stopStepCounter()

                    // send Notification
                    userDataViewModel.prepNotification(
                        getString(R.string.notify_your_turn_title),
                        getString(R.string.notify_your_turn_steps_game_msg, userDataViewModel.getOwnDisplayName()),
                        stepsGameViewModel.otherID
                    )

                    db.collection(COL_GAMES).document(args.gameID).update(
                        GAME_DATA, FieldValue.arrayUnion(
                            Firebase.auth.currentUser!!.email + "=" + "finalStepsAmount" + "=" + stepsGameViewModel.actualSteps.value
                        )
                    )

                    stepsDone.text = "FINISHED:\n" + stepsGameViewModel.actualSteps.value.toString()
                }

                override fun onTick(remainingTime: Long) {
                    //updates the remaining time in the database
                    db.collection(COL_GAMES).document(args.gameID).update(
                        GAME_DATA, FieldValue.arrayRemove(
                            Firebase.auth.currentUser!!.email + "=" + "remainingTime" + "=" + this@StepsGameFragment.time_in_milli_seconds
                        )
                    )

                    this@StepsGameFragment.time_in_milli_seconds = remainingTime

                    db.collection(COL_GAMES).document(args.gameID).update(
                        GAME_DATA, FieldValue.arrayUnion(
                            Firebase.auth.currentUser!!.email + "=" + "remainingTime" + "=" + remainingTime
                        )
                    )
                    //updates the timer the player can see
                    updateTextUI()
                }
            }
            countdown_timer.start()

            isRunning = true
            startStepsButton.visibility = View.INVISIBLE
        }
    }

    private fun updateTextUI() {
        val minute = (time_in_milli_seconds / 1000) / 60
        val seconds = (time_in_milli_seconds / 1000) % 60

        stepsTimer.text = "$minute:$seconds"
    }
}