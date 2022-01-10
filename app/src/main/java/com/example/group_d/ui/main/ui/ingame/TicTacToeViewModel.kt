package com.example.group_d.ui.main.ui.ingame

import androidx.lifecycle.*
import com.example.group_d.data.model.tictactoe.Player
import com.example.group_d.data.model.tictactoe.TicTacToeGame

class TicTacToeViewModel(private val state: SavedStateHandle) : ViewModel() {

    private val _game = MutableLiveData<TicTacToeGame>()
    val game: LiveData<TicTacToeGame> = _game
    val opponentName: String
        get() = _game.value!!.player2.name

    fun loadGame(gameID: Int) {
        // TODO load from repository
        _game.apply {
            value = TicTacToeGame(Player("Erna"), Player("Hans"))
        }
    }

    fun fieldIsEmpty(fieldNum: Int): Boolean {
        return _game.value!!.fields[fieldNum].player == null
    }

    fun move(fieldNum: Int) {
        _game.value!!.fields[fieldNum].player = game.value!!.player1
    }

    fun checkWin(fieldNum: Int): Boolean {
        TODO("Not implemented")
    }
}