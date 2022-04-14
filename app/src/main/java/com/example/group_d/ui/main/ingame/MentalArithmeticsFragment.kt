package com.example.group_d.ui.main.ingame

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.group_d.COL_GAMES
import com.example.group_d.GAME_DATA
import com.example.group_d.GAME_DRAW
import com.example.group_d.R
import com.example.group_d.data.model.GameEnding
import com.example.group_d.data.model.Problem
import com.example.group_d.data.model.UserDataViewModel
import com.example.group_d.databinding.FragmentMentalArithmeticsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

class MentalArithmeticsFragment : Fragment() {

    private var _binding: FragmentMentalArithmeticsBinding? = null
    private val db = Firebase.firestore
    private val args: MentalArithmeticsFragmentArgs by navArgs()
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    private lateinit var timer: Chronometer
    private lateinit var assignment: TextView
    private lateinit var userSolution: EditText
    private lateinit var submitSolution: Button
    private lateinit var mentalArithmeticsViewModel: MentalArithmeticsViewModel
    private lateinit var opponentTime: TextView

    companion object {
        fun newInstance() = MentalArithmeticsFragment()
    }

    private val binding get() = _binding!!

    private lateinit var viewModel: MentalArithmeticsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMentalArithmeticsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        timer = root.findViewById(R.id.timer)

        mentalArithmeticsViewModel =
            ViewModelProvider(this)[MentalArithmeticsViewModel::class.java]

        mentalArithmeticsViewModel.winner.observe(viewLifecycleOwner) { winner ->
            val msgID = "The winner is: " + winner
            Toast.makeText(activity, msgID, Toast.LENGTH_LONG).show()
            // send Notification
            //the winner gets a WON text and the loser a LOST text in place of the assignments
            userDataViewModel.prepNotification(
                getString(R.string.notify_game_ended_title),
                "Mental Arithmetics",
                mentalArithmeticsViewModel.otherID
            )
            var ending: GameEnding
            if(winner == Firebase.auth.currentUser!!.email) {
                assignment.text = "WON"
                ending = GameEnding.WIN
            } else {
                assignment.text = "LOST"
                ending = GameEnding.LOSE
            }
            setWinner(ending)
            mentalArithmeticsViewModel.deleteLoadedGame()
        }

        opponentTime = root.findViewById(R.id.opponentTime)

        mentalArithmeticsViewModel.opponentTime.observe(viewLifecycleOwner) { opponentTime ->
            this.opponentTime.text = "Opponent: " + opponentTime
        }

        val game = db.collection(COL_GAMES).document(args.gameID).get().addOnSuccessListener { doc ->
            val gameData = doc.data!!.get("gameData") as ArrayList<String>
            var problemNumber = 1
            var started = false
            var timerBase: Long = 0
            var finished = false
            var finishTime: String = ""
            //the game database is read and the stored variables are stored locally to work with them
            if(gameData.size > 1) {
                for (i in 1 until (gameData.size)) {
                    val dataItem = gameData[i].split("=")
                    if ((dataItem.get(0) == Firebase.auth.currentUser!!.email) && (dataItem.get(1) == "started")) {
                        started = true
                    }
                    if((dataItem.get(0) == Firebase.auth.currentUser!!.email) && (dataItem.get(1) == "timerBase")) {
                        timerBase = dataItem.get(2).toLong()
                    }
                    if((dataItem.get(0) == Firebase.auth.currentUser!!.email) && (dataItem.get(1) == "problemNumber")) {
                        problemNumber = dataItem.get(2).toInt()
                    }
                    if ((dataItem.get(0) == Firebase.auth.currentUser!!.email) && (dataItem.get(1) == "finalTime")) {
                        finished = true
                        finishTime = dataItem.get(2)
                    }
                }
            }

            val problems = createProblems(gameData.get(0))

            userSolution = root.findViewById(R.id.solution)

            submitSolution = root.findViewById(R.id.submitSolution)

            assignment = root.findViewById(R.id.assignment)

            var currentProblem = Problem(0, 0, "?")
            val problemText: String

            //if the game started and is not finished yet, the start button changes to submit and the first problem is shown also the timer starts
            if(started && !finished) {
                submitSolution.text = "Submit"
                for(i in 0 until problemNumber-1) {
                    currentProblem = problems.removeFirst()
                }
                val problemText = currentProblem.left.toString() + currentProblem.operator + currentProblem.right.toString()
                assignment.text = problemText
                timer.base = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - timerBase)
                timer.start()
            }else if(finished){
                //if the game is finished the problem text changes to finished and the submit button and textfield becomes invisible
                assignment.text = "FINISHED"
                timer.text = finishTime
                submitSolution.visibility = View.GONE
                userSolution.visibility = View.GONE

            }

