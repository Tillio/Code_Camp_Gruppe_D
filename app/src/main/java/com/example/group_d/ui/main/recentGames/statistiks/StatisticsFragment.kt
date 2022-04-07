package com.example.group_d.ui.main.recentGames.statistiks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry


class StatisticsFragment : Fragment() {

    private var _binding:FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val statisticsViewModel: StatisticsViewModel by activityViewModels()
    private var recyclerAdapter = StatisticsAdapter()

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


        var pieChart: PieChart = root.findViewById(R.id.winLossPie)
        pieChart.data = pieData()

        statisticsRecycler = root.findViewById<RecyclerView>(R.id.statistiks_recycler).apply {
            adapter = recyclerAdapter
            layoutManager = LinearLayoutManager(context)
        }
        statisticsViewModel.pastGamesData.observe(viewLifecycleOwner){it ->
            recyclerAdapter.data = it
            statisticsRecycler.adapter?.notifyDataSetChanged()

        }
        viewModel.updateData()

        val upButton: Button = root.findViewById(R.id.updateButton)
        upButton.setOnClickListener{
            viewModel.updateData()
        }

        return root
    }

    fun pieData(): PieData{
        val entry1 = PieEntry(50f, "1")
        val entry2 = PieEntry(50f, "2")
        val arrayListOf = arrayListOf<PieEntry>(entry1, entry2)
        val pieDataSet = PieDataSet(arrayListOf, "Pie")
        return PieData(pieDataSet)

    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Use the ViewModel
    }



}