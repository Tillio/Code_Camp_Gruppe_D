package com.example.group_d.ui.main.ui.ingame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.group_d.data.model.tictactoe.TicTacToeGame
import kotlin.random.Random

class TicTacToeViewModel(private val state: SavedStateHandle) : ViewModel() {

    private val _game = MutableLiveData<TicTacToeGame>()
    val game: LiveData<TicTacToeGame> = _game
    val opponentName: String
        get() = game.value!!.player2.name

    fun loadGame(gameID: Int) {
        // TODO load from repository
        _game.apply {
            value = TicTacToeGame.buildGame("Erna", "Hans")
        }
    }

    fun getOpponentMove(): Int {
        // TODO load from repository
        var rand = Random.nextInt(TicTacToeGame.NUM_FIELDS)
        while (!fieldIsEmpty(rand)) {
            rand = Random.nextInt(TicTacToeGame.NUM_FIELDS)
        }
        return rand
    }

    fun fieldIsEmpty(fieldNum: Int): Boolean {
        return game.value!!.fields[fieldNum].player == null
    }

    fun move(fieldNum: Int) {
        game.value!!.fields[fieldNum].player = game.value!!.player1
    }

    fun moveOpponent(fieldNum: Int) {
        game.value!!.fields[fieldNum].player = game.value!!.player2
    }

    fun checkWin(fieldNum: Int): Boolean {
        val field = _game.value!!.fields[fieldNum]
        val lastPlayer = field.player!!

        return field.west?.player == lastPlayer && field.west?.west?.player == lastPlayer
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
    }
}