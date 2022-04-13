package com.example.group_d.ui.main.ingame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.group_d.*
import com.example.group_d.data.model.Game
import com.example.group_d.data.model.GameEnding
import com.example.group_d.data.model.TicTacToeModel
import com.example.group_d.ui.main.recentGames.RecentGamesViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

class TicTacToeViewModel : GameViewModel() {

    lateinit var recentGamesViewModel: RecentGamesViewModel

    private val _gameModel = MutableLiveData<TicTacToeModel>()
    val gameModel: LiveData<TicTacToeModel> = _gameModel

    private val _nextField = MutableLiveData<Int>()
    val nextField: LiveData<Int> = _nextField

    private val _showOnTurn = MutableLiveData<Boolean>()
    val showOnTurn: LiveData<Boolean> = _showOnTurn

    private var turnNumber: Int = 0

    private val _ending = MutableLiveData<GameEnding>()
    val ending: LiveData<GameEnding> = _ending

    private val modelObj: TicTacToeModel get() = gameModel.value!!
    val opponentName: String
        get() = modelObj.player2.name

    var otherID: String = ""
    var otherName: String = ""

    fun fieldIsEmpty(fieldNum: Int): Boolean {
        return modelObj.fields[fieldNum].player == null
    }

    // Let the user move
    fun playerMove(fieldNum: Int) {
        // Add the field id to database
        runGameRaw.gameData.add(fieldNum.toString())
        updateGameData()
        move(fieldNum)
        // User moved, now the user is on turn
        _showOnTurn.value = false
    }

    private fun move(fieldNum: Int) {
        // Ensure field exists and is empty
        val field = if (fieldNum >= 0) modelObj.fields[fieldNum] else null
        if (field?.player != null) {
            return
        }
        if (field != null) {
            // Set player in game model
            field.player = modelObj.currentPlayer
            // Notify the UI
            _nextField.value = fieldNum
        }
        checkResult(field)
        nextPlayer()
    }

    private fun nextPlayer() {
        modelObj.currentPlayer = modelObj.currentPlayer.next!!
        turnNumber++
    }

    // Checks if the game is over
    private fun checkResult(lastSetField: TicTacToeModel.Field?) {
        if (lastSetField == null) {
            // lastSetField == null -> field ID is negative -> current player gave up and should lose
            modelObj.winner = modelObj.currentPlayer.next
            _ending.value = if (isOnTurn()) GameEnding.LOSE else GameEnding.WIN
            return
        }
        val lastPlayer = lastSetField.player!!

        // Check in the game model if there is a win (three pieces in a row)
        val win =
            // Check horizontal win
            lastSetField.west?.player == lastPlayer && lastSetField.west?.west?.player == lastPlayer
                    || lastSetField.west?.player == lastPlayer && lastSetField.east?.player == lastPlayer
                    || lastSetField.east?.player == lastPlayer && lastSetField.east?.east?.player == lastPlayer

                    // Check vertical win
                    || lastSetField.north?.player == lastPlayer && lastSetField.north?.north?.player == lastPlayer
                    || lastSetField.north?.player == lastPlayer && lastSetField.south?.player == lastPlayer
                    || lastSetField.south?.player == lastPlayer && lastSetField.south?.south?.player == lastPlayer

                    // Check diagonal wins
                    || lastSetField.northWest?.player == lastPlayer && lastSetField.northWest?.northWest?.player == lastPlayer
                    || lastSetField.northWest?.player == lastPlayer && lastSetField.southEast?.player == lastPlayer
                    || lastSetField.southEast?.player == lastPlayer && lastSetField.southEast?.southEast?.player == lastPlayer

                    || lastSetField.southWest?.player == lastPlayer && lastSetField.southWest?.southWest?.player == lastPlayer
                    || lastSetField.southWest?.player == lastPlayer && lastSetField.northEast?.player == lastPlayer
                    || lastSetField.northEast?.player == lastPlayer && lastSetField.northEast?.northEast?.player == lastPlayer

        if (win) {
            //The winner is the last player
            modelObj.winner = lastPlayer
            _ending.value = if (isOnTurn()) GameEnding.WIN else GameEnding.LOSE
        } else if (modelObj.player1.amountOfFields + modelObj.player2.amountOfFields >= TicTacToeModel.NUM_FIELDS) {
            // All fields are occupied but no win -> draw
            _ending.value = GameEnding.DRAW
        }
    }

    fun giveUp() {
        // Send a negative field id to signalize a give up
        playerMove(-1)
    }

    fun isOnTurn(): Boolean {
        return modelObj.currentPlayer == modelObj.player1
    }

    override fun initGame(snap: DocumentSnapshot, docref: DocumentReference) {
        val playerRefs = snap[GAME_PLAYERS] as List<DocumentReference>
        val beginnerIndex = snap.getString(GAME_BEGINNER) ?: "0"
        val isBeginner = playerRefs[beginnerIndex.toInt()].id == getOwnUserID()
        for (playerRef in playerRefs) {
            if (playerRef.id != getOwnUserID()) {
                // get the ID of the other player
                otherID = playerRef.id
                // get the name of the other player
                playerRef.get().addOnSuccessListener { document ->
                    otherName = document["name"].toString() }
                playerRef.get().addOnSuccessListener { playerSnap ->
                    val opponentName = playerSnap.getString(USER_DISPLAY_NAME)
                    // Build game model
                    _gameModel.apply {
                        value = TicTacToeModel.buildGame("You", opponentName ?: "")
                        value!!.currentPlayer = if (isBeginner) value!!.player1 else value!!.player2
                    }

                    val gameData = snap[GAME_DATA] as MutableList<String>
                    for (fieldNum in gameData) {
                        // Restore all moves
                        move(fieldNum.toInt())
                    }

                    _showOnTurn.value = isOnTurn()
                    // Notify the fragment that the game is loaded
                    runGame.value =
                        Game(beginnerIndex, gameData, GAME_TYPE_TIC_TAC_TOE, playerRefs)
                    addGameDataChangedListener(docref)
                }
            }
        }
    }

    override fun showEndstate(gameID: String) {
        var localGame: Game? = null
        for (game in recentGamesViewModel.recentGames) {
            if (gameID == game.id) {
                localGame = game
                break
            }
        }

        val isBeginner = localGame!!.players[localGame.beginner.toInt()].id == getOwnUserID()
        for (player in localGame.players) {
            if (player.id != getOwnUserID()) {
                player.get().addOnSuccessListener { playerSnap ->
                    val opponentName = playerSnap.getString(USER_DISPLAY_NAME)
                    _gameModel.apply {
                        value = TicTacToeModel.buildGame("You", opponentName ?: "")
                        value!!.currentPlayer = if (isBeginner) value!!.player1 else value!!.player2

                    }
                    val gameData = localGame.gameData

                    // Set the value before the game is finished and life data observers are removed
                    runGame.value = localGame

                    for (fieldNum in gameData) {
                        move(fieldNum.toInt())
                    }
                }
            }
        }
    }

    override fun onGameDataChanged(gameData: List<String>) {
        if (isOnTurn()) {
            // User is on turn -> the game data change is a user move
            return
        }
        if (turnNumber >= gameData.size) {
            return
        }
        val movedFieldNum = gameData[turnNumber].toInt()
        move(movedFieldNum)
        // Opponent moved, now the user is on turn
        _showOnTurn.value = true
    }
}