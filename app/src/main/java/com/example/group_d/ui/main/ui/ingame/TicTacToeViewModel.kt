package com.example.group_d.ui.main.ui.ingame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.group_d.data.model.tictactoe.Result
import com.example.group_d.data.model.tictactoe.TicTacToeGame
import kotlin.random.Random

class TicTacToeViewModel(private val state: SavedStateHandle) : ViewModel() {

    private val _game = MutableLiveData<TicTacToeGame>()
    val game: LiveData<TicTacToeGame> = _game
    private val gameObj: TicTacToeGame get() = game.value!!
    val opponentName: String
        get() = gameObj.player2.name

    fun loadGame(gameID: Int) {
        // TODO load from repository
        _game.apply {
            value = TicTacToeGame.buildGame(gameID, "Erna", "Hans")
            value!!.currentPlayer = value!!.player1
        }
    }

    fun getOpponentMove(): Int {
        // TODO load from server
        var rand = Random.nextInt(TicTacToeGame.NUM_FIELDS)
        while (!fieldIsEmpty(rand)) {
            rand = Random.nextInt(TicTacToeGame.NUM_FIELDS)
        }
        moveOpponent(rand)
        return rand
    }

    fun fieldIsEmpty(fieldNum: Int): Boolean {
        return gameObj.fields[fieldNum].player == null
    }

    fun move(fieldNum: Int) {
        gameObj.fields[fieldNum].player = gameObj.player1
    }

    fun moveOpponent(fieldNum: Int) {
        gameObj.fields[fieldNum].player = gameObj.player2
    }

    fun checkResult(lastSetFieldNum: Int): Boolean? {
        val field = gameObj.fields[lastSetFieldNum]
        val lastPlayer = field.player!!

        val win = field.west?.player == lastPlayer && field.west?.west?.player == lastPlayer
                || field.west?.player == lastPlayer && field.east?.player == lastPlayer
                || field.east?.player == lastPlayer && field.east?.east?.player == lastPlayer

                || field.north?.player == lastPlayer && field.north?.north?.player == lastPlayer
                || field.north?.player == lastPlayer && field.south?.player == lastPlayer
                || field.south?.player == lastPlayer && field.south?.south?.player == lastPlayer

                || field.northWest?.player == lastPlayer && field.northWest?.northWest?.player == lastPlayer
                || field.northWest?.player == lastPlayer && field.southEast?.player == lastPlayer
                || field.southEast?.player == lastPlayer && field.southEast?.southEast?.player == lastPlayer

                || field.southWest?.player == lastPlayer && field.southWest?.southWest?.player == lastPlayer
                || field.southWest?.player == lastPlayer && field.northEast?.player == lastPlayer
                || field.northEast?.player == lastPlayer && field.northEast?.northEast?.player == lastPlayer

        if (win) {
            gameObj.winner = lastPlayer
            gameObj.result = Result.WIN
            return true
        }
        if (gameObj.player1.amountOfFields + gameObj.player2.amountOfFields >= TicTacToeGame.NUM_FIELDS) {
            gameObj.result = Result.DRAW
            return false
        }
        return null
    }

    fun giveUp() {
        gameObj.winner = gameObj.player2
    }

    fun isOnTurn(): Boolean {
        return gameObj.currentPlayer == gameObj.player1
    }

    fun isOwnField(fieldNum: Int): Boolean? {
        return when (gameObj.fields[fieldNum].player) {
            gameObj.player1 -> true
            gameObj.player2 -> false
            else -> null
        }
    }
}