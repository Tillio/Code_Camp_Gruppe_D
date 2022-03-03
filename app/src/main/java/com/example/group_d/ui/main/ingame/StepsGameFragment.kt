package com.example.group_d.ui.main.ingame

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.group_d.COL_GAMES
import com.example.group_d.GAME_DATA
import com.example.group_d.R
import com.example.group_d.data.model.UserDataViewModel
import com.example.group_d.databinding.FragmentStepsGameBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tbruyelle.rxpermissions2.RxPermissions


class StepsGameFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentStepsGameBinding? = null
    private val db = Firebase.firestore
    private val args: MentalArithmeticsFragmentArgs by navArgs()
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    private lateinit var stepsOpponent: TextView
    private lateinit var stepsDone: TextView
    private lateinit var startStepsButton: Button
    private lateinit var stepsGameViewModel: StepsGameViewModel
    private lateinit var stepsTimer: TextView

    private lateinit var viewModel: StepsGameViewModel

    private lateinit var sensorManager: SensorManager

    private lateinit var countdown_timer: CountDownTimer
    var isRunning: Boolean = false
    var time_in_milli_seconds = 0L

    companion object {
        fun newInstance() = MentalArithmeticsFragment()
    }

    private val binding get() = _binding!!

    @SuppressLint("CheckResult")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment

        _binding = FragmentStepsGameBinding.inflate(inflater, container, false)
        val root: View = binding.root

        stepsTimer = root.findViewById(R.id.stepsTimer)

        stepsGameViewModel =
            ViewModelProvider(this)[StepsGameViewModel::class.java]

        stepsDone = root.findViewById(R.id.stepsDone)

        stepsGameViewModel.stepsWinner.observe(viewLifecycleOwner) { stepsWinner ->
            val msgID = "The winner is: " + stepsWinner
            Toast.makeText(activity, msgID, Toast.LENGTH_LONG).show()
            if(stepsWinner == Firebase.auth.currentUser!!.email) {
                stepsDone.text = "WON"
            } else {
                stepsDone.text = "LOST"
            }
            stepsGameViewModel.deleteLoadedGame()
        }

        stepsOpponent = root.findViewById(R.id.stepsTimeOpponent)

        stepsGameViewModel.stepsTimeOpponent.observe(viewLifecycleOwner) { stepsTimeOpponent ->
            this.stepsOpponent.text = "Opponent: " + stepsTimeOpponent
        }

        val game = db.collection(COL_GAMES).document(args.gameID).get().addOnSuccessListener { doc ->
            val gameData = doc.data!!.get("gameData") as ArrayList<String>
            var currentSteps = 0
            var stepsStarted = false
            var stepsTimerBase: Long = 0

            //dursucht die Datenbank nach bestimmten Schlüsselwörtern
            for (i in 1 until (gameData.size)) {
                val dataItem = gameData[i].split("=")
                if ((dataItem[0] == Firebase.auth.currentUser!!.email) && (dataItem[1] == "stepsStarted")) {
                    stepsStarted = true
                }
                if((dataItem[0] == Firebase.auth.currentUser!!.email) && (dataItem[1] == "stepsTimerBase")) {
                    stepsTimerBase = dataItem[2].toLong()
                }
                if((dataItem[0] == Firebase.auth.currentUser!!.email) && (dataItem[1] == "currentSteps")) {
                    currentSteps = dataItem[2].toInt()
                }
            }

            if(stepsStarted) {
                startStepsButton.visibility = View.GONE

                stepsDone.text = currentSteps.toString()

                //TODO:ändern
                //stepsTimer.base = stepsTimerBase
                //stepsTimer.start()
            }

            startStepsButton = root.findViewById(R.id.startStepsButton)

            startStepsButton.setOnClickListener {
                if(startStepsButton.isVisible) {
                    //schreibt in die Datenbank, dass das Spiel gestartet wurde
                    db.collection(COL_GAMES).document(args.gameID).update(
                        GAME_DATA, FieldValue.arrayUnion(
                        Firebase.auth.currentUser!!.email + "=" + "started"))

                        startTimer(15000)

                    //Step Goal und gegangene Schritte (0) in den TextView eintragen
                    stepsDone.text = currentSteps.toString()

                    //Timer auf null setzen und starten

                    //TODO: ändern
                    //stepsTimer.base = SystemClock.elapsedRealtime()
                    //stepsTimer.start()

                    //timer base in die Datenbank eintragen
                    /*db.collection(COL_GAMES).document(args.gameID).update(
                        GAME_DATA, FieldValue.arrayUnion(
                        Firebase.auth.currentUser!!.email + "=" + "stepsTimerBase" + "=" + stepsTimer.base
                        )
                    )*/
                }

                val rxPermissions = RxPermissions(this
                )

                rxPermissions.request(Manifest.permission.ACTIVITY_RECOGNITION)
                    .subscribe { isGranted ->
                        Log.d("TAG", "Is ACTIVITY_RECOGNITION permission granted: $isGranted")
                    }


                val pm: PackageManager = requireContext().getPackageManager()
                if (pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)) {
                    print("amk")
                }

                if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
                    print("amk")
                }

                sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
                var stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                stepCounterSensor.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
                }
                /*{

                    //Nach jedem Schritt wird die Zahl in der Datenbank aktualisiert
                    //TODO: If Timer erhöht
                    if(/*timer erhöht*/true) {
                        db.collection(COL_GAMES).document(args.gameID).update(
                            GAME_DATA, FieldValue.arrayRemove(
                                Firebase.auth.currentUser!!.email + "=" + "currentSteps" + currentSteps
                            )
                        )
                        //TODO: Hier müssen die Schritte durch den Sensor erhöht werden
                        db.collection(COL_GAMES).document(args.gameID).update(
                            GAME_DATA, FieldValue.arrayUnion(
                                Firebase.auth.currentUser!!.email + "=" + "currentSteps" + "=" + currentSteps
                            )
                        )


                        if(currentSteps >= STEPS_TO_DO.toInt()) {
                            stepsTimer.stop()
                            stepsToDo.text = ""
                            stepsDone.text = ""
                            slash.text = "FINISHED"
                            db.collection(COL_GAMES).document(args.gameID).update(
                                GAME_DATA, FieldValue.arrayUnion(
                                Firebase.auth.currentUser!!.email + "=" + "finalTime" + "=" + stepsTimer.text))
                        }
                    }
                }*/
            }
        }

        stepsGameViewModel.loadRunningGame(args.gameID)

        return root
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEvent ?: return
        // Data 1: According to official documentation, the first value of the `SensorEvent` value is the step count
        sensorEvent.values.firstOrNull()?.let {
            stepsDone.text = it.toString()
        }
    }

    private fun startTimer(time_in_milli_seconds: Long) {
        countdown_timer = object : CountDownTimer(time_in_milli_seconds, 1000) {
            override fun onFinish() {
                db.collection(COL_GAMES).document(args.gameID).update(
                    GAME_DATA, FieldValue.arrayUnion(
                        Firebase.auth.currentUser!!.email + "=" + "finalStepsAmount" + "=" + stepsDone.text.toString()))
                //TODO: stoppe Schrittsensor
                //sensorManager.unregisterListener(stepCounterSensor)
            }

            override fun onTick(p0: Long) {
                this@StepsGameFragment.time_in_milli_seconds = p0
                updateTextUI()
            }
        }
        countdown_timer.start()

        isRunning = true
        startStepsButton.visibility = View.INVISIBLE
    }

    private fun updateTextUI() {
        val minute = (time_in_milli_seconds / 1000) / 60
        val seconds = (time_in_milli_seconds / 1000) % 60

        stepsTimer.text = "$minute:$seconds"
    }
}