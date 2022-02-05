package com.example.group_d.ui.main.ui.ingame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.group_d.*
import com.example.group_d.data.model.Game
import com.example.group_d.data.model.GameEnding
import com.example.group_d.data.model.tictactoe.TicTacToeModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

class TicTacToeViewModel() : GameViewModel() {

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

    fun fieldIsEmpty(fieldNum: Int): Boolean {
        return modelObj.fields[fieldNum].player == null
    }

    fun playerMove(fieldNum: Int) {
        runGameRaw.gameData.add(fieldNum.toLong())
        updateGameData()
        move(fieldNum)
        _showOnTurn.value = false
    }

    fun move(fieldNum: Int) {
        val field = modelObj.fields[fieldNum]
        if (field.player != null) {
            return
        }
        field.player = modelObj.currentPlayer
        _nextField.value = fieldNum
        checkResult(field)
        nextPlayer()
    }

    private fun nextPlayer() {
        modelObj.currentPlayer = modelObj.currentPlayer.next!!
        turnNumber++
    }

    private fun checkResult(lastSetField: TicTacToeModel.Field) {
        val lastPlayer = lastSetField.player!!

        val win = lastSetField.west?.player == lastPlayer && lastSetField.west?.west?.player == lastPlayer
                || lastSetField.west?.player == lastPlayer && lastSetField.east?.player == lastPlayer
                || lastSetField.east?.player == lastPlayer && lastSetField.east?.east?.player == lastPlayer

                || lastSetField.north?.player == lastPlayer && lastSetField.north?.north?.player == lastPlayer
                || lastSetField.north?.player == lastPlayer && lastSetField.south?.player == lastPlayer
                || lastSetField.south?.player == lastPlayer && lastSetField.south?.south?.player == lastPlayer

                || lastSetField.northWest?.player == lastPlayer && lastSetField.northWest?.northWest?.player == lastPlayer
                || lastSetField.northWest?.player == lastPlayer && lastSetField.southEast?.player == lastPlayer
                || lastSetField.southEast?.player == lastPlayer && lastSetField.southEast?.southEast?.player == lastPlayer

                || lastSetField.southWest?.player == lastPlayer && lastSetField.southWest?.southWest?.player == lastPlayer
                || lastSetField.southWest?.player == lastPlayer && lastSetField.northEast?.player == lastPlayer
                || lastSetField.northEast?.player == lastPlayer && lastSetField.northEast?.northEast?.player == lastPlayer

        if (win) {
            modelObj.winner = lastPlayer
            _ending.value = if(isOnTurn()) GameEnding.WIN else GameEnding.LOSE
        }
        else if (modelObj.player1.amountOfFields + modelObj.player2.amountOfFields >= TicTacToeModel.NUM_FIELDS) {
            _ending.value = GameEnding.DRAW
        }
    }

    fun giveUp() {
        modelObj.winner = modelObj.player2
        _ending.value = GameEnding.LOSE
    }

    fun isOnTurn(): Boolean {
        return modelObj.currentPlayer == modelObj.player1
    }

    override fun initGame(snap: DocumentSnapshot, docref: DocumentReference) {
        val gameID = docref.id
        val playerRefs = snap[GAME_PLAYERS] as List<DocumentReference>
        val beginnerIndex = snap.getLong(GAME_BEGINNER)?:0
        val isBeginner = playerRefs[beginnerIndex.toInt()].id == getOwnUserID()
        for (playerRef in playerRefs) {
            if (playerRef.id != getOwnUserID()) {
                playerRef.get().addOnSuccessListener { playerSnap ->
                    val opponentName = playerSnap.getString(USER_NAME)
                    _gameModel.apply {
                        value = TicTacToeModel.buildGame(gameID, "You", opponentName?:"")
                        value!!.currentPlayer = if (isBeginner) value!!.player1 else value!!.player2
                    }
                    val gameData = snap[GAME_DATA] as MutableList<Long>
                    for (fieldNum in gameData) {
                        move(fieldNum.toInt())
                    }
                    _showOnTurn.value = isOnTurn()
                    runGame.value = Game(beginnerIndex, gameData, GAME_TYPE_TIC_TAC_TOE, playerRefs)
                    docref.addSnapshotListener(this::onServerGameDataChanged)
                }
            }
        }
    }

    override fun onGameDataChanged(gameData: List<Long>) {
        if (isOnTurn()) {
            return
        }
        if (turnNumber >= gameData.size) {
            return
        }
        move(gameData[turnNumber].toInt())
        _showOnTurn.value = true
    }
}