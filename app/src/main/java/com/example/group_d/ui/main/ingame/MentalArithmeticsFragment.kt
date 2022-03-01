package com.example.group_d.ui.main.ingame

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
    private lateinit var userSolution: EditText
    private lateinit var submitSolution: Button

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

            userSolution = root.findViewById(R.id.solution)

            submitSolution = root.findViewById(R.id.submitSolution)

            submitSolution.text = "Start"

            assignment = root.findViewById(R.id.assignment)

            var currentProblem = Problem(0, 0, "?")

            submitSolution.setOnClickListener {
                if(submitSolution.text == "Start") {
                    currentProblem = problems.removeFirst()
                    val problemText = currentProblem.left.toString() + currentProblem.operator + currentProblem.right.toString()
                    assignment.text = problemText
                    submitSolution.text = "Submit"
                    timer.start()
                } else if(submitSolution.text == "Submit") {

                    //berechnen die richtige Lösung
                    var realSolution = 0
                    if(currentProblem.operator == "+") {
                        realSolution = currentProblem.left + currentProblem.right
                    } else if (currentProblem.operator == "-") {
                        realSolution = currentProblem.left - currentProblem.right
                    } else if (currentProblem.operator == "*") {
                        realSolution = currentProblem.left * currentProblem.right
                    }

                    //Wenn die Lösung richtig ist, wird das nächste Problem eingefügt
                    if(realSolution.toString() == userSolution.text.toString()) {
                        if(problems.isNotEmpty()) {
                            currentProblem = problems.removeFirst()
                            val problemText =
                                currentProblem.left.toString() + currentProblem.operator + currentProblem.right.toString()
                            assignment.text = problemText
                        } else if(problems.isEmpty()) {
                            timer.stop()
                        }
                    }
                }
            }
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