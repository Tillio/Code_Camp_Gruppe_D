package com.example.group_d.ui.main.ingame

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.group_d.GAME_DRAW
import com.example.group_d.LOCATIONS_GET_QUERY
import com.example.group_d.R
import com.example.group_d.REQUEST_UPDATE_LOCATION_SETTINGS
import com.example.group_d.data.json.CompassLocationListDeserializer
import com.example.group_d.data.json.RetrofitInstanceBuilder
import com.example.group_d.data.model.CompassLocation
import com.example.group_d.data.model.Game
import com.example.group_d.data.model.GameEnding
import com.example.group_d.data.model.UserDataViewModel
import com.example.group_d.databinding.CompassFragmentBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import java.util.*
import kotlin.math.abs


class CompassFragment : Fragment(), Callback<MutableList<CompassLocation>>, GiveUpReceiver, SensorEventListener {
    private val retrofit: Retrofit = RetrofitInstanceBuilder.getRetrofitInstance(
        object : TypeToken<MutableList<CompassLocation>>() {}.type,
        CompassLocationListDeserializer()
    )
    private val restCall = retrofit.create(GeojsonRestService::class.java).loadGeoJson()

    interface GeojsonRestService {
        @GET(LOCATIONS_GET_QUERY)
        fun loadGeoJson(): Call<MutableList<CompassLocation>>
    }

    private val userDataViewModel: UserDataViewModel by activityViewModels()

    private lateinit var compassViewModel: CompassViewModel
    private var _binding: CompassFragmentBinding? = null
    private val args: CompassFragmentArgs by navArgs()
    private var showEndstate: Boolean = false

    private lateinit var textOpName: TextView
    private lateinit var textPlayerAction: TextView
    private lateinit var waitSymbol: ProgressBar
    private lateinit var timeCount: Chronometer
    private lateinit var compassView: View
    private lateinit var compassNeedle: ImageView
    private lateinit var giveUpButton: Button

    private var sensorManager: SensorManager? = null
    private var rotVecSensor: Sensor? = null
    private lateinit var waitTimer: Timer

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastUserPosition: Location? = null
    private var lastOrientation: Float = 0.0F
    /*
        With this flag we ensure that the user doesn't get stuck in a loop of requests
        for location settings change because onResume is also called
        when the user denies the settings change and the error dialog is shown
     */
    private var showLocationSettingsRequest: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        compassViewModel = ViewModelProvider(this)[CompassViewModel::class.java]
        showEndstate = args.showEndstate

        _binding = CompassFragmentBinding.inflate(inflater, container, false)
        val root = binding.root
        textOpName = binding.textOpName
        textPlayerAction = binding.textPlayerAction
        waitSymbol = binding.wait
        timeCount = binding.compassTimer
        compassNeedle = binding.compassNeedle
        compassView = binding.compassView
        giveUpButton = binding.buttonGiveUp

        compassViewModel.opponentName.observe(viewLifecycleOwner) { opName ->
            // Show the opponent's name in the text field
            textOpName.text = opName
        }

        compassViewModel.currentLocation.observe(viewLifecycleOwner) { curLocation ->
            // Show the current location
            textPlayerAction.text = "${curLocation.name}, ${curLocation.addr}"
        }

        compassViewModel.foundAllLocations.observe(viewLifecycleOwner, this::onUserReady)

        compassViewModel.ending.observe(viewLifecycleOwner) { ending ->
            onGameOver(ending)
        }

        giveUpButton.setOnClickListener {
            GiveUpDialogFragment(this).show(parentFragmentManager, "give_up")
        }

        waitTimer = Timer()

        if (!showEndstate) {
            // Make sure the location permission is granted if we don't show the endstate
            requireLocationPermission()
        } else {
            locationAvailable()
        }

