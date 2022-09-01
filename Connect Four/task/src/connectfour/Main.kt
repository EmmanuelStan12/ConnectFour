package connectfour

const val VERTICAL_LINE = '║'
const val HORIZONTAL_LINE = '═'
const val LEFT_BOTTOM_LINE = '╚'
const val RIGHT_BOTTOM_LINE = '╝'
const val MIDDLE_BOTTOM_LINE = '╩'
const val PLAYER_ONE_CHAR = "o"
const val PLAYER_TWO_CHAR = "*"
const val DEFAULT_ROWS = 6
const val DEFAULT_COLUMNS = 7
const val STARS = "****"
const val CIRCLES = "oooo"
const val ROW = "ROW"
const val COLUMN = "COLUMN"

typealias IS_GAME_OVER = Boolean
typealias IS_DRAW = Boolean
typealias CURRENT_PLAYER = String

fun checkInitInput(): Int {
    println("Do you want to play single or multiple games?")
    println("For a single game, input 1 or press Enter")
    println("Input a number of games:")
    val input = readln()
    val regex = Regex("\\d+")
    return if (input.isEmpty()) {
        1
    } else if (regex.matches(input)) {
        val data = input.toInt()
        if (data < 1) {
            println("Invalid input")
            checkInitInput()
        } else {
            data
        }
    } else {
        println("Invalid input")
        checkInitInput()
    }
}

fun checkBoardDimensions(firstPlayer: String, secondPlayer: String): MutableMap<String, Int> {
    val dimensions = mutableMapOf<String, Int>(
            ROW to DEFAULT_ROWS,
            COLUMN to DEFAULT_COLUMNS
    )
    println("Set the board dimensions (Rows x Columns)")
    println("Press Enter for default ($DEFAULT_ROWS x $DEFAULT_COLUMNS) board")
    val input = readln()
    if (input.isEmpty()) {
        return dimensions
    } else {
        val regex = Regex("""\s*(\d)+\s*[xX]\s*(\d)+\s*""")
        if (regex.matches(input)) {
            val data = regex.find(input)?.destructured?.toList()?.map { it.toInt() }
            val rows = data?.get(0)!!
            val columns = data[1]
            if (rows !in 5..9) {
                // Ask Again
                println("Board rows should be from 5 to 9")
                return checkBoardDimensions(firstPlayer, secondPlayer)
            }
            if (columns !in 5..9) {
                // Ask Again
                println("Board columns should be from 5 to 9")
                return checkBoardDimensions(firstPlayer, secondPlayer)
            }
            dimensions[ROW] = rows
            dimensions[COLUMN] = columns
            return dimensions
        } else {
            println("Invalid input")
            return checkBoardDimensions(firstPlayer, secondPlayer)
        }
    }
}

fun main() {
    println("Connect Four")
    println("First player's name:")
    var firstPlayer = readln()
    println("Second player's name:")
    var secondPlayer = readln()
    val constFirstPlayer = firstPlayer
    val constSecondPlayer = secondPlayer
    val dimensions = checkBoardDimensions(firstPlayer, secondPlayer)
    val timesToPlay = checkInitInput()
    val rows = dimensions[ROW] ?: 0
    var firstPlayerScore = 0
    var secondPlayerScore = 0
    val columns = dimensions[COLUMN] ?: 0
    println("$firstPlayer VS $secondPlayer")
    println("$rows X $columns board")
    if (timesToPlay == 1) {
        println("Single game")
        drawBoard(rows, columns)
        startGame(constFirstPlayer, constSecondPlayer, firstPlayer, secondPlayer, rows, columns)
        println("Game over!")
    } else {
        println("Total $timesToPlay games")
        loop@ for (i in 1..timesToPlay) {
            println("Game #$i")
            drawBoard(rows, columns)
            val (isGameOver, isDraw, winner) =
                    startGame(constFirstPlayer, constSecondPlayer, firstPlayer, secondPlayer, rows, columns)
            if (isGameOver && !isDraw) {
                if (winner == constFirstPlayer) {
                    firstPlayerScore += 2
                    val temp = firstPlayer
                    firstPlayer = secondPlayer
                    secondPlayer = temp
                }
                else if (winner == constSecondPlayer) {
                    secondPlayerScore += 2
                    val temp = secondPlayer
                    secondPlayer = firstPlayer
                    firstPlayer = temp
                }
            } else if (isGameOver && isDraw) {
                firstPlayerScore++
                secondPlayerScore++
                if (winner == constFirstPlayer) {
                    val temp = firstPlayer
                    firstPlayer = secondPlayer
                    secondPlayer = temp
                }
                else if (winner == constSecondPlayer) {
                    val temp = secondPlayer
                    secondPlayer = firstPlayer
                    firstPlayer = temp
                }
            }
            if (isGameOver && winner == null) {
                break@loop
            }
            println("Score")
            println("$constFirstPlayer: $firstPlayerScore $constSecondPlayer: $secondPlayerScore")
        }
        println("Game over!")
    }
}

