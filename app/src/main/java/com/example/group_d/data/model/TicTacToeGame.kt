package com.example.group_d.data.model.tictactoe

class TicTacToeGame(val gameID: Int) {

    companion object {
        const val NUM_COLUMNS = 3
        const val NUM_ROWS = 3
        const val NUM_FIELDS = NUM_COLUMNS * NUM_ROWS

        fun buildGame(gameID: Int, player1Name: String, player2Name: String): TicTacToeGame {
            return TicTacToeGame(gameID).apply {
                player1 = Player(player1Name)
                player2 = Player(player2Name)
                for ((i, field) in fields.withIndex()) {
                    if (i < NUM_FIELDS - NUM_COLUMNS) {
                        field.south = fields[i + NUM_COLUMNS]
                    }
                    if (i % NUM_COLUMNS != 0) {
                        field.west = fields[i - 1]
                        if (i >= NUM_COLUMNS) {
                            field.northWest = fields[i - NUM_COLUMNS - 1]
                        }
                        if (i < NUM_FIELDS - NUM_COLUMNS) {
                            field.southWest = fields[i + NUM_COLUMNS - 1]
                        }
                    }
                }
            }
        }
    }

    val fields: Array<Field> = Array(NUM_FIELDS) { i -> Field(i) }
    lateinit var player1: Player
    lateinit var player2: Player
    lateinit var currentPlayer: Player
    var winner: Player? = null
    var result: Result? = null
}

enum class Result {
    DRAW,
    WIN
}

class Player(val name: String) {

    private val fields: MutableList<Field> = ArrayList()
    val amountOfFields get() = fields.size

    fun getField(index: Int): Field {
        return fields[index]
    }

    fun withFields(field: Field): Player {
        if (field !in fields) {
            fields.add(field)
            field.player = this
        }
        return this
    }

    fun withOutFields(field: Field): Player {
        if (fields.remove(field)) {
            field.player = null
        }
        return this
    }
}

class Field(val num: Int) {

    var player: Player? = null
        set(value) {
            if (field == value) {
                return
            }
            field?.withOutFields(this)
            field = value
            value?.withFields(this)
        }

    var west: Field? = null
        set(value) {
            if (field == value) {
                return
            }
            field?.east = null
            field = value
            value?.east = this
        }

    var northWest: Field? = null
        set(value) {
            if (field == value) {
                return
            }
            field?.southEast = null
            field = value
            value?.southEast = this
        }

    var north: Field? = null
        set(value) {
            if (field == value) {
                return
            }
            field?.south = null
            field = value
            value?.south = this
        }

    var northEast: Field? = null
        set(value) {
            if (field == value) {
                return
            }
            field?.southWest = null
            field = value
            value?.southWest = this
        }

    var east: Field? = null
        set(value) {
            if (field == value) {
                return
            }
            field?.west = null
            field = value
            value?.west = this
        }

    var southEast: Field? = null
        set(value) {
            if (field == value) {
                return
            }
            field?.northWest = null
            field = value
            value?.northWest = this
        }

    var south: Field? = null
        set(value) {
            if (field == value) {
                return
            }
            field?.north = null
            field = value
            value?.north = this
        }

    var southWest: Field? = null
        set(value) {
            if (field == value) {
                return
            }
            field?.northEast = null
            field = value
            value?.northEast = this
        }
}