        return root
    }

    // Retrofit callbacks
    /*
        Called when the rest call was successful
     */
    override fun onResponse(
        call: Call<MutableList<CompassLocation>>,
        response: Response<MutableList<CompassLocation>>
    ) {
        // Register callback for touching the compass (location confirm)
        compassView.setOnClickListener(this::onLocationConfirmed)
        compassViewModel.locations = response.body()?:ArrayList()
        compassViewModel.runGame.observe(viewLifecycleOwner, this::onGameLoaded)
        textPlayerAction.text = ""
        waitSymbol.visibility = View.INVISIBLE

        // Start loading game from database
        compassViewModel.loadRunningGame(args.gameID)
    }

    /*
        Called when the rest call failed
     */
    override fun onFailure(call: Call<MutableList<CompassLocation>>, t: Throwable) {
        ErrorDialogFragment(
            R.string.compass_no_internet_dialog_title, R.string.compass_no_internet_dialog_msg
        ).show(parentFragmentManager, "no_internet")
    }

    private fun requireLocationPermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                requireSensor()
            } else {
                locationNotAvailable()
            }
        }
        // Check if location permission is already granted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            // Then require the sensor
            requireSensor()
        } else {
            // If not inform the user
            LocationRequestDialogFragment(requestPermissionLauncher)
                .show(parentFragmentManager, "location_request")
            return
        }
    }
    
    private fun requireSensor() {
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        if (sensorManager == null) {
            sensorNotAvailable()
            return
        }
        rotVecSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotVecSensor == null) {
            sensorNotAvailable()
            return
        }
        showLocationSettingsRequest = true
        requireLocationSettings()
    }

    private fun sensorNotAvailable() {
        ErrorDialogFragment(
            R.string.compass_no_sensor_dialog_title,
            R.string.compass_no_sensor_dialog_msg
        ).show(parentFragmentManager, "no_sensor")
    }

    private fun requireLocationSettings() {
        if (!showLocationSettingsRequest) {
            return
        }
        showLocationSettingsRequest = false
        // Only needed to form the LocationSettingsRequest to prompt the user
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
        LocationServices.getSettingsClient(requireActivity()).checkLocationSettings(builder.build())
            .addOnSuccessListener {
                locationAvailable()
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        e.startResolutionForResult(this, REQUEST_UPDATE_LOCATION_SETTINGS)
                    } catch (sendIntEx: IntentSender.SendIntentException) {
                        // Ignore the exception
                    }
                }
            }
    }

    /*
        In this game this method is only called when there is a result from the request for change
        of location settings
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_UPDATE_LOCATION_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                // User activated location
                locationAvailable()
            } else {
                // User didn't activate location
                locationNotAvailable()
            }
        }
    }

    private fun locationAvailable() {
        showLocationSettingsRequest = true
        if (!restCall.isExecuted) {
            // Show the user there is a loading process
            waitSymbol.visibility = View.VISIBLE
            textPlayerAction.setText(R.string.compass_loading_locations)
            // Start the call to the API with this fragment object as callback
            restCall.enqueue(this)
        }
    }

    private fun locationNotAvailable() {
        ErrorDialogFragment(
            R.string.compass_location_denied_dialog_title,
            R.string.compass_location_denied_dialog_msg
        ).show(parentFragmentManager, "location_deniedshow")
    }

    private fun onGameLoaded(game: Game?) {
        val currentTime = System.currentTimeMillis()
        // Load start time from game data
        var startTime = compassViewModel.startTime
        if (startTime == 0L) {
            // startTime == 0 -> There isn't a start time saved
            startTime = currentTime
            compassViewModel.saveStartTime(startTime)
        }
        giveUpButton.visibility = View.VISIBLE
        /*
            As the chronometer shows the difference between the elapsed time since boot and its
            base, we have to set the base as the difference between the elapsed time since boot
            and the elapsed time since the user first started this game
         */
        timeCount.base = SystemClock.elapsedRealtime() - (currentTime - startTime)
        timeCount.start()
    }

    private fun startWaiting() {
        // Show loading symbol
        waitSymbol.visibility = View.VISIBLE
        // Wait 5 seconds so the user can't "spam" locations without time loss
        waitTimer.schedule(object : TimerTask() {
            override fun run() {
                waitTimerFinished()
            }
        }, 5000)
    }

    private fun abortWaiting() {
        waitTimer.cancel()
        // Reinitialize because a canceled timer cannot schedule any tasks
        waitTimer = Timer()
        waitSymbol.visibility = View.INVISIBLE
    }

    /*
        We have already ensured in requireLocationPermission that the location permission is granted
        so it should be fine to ignore the warning
        (a permission change at runtime forces a restart of the app)
     */
    @SuppressLint("MissingPermission")
    private fun onLocationConfirmed(view: View) {
        if (waitSymbol.visibility == View.VISIBLE) {
            // cancel confirmation if the user is waiting
            abortWaiting()
            return
        }
        startWaiting()
        val cancelToken = CancellationTokenSource().token
        // Get current user location with Google Play services
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY, cancelToken
        ).addOnSuccessListener { location ->
            lastUserPosition = location
        }
    }

    private fun waitTimerFinished() {
        val error = compassViewModel.getDirectionError(lastUserPosition!!, lastOrientation)
        // tolerance of 10 degrees
        if (abs(error) <= 10.0) {
            compassViewModel.nextLocation()
        } else {
            val hint = getString(
                if (error > 0.0) {
                    /*
                        error > 0 -> user turned device too much in clockwise direction so he should
                        turn the device counterclockwise
                     */
                    R.string.compass_hint_counterclockwise
                } else {
                    /*
                        error > 0 -> user turned device too much in counterclockwise direction so he should
                        turn the device clockwise
                     */
                    R.string.compass_hint_clockwise
                }
            )
            /**
                This method is called in the timer thread
                but the toast can only be shown on the UI thread
             */
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), getString(R.string.compass_hint, hint), Toast.LENGTH_SHORT).show()
            }
        }
        waitSymbol.visibility = View.INVISIBLE
    }

    private fun onUserReady(foundAllLocations: Boolean) {
        timeCount.stop()
        if (foundAllLocations) {
            // The user found all locations
            var endTime = compassViewModel.endTime
            if (endTime == 0L) {
                // end time has not been saved yet
                endTime = System.currentTimeMillis()
                compassViewModel.saveEndTime(endTime)
                // send Notification
                userDataViewModel.prepNotification(
                    "your turn",
                    compassViewModel.otherName + " finished the compass-game.",
                    compassViewModel.otherID
                )
            }
            timeCount.base = SystemClock.elapsedRealtime() - (endTime - compassViewModel.startTime)
            textPlayerAction.text =
                getString(
                    R.string.compass_waiting_for_opponent,
                    compassViewModel.opponentName.value?:"?"
                )
        } else {
            // The user gave up
            timeCount.visibility = View.INVISIBLE
        }
        giveUpButton.visibility = View.INVISIBLE
        compassView.isClickable = false
    }

    override fun onGiveUp() {
        compassViewModel.giveUp()
    }

    private fun onGameOver(ending: GameEnding) {
        val msgID = when (ending) {
            GameEnding.WIN -> R.string.ending_win
            GameEnding.LOSE -> R.string.ending_lose
            GameEnding.DRAW -> R.string.ending_draw
        }
        setWinner(ending)

        waitSymbol.visibility = View.INVISIBLE
        Toast.makeText(activity, msgID, Toast.LENGTH_SHORT).show()
        textPlayerAction.setText(msgID)
        if (!showEndstate) {
            // send Notification
            userDataViewModel.prepNotification(
                "Game ended",
                "A game of TicTacToe against " + compassViewModel.otherName + " has ended.",
                compassViewModel.otherID
            )
            compassViewModel.deleteLoadedGame()
        }
    }

    /**
     * adds the winner of the game to the game Object
     * If a player ins it adds the players id
     * if the game results in a draw it adds GAME_DRAW
     */
    private fun setWinner(ending: GameEnding) {
        val thisGame = compassViewModel.runGameRaw
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

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (!showEndstate) {
            // While the fragment was paused the user could have disabled device location in settings
            requireLocationSettings()
        }
        sensorManager?.registerListener(this, rotVecSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Prevent resource leak
        timeCount.stop()
        // Cancel timer to prevent executing timer task when the fragment view is destroyed
        waitTimer.cancel()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val rotMat = FloatArray(9)
        // Calculate the rotation matrix with the values from the event
        SensorManager.getRotationMatrixFromVector(rotMat, event.values)
        val orientation = FloatArray(3)
        // Calculate the orientations with the rotation matrix
        SensorManager.getOrientation(rotMat, orientation)
        /*
            We only care about the angle between the device's x-Axis and the magnetic north pole
            which is the first element of the vector
         */
        val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
        // Update UI so the needle points north
        compassNeedle.rotation = 360 - degrees
        if (waitSymbol.visibility != View.VISIBLE) {
            // only update last location if the user isn't waiting
            lastOrientation = degrees
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }
}

/*
    Since the method startResolutionForResult from the class ResolvableApiException works only for
    an activity as parameter (onActivityResult will be only called in the activity),
    we write a equivalent extension function which takes a fragment as parameter
 */
@Suppress("DEPRECATION")
fun ResolvableApiException.startResolutionForResult(fragment: Fragment, requestCode: Int) {
    val pendingIntent = status.resolution
    if (pendingIntent != null) {
        fragment.startIntentSenderForResult(
            pendingIntent.intentSender,
            requestCode, null, 0, 0, 0, null
        )
    }
}