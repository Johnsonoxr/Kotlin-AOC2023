package day18

import println
import readInput
import kotlin.system.exitProcess
import kotlin.system.measureNanoTime

private const val FOLDER = "day18"

class TwoDimenGraph<T>(graph: Collection<T>, val stride: Int) {

    inner class Position(val y: Int, val x: Int) {

        val info: MutableMap<String, Any> by lazy { mutableMapOf() }

        fun up(offset: Int = 1) = if (y - offset >= 0) Position(y - offset, x) else null
        fun down(offset: Int = 1) = if (y + offset < h) Position(y + offset, x) else null
        fun left(offset: Int = 1) = if (x - offset >= 0) Position(y, x - offset) else null
        fun right(offset: Int = 1) = if (x + offset < w) Position(y, x + offset) else null

        fun neighbors() = listOfNotNull(up(), down(), left(), right())

        fun createIntervalPositionsTo(other: Position): List<Position> {
            val positions = mutableListOf<Position>()
            var currentP = this
            while (currentP != other) {
                currentP = when {
                    other.y > currentP.y -> currentP.down()!!
                    other.y < currentP.y -> currentP.up()!!
                    other.x > currentP.x -> currentP.right()!!
                    other.x < currentP.x -> currentP.left()!!
                    else -> throw Exception("Not gonna happen")
                }
                positions.add(currentP)
            }
            return positions
        }

        override fun toString() = "P($y, $x)"

        override fun equals(other: Any?): Boolean {
            if (other !is TwoDimenGraph<*>.Position) return false
            return y == other.y && x == other.x
        }

        override fun hashCode(): Int {
            return stride * y + x
        }
    }

    fun createPosition(y: Int, x: Int, dir: String = "") = Position(y, x)

    val graph = graph.toMutableList()
    val w = stride
    val h = graph.size / stride

    operator fun get(y: Int, x: Int): T {
        return graph[y * stride + x]
    }

    operator fun get(position: Position): T {
        return get(position.y, position.x)
    }

    operator fun set(position: Position, value: T): Boolean {
        graph[position.y * stride + position.x] = value
        return true
    }

    override fun toString(): String {
        return graph.chunked(stride).joinToString("\n") { it.joinToString("") }
    }
}

data class Move(val dir: String, val steps: Long)

data class Position(val y: Long, val x: Long) {
    fun move(move: Move) = when (move.dir) {
        "U" -> Position(y - move.steps, x)
        "D" -> Position(y + move.steps, x)
        "L" -> Position(y, x - move.steps)
        "R" -> Position(y, x + move.steps)
        else -> throw Exception("Not gonna happen")
    }
}

val ccwTurns = mapOf(
    "U" to "L",
    "L" to "D",
    "D" to "R",
    "R" to "U"
)

fun main() {

    fun toCcwMoves(moves: List<Move>): List<Move> {


        val isCcw = moves.windowed(2).sumOf { (m1, m2) ->
            when (m2.dir) {
                ccwTurns[m1.dir] -> 1
                else -> -1
            }.toInt()
        } > 0

        val ccwMoves = if (isCcw) moves else moves.reversed().map {
            val dir = when (it.dir) {
                "U" -> "D"
                "D" -> "U"
                "L" -> "R"
                "R" -> "L"
                else -> throw Exception("Not gonna happen")
            }
            Move(dir, it.steps)
        }
        return ccwMoves
    }

    fun solve(moves: List<Move>): Long {

        data class Movement(val x: Long, val y: Long, val dir: String, val steps: Long) {
            fun rangeX(): LongRange = when (dir) {
                "L" -> x - steps..x
                "R" -> x..x + steps
                else -> throw Exception("Not gonna happen")
            }

            fun rangeY(): LongRange = when (dir) {
                "U" -> y - steps..y
                "D" -> y..y + steps
                else -> throw Exception("Not gonna happen")
            }
        }

        fun LongRange.isIntersect(other: LongRange): Boolean {
            return this.first <= other.last && this.last >= other.first
        }

        val ccwMoves = toCcwMoves(moves)

        val newMoves = mutableListOf<Move>()

        (ccwMoves.takeLast(1) + ccwMoves + ccwMoves.take(1)).windowed(3).forEach { (m1, m2, m3) ->

            val isTurn1Ccw = ccwTurns[m1.dir] == m2.dir
            val isTurn2Ccw = ccwTurns[m2.dir] == m3.dir
            val stepFix = when {
                isTurn1Ccw && isTurn2Ccw -> m2.steps + 1
                isTurn1Ccw || isTurn2Ccw -> m2.steps
                else -> m2.steps - 1
            }

            newMoves.add(Move(m2.dir, stepFix))
        }

        val movePosition = mutableListOf(Position(0, 0))
        newMoves.dropLast(1).forEach { move ->
            movePosition.add(movePosition.last().move(move))
        }

        val movements = newMoves.zip(movePosition).map { (move, position) ->
            Movement(position.x, position.y, move.dir, move.steps)
        }

        movements.forEach { it.println() }
        exitProcess(0)

        data class Rectangle(val left: Long, val top: Long, val right: Long, val bottom: Long) {
            val area = (right - left + 1) * (bottom - top + 1)
        }

        val rectangles = mutableSetOf<Rectangle>()

        var closedMovementsList = mutableListOf(movements)

        while (closedMovementsList.isNotEmpty()) {
            val newClosedList = closedMovementsList.drop(1).toMutableList()

            val firstMovements = closedMovementsList.first()
            val theMovement = firstMovements.filter { it.dir == "U" }.maxBy { it.y }

            val rectLeft = firstMovements.filter { m ->
                m.dir == "D" && m.x < theMovement.x && m.rangeY().isIntersect(theMovement.rangeY())
            }.maxOf { it.x }

            val rect = Rectangle(rectLeft, theMovement.y - theMovement.steps, theMovement.x, theMovement.y)
            rectangles.add(rect)

            var newClosedMovements: MutableList<Movement> = mutableListOf()
            firstMovements.forEach { m ->
                if (m.dir == "D" && m.rangeY().isIntersect(theMovement.rangeY())) {
                    newClosedMovements.dropLast(1)
                }
            }

            closedMovementsList = newClosedList
        }

        return rectangles.sumOf { it.area }
    }

    fun part1(input: List<String>): Long {

        val moves = input.map {
            val s = it.split(" ")
            Move(s[0], s[1].toLong())
        }

        return solve(moves)
    }

    fun part2(input: List<String>): Long {

        val moves = input.map { line ->
            val s = "#(\\w+)".toRegex().find(line)!!.groupValues.last()
            val dir = when (s.last()) {
                '0' -> "R"
                '1' -> "D"
                '2' -> "L"
                '3' -> "U"
                else -> throw Exception("Not gonna happen")
            }
            val steps = s.dropLast(1).toLong(16)
            Move(dir, steps)
        }

        return solve(moves)
    }

    check(part1(readInput("$FOLDER/test")) == 62L)
    check(part2(readInput("$FOLDER/test")) == 952408144115L)

    val input = readInput("$FOLDER/input")
    val part1Result: Long
    val part1Time = measureNanoTime {
        part1Result = part1(input)
    }
    val part2Result: Long
    val part2Time = measureNanoTime {
        part2Result = part2(input)
    }

    println("Part 1 result: $part1Result")
    println("Part 2 result: $part2Result")
    println("Part 1 takes ${part1Time / 1e6f} milliseconds.")
    println("Part 2 takes ${part2Time / 1e6f} milliseconds.")
}