package com.example.group_d.ui.main.games

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.GAME_TYPE_MAP
import com.example.group_d.R
import com.example.group_d.USER_DISPLAY_NAME
import com.example.group_d.data.model.Game
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class GamesAdapter(private val games: ArrayList<Game>, private val gameStarter: GameStarter) :
    RecyclerView.Adapter<GamesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ownVsEnemyName: TextView = view.findViewById(R.id.own_vs_enemy_name)
        val gameTypeText: TextView = view.findViewById(R.id.game_type)
        val whosTurn: TextView = view.findViewById(R.id.whos_turn)
    }

    interface GameStarter {

        fun startGame(game: Game)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_game_item, parent, false)


        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        for(doc in games[position].players) {
            if(doc.id != FirebaseAuth.getInstance().uid){
                doc.get().addOnSuccessListener {
                    val opName = it.data?.get(USER_DISPLAY_NAME)
                    holder.ownVsEnemyName.setText(R.string.player_names, opName)
                    val gameTypeString = GAME_TYPE_MAP[games[position].gameType]
                    holder.gameTypeText.text = gameTypeString
                    val lastPlayer = games[position].lastPlayer
                    val whosTurnTxt = if (lastPlayer != Firebase.auth.currentUser!!.uid) {
                        // The user is not the last player and is on turn
                        R.string.your_turn
                    } else {
                        R.string.other_player_turn
                    }

                    holder.whosTurn.setText(whosTurnTxt, opName)
                }
            }
        }
        holder.itemView.setOnClickListener {
            gameStarter.startGame(games[position])
        }


    }


    override fun getItemCount(): Int {
        return games.size
    }

    // Extension to enable formatted strings for text fields
    private fun TextView.setText(resId: Int, vararg formatArgs: Any?) {
        text = context.resources.getString(resId, *formatArgs)
    }
}