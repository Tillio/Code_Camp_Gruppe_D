package com.example.group_d.data.model

class TicTacToeModel {

    companion object {
        const val NUM_COLUMNS = 3
        const val NUM_ROWS = 3
        const val NUM_FIELDS = NUM_COLUMNS * NUM_ROWS

        // Factory function for TicTacToeModel
        fun buildGame(player1Name: String, player2Name: String): TicTacToeModel {
            return TicTacToeModel().apply {
                // Generate player objects
                player1 = Player(player1Name)
                player2 = Player(player2Name)
                player2.previous = player1
                player2.next = player1
                for ((i, field) in fields.withIndex()) {
                    // Connect field with its neighbor fields
                    if (i < NUM_FIELDS - NUM_COLUMNS) {
                        // We aren't in the last row -> connect with the south field
                        field.south = fields[i + NUM_COLUMNS]
                    }
                    if (i % NUM_COLUMNS != 0) {
                        // We aren't in the first column -> connect with the west field
                        field.west = fields[i - 1]
                        if (i >= NUM_COLUMNS) {
                            // We aren't in the first row -> connect with the northwest field
                            field.northWest = fields[i - NUM_COLUMNS - 1]
                        }
                        if (i < NUM_FIELDS - NUM_COLUMNS) {
                            // We aren't in the last row -> connect with the southwest field
                            field.southWest = fields[i + NUM_COLUMNS - 1]
                        }
                    }
                }
            }
        }
    }

    // Model classes with referential integrity
    class Player(val name: String) {

        private val fields: MutableList<Field> = ArrayList()
        val amountOfFields get() = fields.size

        var previous: Player? = null
            set(value) {
                if (field == value) {
                    return
                }
                field?.next = null
                field = value
                value?.next = this
            }

        var next: Player? = null
            set(value) {
                if (field == value) {
                    return
                }
                field?.previous = null
                field = value
                value?.previous = this
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

    class Field {

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

    val fields: Array<Field> = Array(NUM_FIELDS) { Field() }
    lateinit var player1: Player
    lateinit var player2: Player
    lateinit var currentPlayer: Player
    var winner: Player? = null
}