fun drawBoard(
        rows: Int,
        columns: Int,
) {
    val header = (1..columns).joinToString(separator = " ", prefix = " ")
    println(header)
    for (i in 0 until rows) {
        for (j in 1..columns + 1) {
            print(
                    "$VERTICAL_LINE "
            )
        }
        println()
    }
    for (j in 1..columns + 1) {
        when (j) {
            1 -> {
                print(LEFT_BOTTOM_LINE)
            }

            columns + 1 -> {
                print(RIGHT_BOTTOM_LINE)
            }

            else -> {
                print(MIDDLE_BOTTOM_LINE)
            }
        }
        if (j != columns + 1)
            print(HORIZONTAL_LINE)
    }
    println()
}

fun startGame(
        constFirstPlayer: String,
        constSecondPlayer: String,
        firstPlayer: String,
        secondPlayer: String,
        rows: Int,
        columns: Int
): Triple<IS_GAME_OVER, IS_DRAW, CURRENT_PLAYER?> {
    val gameBoard = mutableListOf<MutableList<String>>()
    for (i in 1..rows) {
        gameBoard.add(mutableListOf())
        for (j in 1..(columns * 2 + 1)) {
            if (j % 2 == 0) {
                gameBoard[i - 1].add(" ")
            } else {
                gameBoard[i - 1].add("$VERTICAL_LINE")
            }
        }
    }
    val regex = Regex("\\d+")
    while (true) {
        val fChar = if (firstPlayer == constFirstPlayer) PLAYER_ONE_CHAR else PLAYER_TWO_CHAR
        val sChar = if (secondPlayer == constSecondPlayer) PLAYER_TWO_CHAR else PLAYER_ONE_CHAR
        val p1Result = analyzePlayerInput(regex, firstPlayer, gameBoard, fChar)
        if (p1Result.first) {
            return p1Result
        }
        //println("First: $firstPlayer == $constFirstPlayer Second: $secondPlayer == $constSecondPlayer")
        val p2Result = analyzePlayerInput(regex, secondPlayer, gameBoard, sChar)
        if (p2Result.first) return p2Result
    }
}

fun analyzePlayerInput(
        regex: Regex,
        player: String,
        gameBoard: MutableList<MutableList<String>>,
        char: String
): Triple<IS_GAME_OVER, IS_DRAW, CURRENT_PLAYER?> {
    println("$player's turn:")
    val columns = (gameBoard[0].size - 1) / 2
    val playerNumber = readln()
    if (regex.matches(playerNumber)) {
        val data = playerNumber.toInt()
        val column = data * 2 - 1
        if (column !in 1 until gameBoard[0].size) {
            println("The column number is out of range (1 - $columns)")
            return analyzePlayerInput(
                    regex, player, gameBoard, char
            )
        } else {
            var entered = false
            loop@ for (row in gameBoard.reversed()) {
                if (row[column] == " ") {
                    row[column] = char
                    printBoard(gameBoard)
                    val p1Result = checkBoard(
                            gameBoard,
                            player,
                            if (STARS.contains(char)) STARS else CIRCLES)
                    if (p1Result.first) {
                        return p1Result
                    }
                    val p2Result = checkBoard(
                            reverseBoard(gameBoard),
                            player,
                            if (STARS.contains(char)) STARS else CIRCLES
                    )
                    if (p2Result.first) {
                        return p2Result
                    }
                    entered = true
                    break@loop
                }
            }
            if (!entered) {
                println("Column $data is full")
                return analyzePlayerInput(regex, player, gameBoard, char)
            }
        }
    } else if (playerNumber == "end") {
        return Triple(true, false, null)
    } else {
        println("Incorrect column number")
        return analyzePlayerInput(regex, player, gameBoard, char)
    }
    return Triple(false, false, null)
}

