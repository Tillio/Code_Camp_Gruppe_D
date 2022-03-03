package com.example.group_d.ui.main.ingame

import android.Manifest
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
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.group_d.R
import com.example.group_d.data.model.Game
import com.example.group_d.databinding.CompassFragmentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.*

class CompassFragment : Fragment(), SensorEventListener {
    private lateinit var compassViewModel: CompassViewModel
    private var _binding: CompassFragmentBinding? = null
    private val args: CompassFragmentArgs by navArgs()

    private lateinit var textOpName: TextView
    private lateinit var textPlayerAction: TextView
    private lateinit var waitSymbol: ProgressBar
    private lateinit var timeCount: Chronometer
    private lateinit var compassNeedle: ImageView

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
        val compassView = binding.compassView
        val buttonGiveUp = binding.buttonGiveUp

        compassViewModel.opponentName.observe(viewLifecycleOwner) { opName ->
            textOpName.text = opName
        }

        compassViewModel.currentLocation.observe(viewLifecycleOwner) { curLocation ->
            textPlayerAction.text = "${curLocation.name}, ${curLocation.addr}"
        }

        compassViewModel.foundAllLocations.observe(viewLifecycleOwner) { foundAllLocations ->
            if (foundAllLocations) {
                onAllLocationsFound()
            }
        }

        compassView.setOnClickListener(this::onLocationConfirmed)

        buttonGiveUp.setOnClickListener {
            compassViewModel.deleteLoadedGame()
        }

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        rotVecSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        compassViewModel.loadLocations(String(resources.openRawResource(R.raw.compass_data).readBytes()))
        compassViewModel.runGame.observe(viewLifecycleOwner, this::onGameLoaded)

        requireLocation()

        return root
    }

    private fun requireLocation() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                compassViewModel.loadRunningGame(args.gameID)
            }
        }
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                compassViewModel.loadRunningGame(args.gameID)
            }
            else -> {
                requestPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                )
            }
        }

    }

    private fun onGameLoaded(game: Game?) {
        var timerBase = compassViewModel.loadTimerBase()
        timerBase = if (timerBase != 0L) timerBase else SystemClock.elapsedRealtime()
        timeCount.base = timerBase
        timeCount.start()
        compassViewModel.saveTimerBase(timerBase)
    }

    private fun onLocationConfirmed(view: View) {
        if (waitSymbol.visibility == View.VISIBLE) {
            // cancel confirmation if the user is waiting
            waitTimer.cancel()
            waitSymbol.visibility = View.INVISIBLE
            return
        }
        waitSymbol.visibility = View.VISIBLE
        waitTimer = Timer()
        waitTimer.schedule(object : TimerTask() {
            override fun run() {
                waitTimerFinished()
            }
        }, 5000)
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val cancelToken = CancellationTokenSource().token
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY, cancelToken
        ).addOnSuccessListener { location ->
            lastUserPosition = location
        }
    }

    private fun waitTimerFinished() {
        if (compassViewModel.checkRightDirection(lastUserPosition!!, lastOrientation)) {
            compassViewModel.nextLocation()
        }
        waitSymbol.visibility = View.INVISIBLE
    }

    private fun onAllLocationsFound() {
        timeCount.stop()
        val neededTime = timeCount.text.split(":").run {
            var result = 0
            forEach {
                result *= 60
                result += it.toInt()
            }
            result
        }
        compassViewModel.saveNeededTime(neededTime)
        textPlayerAction.text =
            getString(R.string.compass_waiting_for_opponent, compassViewModel.opponentName.value?:"?")
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
        SensorManager.getRotationMatrixFromVector(rotMat, event.values.filterIndexed { index, fl -> index < 3 }.toFloatArray())
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotMat, orientation)
        val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
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