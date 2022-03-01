package com.example.group_d.ui.main.recentGames.statistiks

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.group_d.R
import com.example.group_d.databinding.StatistiksFragmentBinding

class StatistiksFragment : Fragment() {

    private var _binding:StatistiksFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = StatistiksFragment()
    }

    private lateinit var viewModel: StatistiksViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = StatistiksFragmentBinding.inflate(inflater, container, false)
        val root = binding.root

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(StatistiksViewModel::class.java)
        // TODO: Use the ViewModel
    }

}