            submitSolution.setOnClickListener {
                if(submitSolution.text == "Start") {
                    //it is stored in the database, that the game started
                    db.collection(COL_GAMES).document(args.gameID).update(GAME_DATA, FieldValue.arrayUnion(
                        Firebase.auth.currentUser!!.email + "=" + "started"))
                    currentProblem = problems.removeFirst()
                    val problemText = currentProblem.left.toString() + currentProblem.operator + currentProblem.right.toString()
                    assignment.text = problemText
                    //the button text changes from start to submit
                    submitSolution.text = "Submit"
                    val timerBase = System.currentTimeMillis()
                    //timerbase is stored in the database
                    db.collection(COL_GAMES).document(args.gameID).update(GAME_DATA, FieldValue.arrayUnion(
                        Firebase.auth.currentUser!!.email + "=" + "timerBase" + "=" + timerBase
                    ))

                    timer.base = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - timerBase)
                    timer.start()

                } else if(submitSolution.text == "Submit") {

                    //calculating the correct solution
                    var realSolution = 0
                    if(currentProblem.operator == "+") {
                        realSolution = currentProblem.left + currentProblem.right
                    } else if (currentProblem.operator == "-") {
                        realSolution = currentProblem.left - currentProblem.right
                    } else if (currentProblem.operator == "*") {
                        realSolution = currentProblem.left * currentProblem.right
                    }

                    //if the user solution is correct, the next problem gets shown or the game is finished
                    if(realSolution.toString() == userSolution.text.toString()) {
                        db.collection(COL_GAMES).document(args.gameID).update(
                            GAME_DATA, FieldValue.arrayRemove(
                                Firebase.auth.currentUser!!.email + "=" + "problemNumber" +"="+ problemNumber
                            )
                        )
                        problemNumber += 1
                        db.collection(COL_GAMES).document(args.gameID).update(
                            GAME_DATA, FieldValue.arrayUnion(
                                Firebase.auth.currentUser!!.email + "=" + "problemNumber" + "=" + problemNumber
                            )
                        )
                        if(problems.isNotEmpty()) {
                            currentProblem = problems.removeFirst()
                            val problemText =
                                currentProblem.left.toString() + currentProblem.operator + currentProblem.right.toString()
                            assignment.text = problemText
                        } else if(problems.isEmpty()) {
                            timer.stop()
                            assignment.setText("FINISHED")
                            // send Notification
                            userDataViewModel.prepNotification(
                                getString(R.string.notify_your_turn_title),
                                getString(R.string.notify_your_turn_mentarith_msg, userDataViewModel.getOwnDisplayName()),
                                mentalArithmeticsViewModel.otherID
                            )
                            db.collection(COL_GAMES).document(args.gameID).update(GAME_DATA, FieldValue.arrayUnion(
                                Firebase.auth.currentUser!!.email + "=" + "finalTime" + "=" + timer.text))
                        }
                    }
                }
                userSolution.setText("")
            }
        }

        mentalArithmeticsViewModel.loadRunningGame(args.gameID)

        return root
    }

    private fun setWinner(ending: GameEnding) {
        val thisGame = mentalArithmeticsViewModel.runGameRaw
        val ownId = userDataViewModel.getOwnUserID()
        var opponentId = ""
        for (player in thisGame.players) {
            if (ownId != player.id) {
                opponentId = player.id
                break
            }
        }
        thisGame.winner = when (ending) {
            GameEnding.WIN -> ownId
            GameEnding.LOSE -> opponentId
            else -> GAME_DRAW
        }
    }

    private fun createProblems(seed: String): ArrayList<Problem> {
        var problems = arrayListOf<Problem>()
        val random = Random(seed.toInt())
        problems.add(Problem(random.nextInt(0, 10), random.nextInt(10, 100), "+"))
        problems.add(Problem(random.nextInt(10, 100), random.nextInt(10, 100), "-"))
        problems.add(Problem(random.nextInt(10, 100), random.nextInt(10, 100), "+"))
        problems.add(Problem(random.nextInt(100, 1000), random.nextInt(10, 100), "+"))
        //problems.add(Problem(random.nextInt(100, 1000), random.nextInt(100, 1000), "-"))
        //problems.add(Problem(random.nextInt(0, 10), random.nextInt(0, 10), "*"))
        //problems.add(Problem(random.nextInt(0, 10), random.nextInt(10, 50), "*"))
        //problems.add(Problem(random.nextInt(1000, 10000), random.nextInt(1000, 10000), "+"))
        //problems.add(Problem(random.nextInt(1000, 10000), random.nextInt(1000, 10000), "+"))
        //problems.add(Problem(random.nextInt(10, 26), random.nextInt(10, 26), "*"))
        return problems
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MentalArithmeticsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}