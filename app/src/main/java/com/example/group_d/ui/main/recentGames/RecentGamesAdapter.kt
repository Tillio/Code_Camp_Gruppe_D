package com.example.group_d.ui.main.recentGames

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.group_d.GAME_TYPE_MAP
import com.example.group_d.R
import com.example.group_d.USER_DISPLAY_NAME
import com.example.group_d.data.model.Game
import com.google.firebase.auth.FirebaseAuth


class RecentGamesAdapter(private val recentGames: ArrayList<Game>, private val gameStarter: GameStarter) :
    RecyclerView.Adapter<RecentGamesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val opponentName: TextView = view.findViewById(R.id.opponent)
        val gameTypeText: TextView = view.findViewById(R.id.gameType)
        //val winImageview: ImageView = view.findViewById(R.id.winIcon)
        val defeatImageview: ImageView = view.findViewById(R.id.defeatIcon)
        val gameDetailButton: ImageButton = view.findViewById(R.id.gameDetailsButton)

    }

    interface GameStarter {

        fun startGame(game: Game)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_game_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val game:Game=recentGames[position]

        loadOpponent(holder, game)

        holder.gameTypeText.text = GAME_TYPE_MAP[game.gameType]
        holder.defeatImageview.isVisible = false
        holder.gameDetailButton.setOnClickListener {
            gameStarter.startGame(recentGames[position])
        }

    }

    /**
     * load opponents name from firestore
     */
    private fun loadOpponent(holder: ViewHolder, game:Game){
        for(doc in game.players) {
            doc.get().addOnSuccessListener {user ->
                var name = user.get(USER_DISPLAY_NAME).toString()
                if (doc.id != FirebaseAuth.getInstance().uid){
                    holder.opponentName.text = name
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return recentGames.size
    }
}