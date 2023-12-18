package day18

import println
import readInput
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

data class Move(val dir: String, val steps: Int, val color: Int)

fun main() {
    fun part1(input: List<String>): Int {

        val moves = input.map {
            val s = it.split(" ")
            Move(s[0], s[1].toInt(), 0)
        }

        var leftMost = Int.MAX_VALUE
        var rightMost = Int.MIN_VALUE
        var topMost = Int.MAX_VALUE
        var bottomMost = Int.MIN_VALUE

        var x = 0
        var y = 0

        moves.forEach { move ->
            when (move.dir) {
                "U" -> y -= move.steps
                "D" -> y += move.steps
                "L" -> x -= move.steps
                "R" -> x += move.steps
            }
            if (x < leftMost) leftMost = x
            if (x > rightMost) rightMost = x
            if (y < topMost) topMost = y
            if (y > bottomMost) bottomMost = y
        }

        val isCcw = moves.windowed(2).sumOf { (m1, m2) ->
            when (m1.dir) {
                "U" -> when (m2.dir) {
                    "L" -> 1
                    "R" -> -1
                    else -> 0
                }

                "D" -> when (m2.dir) {
                    "L" -> -1
                    "R" -> 1
                    else -> 0
                }

                "L" -> when (m2.dir) {
                    "U" -> -1
                    "D" -> 1
                    else -> 0
                }

                "R" -> when (m2.dir) {
                    "U" -> 1
                    "D" -> -1
                    else -> 0
                }

                else -> 0
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
            Move(dir, it.steps, it.color)
        }

        "rectangle: $leftMost, $topMost, $rightMost, $bottomMost".println()
        val graphH = bottomMost - topMost + 1
        val graphW = rightMost - leftMost + 1

        val graph = TwoDimenGraph(List(graphH * graphW) { '.' }, graphW)

        var currentP = graph.createPosition(-topMost, -leftMost)

        val trench = mutableListOf<TwoDimenGraph<Char>.Position>()
        val diggedPositions = mutableSetOf<TwoDimenGraph<Char>.Position>()
        var diggingPositions = mutableSetOf<TwoDimenGraph<Char>.Position>()

        ccwMoves.forEach { move ->
            val nextP = when (move.dir) {
                "U" -> currentP.up(move.steps)
                "D" -> currentP.down(move.steps)
                "L" -> currentP.left(move.steps)
                "R" -> currentP.right(move.steps)
                else -> null
            } ?: throw Exception("Not gonna happen")

            currentP.createIntervalPositionsTo(nextP).forEach {
                trench.add(it)
                when (move.dir) {
                    "U" -> it.left()
                    "D" -> it.right()
                    "L" -> it.down()
                    "R" -> it.up()
                    else -> throw Exception("Not gonna happen")
                }?.let { seed -> diggingPositions.add(seed) }
            }

            currentP = nextP
        }

        diggingPositions.removeAll(trench.toSet())

        diggedPositions.addAll(trench)
        diggedPositions.addAll(diggingPositions)

        while (diggingPositions.isNotEmpty()) {
            val nextDigging = mutableSetOf<TwoDimenGraph<Char>.Position>()

            diggingPositions.forEach { position ->
                position.neighbors().forEach { neighbor ->
                    if (neighbor !in diggedPositions) {
                        nextDigging.add(neighbor)
                    }
                }
            }

            diggedPositions.addAll(nextDigging)

            diggingPositions = nextDigging.toMutableSet()
        }

        return diggedPositions.size
    }

    fun part2(input: List<String>): Long {
        return 1
    }

    check(part1(readInput("$FOLDER/test")) == 62)
    check(part2(readInput("$FOLDER/test")) == 952408144115L)

    val input = readInput("$FOLDER/input")
    val part1Result: Int
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