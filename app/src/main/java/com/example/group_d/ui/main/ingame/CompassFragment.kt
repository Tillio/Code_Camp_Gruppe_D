package com.example.group_d.ui.main.ingame

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.group_d.LOCATIONS_GET_QUERY
import com.example.group_d.R
import com.example.group_d.data.json.CompassLocationListDeserializer
import com.example.group_d.data.json.RetrofitInstanceBuilder
import com.example.group_d.data.model.CompassLocation
import com.example.group_d.data.model.Game
import com.example.group_d.data.model.GameEnding
import com.example.group_d.databinding.CompassFragmentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
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

    private lateinit var compassViewModel: CompassViewModel
    private var _binding: CompassFragmentBinding? = null
    private val args: CompassFragmentArgs by navArgs()

    private lateinit var textOpName: TextView
    private lateinit var textPlayerAction: TextView
    private lateinit var waitSymbol: ProgressBar
    private lateinit var timeCount: Chronometer
    private lateinit var compassView: View
    private lateinit var compassNeedle: ImageView
    private lateinit var buttonGiveUp: Button

    private var sensorManager: SensorManager? = null
    private var rotVecSensor: Sensor? = null
    private lateinit var waitTimer: Timer

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastUserPosition: Location? = null
    private var lastOrientation: Float = 0.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        compassViewModel = ViewModelProvider(this).get(CompassViewModel::class.java)

        _binding = CompassFragmentBinding.inflate(inflater, container, false)
        val root = binding.root
        textOpName = binding.textOpName
        textPlayerAction = binding.textPlayerAction
        waitSymbol = binding.wait
        timeCount = binding.compassTimer
        compassNeedle = binding.compassNeedle
        compassView = binding.compassView
        buttonGiveUp = binding.buttonGiveUp

        compassViewModel.opponentName.observe(viewLifecycleOwner) { opName ->
            // Show the opponent's name in the text field
            textOpName.text = opName
        }

        compassViewModel.currentLocation.observe(viewLifecycleOwner) { curLocation ->
            // Show the current location
            textPlayerAction.text = "${curLocation.name}, ${curLocation.addr}"
        }

        compassViewModel.foundAllLocations.observe(viewLifecycleOwner) { foundAllLocations ->
            if (foundAllLocations) {
                onAllLocationsFound()
            }
        }

        compassViewModel.ending.observe(viewLifecycleOwner) { ending ->
            onGameOver(ending)
        }

        buttonGiveUp.setOnClickListener {
            GiveUpDialogFragment(this).show(parentFragmentManager, "give_up")
        }

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        rotVecSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        // Make sure the location permission is granted
        requireLocationPermission()

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
                locationPermissionGranted()
            } else {
                locationPermissionDenied()
            }
        }
        when (PackageManager.PERMISSION_GRANTED) {
            // Check if location permission is already granted
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                locationPermissionGranted()
            }
            else -> {
                // If not inform the user
                LocationRequestDialogFragment(requestPermissionLauncher)
                    .show(parentFragmentManager, "location_request")
            }
        }

    }

    private fun locationPermissionGranted() {
        // Show the user there is a loading process
        waitSymbol.visibility = View.VISIBLE
        textPlayerAction.setText(R.string.compass_loading_locations)
        // Start the call to the API with this fragment object as callback
        restCall.enqueue(this)
    }

    private fun locationPermissionDenied() {
        ErrorDialogFragment(
            R.string.compass_location_denied_dialog_title,
            R.string.compass_location_denied_dialog_msg
        ).show(parentFragmentManager, "location_deniedshow")
    }

    private fun onGameLoaded(game: Game?) {
        // Load timer base from data base
        var timerBase = compassViewModel.loadTimerBase()
        timerBase = if (timerBase != 0L)
            timerBase
        else
            // timerBase == 0 -> There isn't a timer base saved -> create new timer base
            SystemClock.elapsedRealtime()
        timeCount.base = timerBase
        timeCount.start()
        // Save timer base to data base
        compassViewModel.saveTimerBase(timerBase)
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
            waitTimer.cancel()
            waitSymbol.visibility = View.INVISIBLE
            return
        }
        // Show loading symbol
        waitSymbol.visibility = View.VISIBLE
        // Wait 5 seconds so the user can't "spam" locations without time loss
        waitTimer = Timer()
        waitTimer.schedule(object : TimerTask() {
            override fun run() {
                waitTimerFinished()
            }
        }, 5000)
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
            /*
                This method is called in the timer thread
                but the toast can only be shown on the UI thread
             */
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), getString(R.string.compass_hint, hint), Toast.LENGTH_SHORT).show()
            }
        }
        waitSymbol.visibility = View.INVISIBLE
    }

    private fun onAllLocationsFound() {
        timeCount.stop()
        if (compassViewModel.neededTime == 0) {
            // neededTime has not been saved yet
            val neededTime = timeCount.text
            compassViewModel.saveNeededTime(neededTime)
        }
        timeCount.base = SystemClock.elapsedRealtime() - 1000 * compassViewModel.neededTime
        buttonGiveUp.visibility = View.INVISIBLE
        compassView.isClickable = false
        textPlayerAction.text =
            getString(R.string.compass_waiting_for_opponent, compassViewModel.opponentName.value?:"?")
    }

    override fun onGiveUp() {
        compassViewModel.giveUp()
    }

    private fun onGameOver(ending: GameEnding) {
        // stop the time count because in case the user gave up it hasn't been called so far
        timeCount.stop()
        val msgID = when (ending) {
            GameEnding.WIN -> R.string.ending_win
            GameEnding.LOSE -> R.string.ending_lose
            GameEnding.DRAW -> R.string.ending_draw
        }
        waitSymbol.visibility = View.INVISIBLE
        Toast.makeText(activity, msgID, Toast.LENGTH_SHORT).show()
        textPlayerAction.setText(msgID)
        compassViewModel.deleteLoadedGame()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, rotVecSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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