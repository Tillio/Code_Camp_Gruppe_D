package com.example.group_d.ui.main.ui.challenges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.data.model.Challenge
import com.example.group_d.data.model.GameType
import com.example.group_d.data.model.User
import com.example.group_d.databinding.FragmentChallengesBinding

class ChallengesFragment : Fragment() {

    private lateinit var challengesViewModel: ChallengesViewModel
    private var _binding: FragmentChallengesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        challengesViewModel =
            ViewModelProvider(this).get(ChallengesViewModel::class.java)

        _binding = FragmentChallengesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textChallenges
        /*challengesViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/
        val recyclerView: RecyclerView = binding.recyclerViewChallenges
        recyclerView.adapter = ChallengeAdapter(exampleChallenges())
        recyclerView.layoutManager = LinearLayoutManager(context)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun exampleChallenges(): MutableList<Challenge> {
        val challenges: MutableList<Challenge> = ArrayList()
        for (i in 1..10) {
            challenges.add(Challenge(User("$i", "User $i", true), GameType.TIC_TAC_TOE))
        }
        return challenges
    }
}