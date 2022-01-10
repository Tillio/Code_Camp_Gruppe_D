package com.example.group_d.data.model.tictactoe

data class TicTacToeGame(
    val player1: Player,
    val player2: Player
) {
    companion object {
        const val NUM_FIELDS = 9
    }

    val fields: Array<Field> = Array(NUM_FIELDS) { i -> Field(i) }
    var currentPlayer: Player = player1
}

data class Player(
    val name: String
)

data class Field(
    val num: Int
) {
    var player: Player? = null
    var left: Field? = null
        set(value) {
            field?.right = null
            field = value
            value?.right = this
        }
    var top: Field? = null
        set(value) {
            field?.bottom = null
            field = value
            value?.bottom = this
        }
    var right: Field? = null
        set(value) {
            field?.left = null
            field = value
            value?.left = this
        }
    var bottom: Field? = null
        set(value) {
            field?.top = null
            field = value
            value?.top = this
        }
}
