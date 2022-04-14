package com.example.group_d.ui.main.recentGames.statistiks

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.charts.PieChart


class StatisticsFragment : Fragment() {

    private var _binding:FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val statisticsViewModel: StatisticsViewModel by activityViewModels()
    private var recyclerAdapter = StatisticsAdapter()
    private lateinit var pieChart: PieChart

    companion object {
        fun newInstance() = StatisticsFragment()
    }

    private lateinit var viewModel: StatisticsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val statisticsRecycler: RecyclerView?
        viewModel = ViewModelProvider(requireActivity())[StatisticsViewModel::class.java]

        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        val root = binding.root


        pieChart= root.findViewById(R.id.winLossPie)
        initPieChart()


        statisticsRecycler = root.findViewById<RecyclerView>(R.id.statistiks_recycler).apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(context)
        }
        statisticsViewModel.pastGamesData.observe(viewLifecycleOwner){it ->
            recyclerAdapter.data = it
            statisticsRecycler.adapter?.notifyDataSetChanged()
            pieChart.data = statisticsViewModel.pieData()

            pieChart.data.dataSet.valueTextColor = Color.parseColor("#CCFFFFFF")
            pieChart.legend.isEnabled = false

            pieChart.invalidate()

        }
        viewModel.updateData()


        return root
    }

    fun initPieChart(){
        pieChart.description.isEnabled = false
        pieChart.isRotationEnabled = false
        pieChart.description.textColor = Color.WHITE
        pieChart.setEntryLabelColor(Color.parseColor("#CCFFFFFF"))
        pieChart.setHoleColor(Color.parseColor("#212121"))


    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Use the ViewModel
    }



}