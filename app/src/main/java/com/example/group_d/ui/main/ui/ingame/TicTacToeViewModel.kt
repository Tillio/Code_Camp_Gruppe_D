package com.example.group_d.ui.main.ui.ingame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.example.group_d.GAME_DATA
import com.example.group_d.GAME_PLAYERS
import com.example.group_d.GAME_TYPE_TIC_TAC_TOE
import com.example.group_d.data.model.Game
import com.example.group_d.data.model.GameEnding
import com.example.group_d.data.model.User
import com.example.group_d.data.model.tictactoe.Field
import com.example.group_d.data.model.tictactoe.TicTacToeGame
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.random.Random

class TicTacToeViewModel(private val state: SavedStateHandle) : GameViewModel() {

    private val _gameModel = MutableLiveData<TicTacToeGame>()
    val gameModel: LiveData<TicTacToeGame> = _gameModel

    private val _nextField = MutableLiveData<Int>()
    val nextField: LiveData<Int> = _nextField

    private val _turnNumber = MutableLiveData<Int>()
    val turnNumber: LiveData<Int> = _turnNumber

    private val _ending = MutableLiveData<GameEnding>()
    val ending: LiveData<GameEnding> = _ending

    private val modelObj: TicTacToeGame get() = gameModel.value!!
    val opponentName: String
        get() = modelObj.player2.name

    fun getOpponentMove(): Int {
        // TODO load from server
        var rand = Random.nextInt(TicTacToeGame.NUM_FIELDS)
        while (!fieldIsEmpty(rand)) {
            rand = Random.nextInt(TicTacToeGame.NUM_FIELDS)
        }
        move(rand)
        return rand
    }

    fun fieldIsEmpty(fieldNum: Int): Boolean {
        return modelObj.fields[fieldNum].player == null
    }

    fun playerMove(fieldNum: Int) {
        runGameRaw.gameData.add(fieldNum.toLong())
        updateGameData()
        move(fieldNum)
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
        _turnNumber.value = _turnNumber.value!! + 1
    }

    private fun checkResult(lastSetField: Field) {
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
        else if (modelObj.player1.amountOfFields + modelObj.player2.amountOfFields >= TicTacToeGame.NUM_FIELDS) {
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

    override fun initGame(snap: DocumentSnapshot, gameID: String) {
        val players = snap[GAME_PLAYERS] as List<User>
        _gameModel.apply {
            value = TicTacToeGame.buildGame(gameID, "Erna", "Hans")
            value!!.currentPlayer = value!!.player1
        }
        _turnNumber.value = 0
        val gameData = snap[GAME_DATA] as MutableList<Long>
        for (fieldNum in gameData) {
            move(fieldNum.toInt())
        }
        runGame.value = Game(gameID, gameData, GAME_TYPE_TIC_TAC_TOE, players)
    }

    override fun onGameDataChanged(gameData: List<Long>) {
        if (isOnTurn()) {
            return
        }
        val turnNum = turnNumber.value!!
        if (turnNum >= gameData.size) {
            return
        }
        move(gameData[turnNum].toInt())
    }
}