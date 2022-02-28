package com.example.group_d.ui.main.ingame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.group_d.databinding.CompassFragmentBinding

class CompassFragment : Fragment() {
    private lateinit var compassViewModel: CompassViewModel
    private var _binding: CompassFragmentBinding? = null

    private lateinit var compassNeedle: ImageView

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
        compassNeedle = binding.compassNeedle
        compassNeedle.rotation = 90F

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}