fun printBoard(board: MutableList<MutableList<String>>) {
    val columns = board[0].size / 2
    val header = (1..columns).joinToString(separator = " ", prefix = " ")
    println(header)
    for (row in board) {
        println(row.joinToString(""))
    }

    for (j in 1..columns + 1) {
        when (j) {
            1 -> {
                print(LEFT_BOTTOM_LINE)
            }

            columns + 1 -> {
                print(RIGHT_BOTTOM_LINE)
            }

            else -> {
                print(MIDDLE_BOTTOM_LINE)
            }
        }
        if (j != columns + 1)
            print(HORIZONTAL_LINE)
    }
    println()
}

fun isBoardFull(board: MutableList<MutableList<String>>): Boolean {
    for (row in board) {
        if (row.joinToString("").contains(" "))
            return false
    }
    return true
}

fun checkBoard(
        board: MutableList<MutableList<String>>,
        player: String,
        checks: String
): Triple<IS_GAME_OVER, IS_DRAW, CURRENT_PLAYER> {
    val checkBoard = board.toMutableList()
    val columnSize = board[0].size
    var rows = ""
    var columns = ""
    for (row in checkBoard) {
        for (cell in row) {
            if (cell != VERTICAL_LINE.toString())
                rows += cell
        }
    }
    for (i in 0 until columnSize) {
        for (row in checkBoard) {
            if (row[i] != VERTICAL_LINE.toString())
                columns += row[i]
        }
    }
    if (columns.contains(checks)) {
        //println("cols")
        println("Player $player won")
        return Triple(true, false, player)
    }
    if (rows.contains(checks)) {
        //println("rows")
        println("Player $player won")
        return Triple(true, false, player)
    }
    val result = evaluateDiagonals(board.size, (board[0].size - 1) / 2, rows)
    if (result) {
        println("Player $player won")
        return Triple(true, false, player)
    }
    if (isBoardFull(board)) {
        println("It is a draw")
        return Triple(true, true, player)
    }
    return Triple(false, false, player)
}

fun evaluateDiagonals(r_no: Int, c_no: Int, rows: String): Boolean {
    var prev = ""
    var count = 0
    for (i in 0 until c_no) {
        for (j in i until r_no) {
            val n = j * c_no + j
            //println("i: $i, j: $j, n: $n, char: ${rows[n]}")
            if (prev.isEmpty()) {
                prev = rows[n].toString()
                count = 1
                continue
            } else if (rows[n].toString() == " ") {
                count = 0
                prev = ""
                continue
            } else if (prev == rows[n].toString() && rows[n].toString() != " ") {
                count++
            } else if (prev != rows[n].toString()) {
                prev = rows[n].toString()
                count = 1
            }
            if (count == 4) {
                return true
            }
        }
        count = 0
        prev = ""
    }
    return checkColumns(r_no, c_no, rows)
}

fun checkColumns(r_no: Int, c_no: Int, columns: String): Boolean {
    var prev = ""
    var count = 0
    loop@ for (i in 0 until r_no) {
        var j = 0
        while (j < c_no && j < r_no) {
            val n = (j * c_no + j) + (c_no * i)
            if (n >= columns.length) {
                count = 0
                prev = ""
                continue@loop
            }
            if (prev.isEmpty()) {
                prev = columns[n].toString()
                count = 1
                j++
                continue
            } else if (columns[n].toString() == " ") {
                count = 0
                prev = ""
                j++
                continue
            } else if (prev == columns[n].toString() && columns[n].toString() != " ") {
                count++
            } else if (prev != columns[n].toString()) {
                prev = columns[n].toString()
                count = 1
            }
            if (count == 4) {
                return true
            }
            j++
        }
        prev = ""
        count = 0
    }
    return false
}

fun reverseBoard(board: MutableList<MutableList<String>>): MutableList<MutableList<String>> {
    val checkBoard = mutableListOf<MutableList<String>>()
    for (row in board) {
        checkBoard.add(row.reversed().toMutableList())
    }
    return checkBoard
}

infix operator fun String.times(t: Int): String {
    var result = ""
    for (i in 1..t) {
        result += this
    }
    return result
}
