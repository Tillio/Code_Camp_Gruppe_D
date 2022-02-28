package com.example.group_d.ui.main.ingame

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.group_d.COL_GAMES
import com.example.group_d.R
import com.example.group_d.data.model.Problem
import com.example.group_d.data.model.UserDataViewModel
import com.example.group_d.databinding.MentalArithmeticsFragmentBinding
import com.example.group_d.databinding.TicTacToeFragmentBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

class MentalArithmeticsFragment : Fragment() {

    private var _binding: MentalArithmeticsFragmentBinding? = null
    private val db = Firebase.firestore
    private val args: MentalArithmeticsFragmentArgs by navArgs()
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    private lateinit var timer: Chronometer
    private lateinit var assignment: TextView
    private lateinit var solution: EditText

    companion object {
        fun newInstance() = MentalArithmeticsFragment()
    }

    private val binding get() = _binding!!

    private lateinit var viewModel: MentalArithmeticsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MentalArithmeticsFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        timer = root.findViewById(R.id.timer)
        //val game = userDataViewModel.gameIdIsLocal(args.gameID)
        val game = db.collection(COL_GAMES).document(args.gameID).get().addOnSuccessListener { doc ->
            val gameData = doc.data!!.get("gameData") as ArrayList<String>

            val problems = createProblems(gameData.get(0))
            timer.start()
            for(i in 0..10) {
                var realSolution = 0
                val currentProblem = problems.first()
                if(currentProblem.operator == "+") {
                    realSolution = currentProblem.left + currentProblem.right
                } else if (currentProblem.operator == "-") {
                    realSolution = currentProblem.left - currentProblem.right
                } else if (currentProblem.operator == "*") {
                    realSolution = currentProblem.left * currentProblem.right
                }
                val problemText = currentProblem.left.toString() + currentProblem.operator + currentProblem.right.toString()
                assignment = root.findViewById(R.id.assignment)
                assignment.text = problemText
                problems.removeFirst()
                solution = root.findViewById(R.id.solution)

                solution.addTextChangedListener(object: TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun afterTextChanged(p0: Editable?) {
                        TODO("Not yet implemented")
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        TODO("Not yet implemented")
                    }
                })
            }
            timer.stop()
        }

        return root
    }

    private fun createProblems(seed: String): ArrayList<Problem> {
        var problems = arrayListOf<Problem>()
        val random = Random(seed.toInt())
        problems.add(Problem(random.nextInt(0, 10), random.nextInt(10, 100), "+"))
        problems.add(Problem(random.nextInt(10, 100), random.nextInt(10, 100), "-"))
        problems.add(Problem(random.nextInt(10, 100), random.nextInt(10, 100), "+"))
        problems.add(Problem(random.nextInt(100, 1000), random.nextInt(10, 100), "+"))
        problems.add(Problem(random.nextInt(100, 1000), random.nextInt(100, 1000), "-"))
        problems.add(Problem(random.nextInt(0, 10), random.nextInt(0, 10), "*"))
        problems.add(Problem(random.nextInt(0, 10), random.nextInt(10, 100), "*"))
        problems.add(Problem(random.nextInt(1000, 10000), random.nextInt(1000, 10000), "+"))
        problems.add(Problem(random.nextInt(1000, 10000), random.nextInt(1000, 10000), "+"))
        problems.add(Problem(random.nextInt(10, 26), random.nextInt(10, 26), "*"))
        return problems
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MentalArithmeticsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}