package com.example.group_d.ui.main.challenges

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.R
import com.example.group_d.data.model.Challenge

class ChallengeAdapter(private val challenges: List<Challenge>, private val fragment: ChallengesFragment) :
    RecyclerView.Adapter<ChallengeAdapter.ViewHolder>() {

    class ViewHolder(view: View, fragment: ChallengesFragment) : RecyclerView.ViewHolder(view) {
        private val playerName: TextView = view.findViewById(R.id.text_player_name)
        private val buttonAccept: Button = view.findViewById(R.id.button_challenge_acc)
        private val buttonDecline: Button = view.findViewById(R.id.button_challenge_decl)
        private lateinit var challenge: Challenge

        init {
            buttonAccept.setOnClickListener {
                fragment.onAccept(challenge)
            }

            buttonDecline.setOnClickListener {
                fragment.apply {
                    ChallengeDeclineDialogFragment(challenge, this).show(
                        parentFragmentManager,
                        "challenge_decline_${challenge.user.id}"
                    )
                }
            }
        }

        fun bind(challenge: Challenge) {
            this.challenge = challenge
            playerName.text = challenge.user.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_challenge_item, parent, false)

        return ViewHolder(view, fragment)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(challenges[position])
    }

    override fun getItemCount(): Int {
        return challenges.size
    }
}