package com.example.group_d.ui.main.ui.ingame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.group_d.data.model.GameEnding
import com.example.group_d.data.model.tictactoe.Field
import com.example.group_d.data.model.tictactoe.TicTacToeGame
import kotlin.random.Random

class TicTacToeViewModel(private val state: SavedStateHandle) : ViewModel() {

    private val _game = MutableLiveData<TicTacToeGame>()
    val game: LiveData<TicTacToeGame> = _game

    private val _nextField = MutableLiveData<Int>()
    val nextField: LiveData<Int> = _nextField

    private val _isOnTurn = MutableLiveData<Boolean>()
    val isOnTurn: LiveData<Boolean> = _isOnTurn

    private val _ending = MutableLiveData<GameEnding>()
    val ending: LiveData<GameEnding> = _ending

    private val gameObj: TicTacToeGame get() = game.value!!
    val opponentName: String
        get() = gameObj.player2.name

    fun loadGame(gameID: String) {
        // TODO load from repository
        _game.apply {
            value = TicTacToeGame.buildGame(gameID, "Erna", "Hans")
            value!!.currentPlayer = value!!.player1
            _isOnTurn.value = true
        }
    }

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
        return gameObj.fields[fieldNum].player == null
    }

    fun move(fieldNum: Int) {
        val field = gameObj.fields[fieldNum]
        field.player = gameObj.currentPlayer
        _nextField.value = fieldNum
        checkResult(field)
        nextPlayer()
    }

    private fun nextPlayer() {
        gameObj.currentPlayer = gameObj.currentPlayer.next!!
        _isOnTurn.value = !_isOnTurn.value!!
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

        if (win && isOnTurn.value!!) {
            gameObj.winner = lastPlayer
            _ending.value = GameEnding.WIN
        }
        if (gameObj.player1.amountOfFields + gameObj.player2.amountOfFields >= TicTacToeGame.NUM_FIELDS) {
            _ending.value = GameEnding.DRAW
        }
    }

    fun giveUp() {
        gameObj.winner = gameObj.player2
        _ending.value = GameEnding.LOSE
    }

    fun isOnTurn(): Boolean {
        return gameObj.currentPlayer == gameObj.player1
    }
}