package com.example.group_d.ui.main.ingame

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.group_d.R
import com.example.group_d.databinding.CompassFragmentBinding

class CompassFragment : Fragment(), SensorEventListener {
    private lateinit var compassViewModel: CompassViewModel
    private var _binding: CompassFragmentBinding? = null
    private val args: CompassFragmentArgs by navArgs()

    private lateinit var textOpName: TextView
    private lateinit var textPlayerAction: TextView
    private lateinit var compassNeedle: ImageView

    private var sensorManager: SensorManager? = null
    private var rotVecSensor: Sensor? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        compassViewModel = ViewModelProvider(this).get(CompassViewModel::class.java)

        _binding = CompassFragmentBinding.inflate(inflater, container, false)
        val root = binding.root
        textOpName = binding.textOpName
        textPlayerAction = binding.textPlayerAction
        compassNeedle = binding.compassNeedle
        val compassView = binding.compassView

        compassViewModel.opponentName.observe(viewLifecycleOwner) { opName ->
            textOpName.text = opName
        }

        compassViewModel.currentLocation.observe(viewLifecycleOwner) { curLocation ->
            textPlayerAction.text = "${curLocation.name}, ${curLocation.addr}"
        }

        compassView.setOnClickListener(this::onLocationConfirmed)

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        rotVecSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        compassViewModel.loadLocations(String(resources.openRawResource(R.raw.compass_data).readBytes()))
        compassViewModel.loadRunningGame(args.gameID)

        return root
    }

    private fun onLocationConfirmed(view: View) {
        compassViewModel.nextLocation()
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
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }
}