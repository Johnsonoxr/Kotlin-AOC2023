package day18

import readInput
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureNanoTime

private const val FOLDER = "day18"

data class Move(val dir: String, val steps: Long)

data class Position(val y: Long, val x: Long) {
    fun move(move: Move) = when (move.dir) {
        "U" -> Position(y - move.steps, x)
        "D" -> Position(y + move.steps, x)
        "L" -> Position(y, x - move.steps)
        "R" -> Position(y, x + move.steps)
        else -> throw Exception("Not gonna happen")
    }

    fun isInside(rectangle: Rectangle): Boolean {
        return x in rectangle.left + 1..<rectangle.right && y in rectangle.top + 1..<rectangle.bottom
    }

    operator fun plus(other: Position) = Position(y + other.y, x + other.x)
    operator fun minus(other: Position) = Position(y - other.y, x - other.x)
}

data class Edge(val from: Position, val to: Position) {
    val length = abs(from.x - to.x) + abs(from.y - to.y)
}

data class Rectangle(val left: Long, val top: Long, val right: Long, val bottom: Long) {
    val area = (right - left) * (bottom - top)
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

    fun toCcwEdges(ccwMoves: List<Move>): List<Edge> {
        val edges = mutableListOf<Edge>()

        var startP = Position(0, 0)
        (ccwMoves.takeLast(1) + ccwMoves + ccwMoves.take(1)).windowed(3).forEach { (m1, m2, m3) ->
            val isTurn1Ccw = ccwTurns[m1.dir] == m2.dir
            val isTurn2Ccw = ccwTurns[m2.dir] == m3.dir
            val edgeLength = when {
                isTurn1Ccw && isTurn2Ccw -> m2.steps + 1
                isTurn1Ccw || isTurn2Ccw -> m2.steps
                else -> m2.steps - 1
            }
            val endP = startP.move(Move(m2.dir, edgeLength))
            edges.add(Edge(startP, endP))
            startP = endP
        }

        return edges
    }

    fun solve(moves: List<Move>): Long {

        val ccwMoves = toCcwMoves(moves)
        val edges = toCcwEdges(ccwMoves).toMutableList()

        val rectangles = mutableListOf<Rectangle>()

        fun Edge.isCross(rectangle: Rectangle): Boolean {
            return when {
                from.x == to.x -> {
                    val edgeX = from.x
                    val edgeTop = min(from.y, to.y)
                    val edgeBottom = max(from.y, to.y)
                    edgeX in rectangle.left + 1..<rectangle.right && (edgeTop < rectangle.bottom && edgeBottom > rectangle.top)
                }
                from.y == to.y -> {
                    val edgeY = from.y
                    val edgeLeft = min(from.x, to.x)
                    val edgeRight = max(from.x, to.x)
                    edgeY in rectangle.top + 1..<rectangle.bottom && (edgeLeft < rectangle.right && edgeRight > rectangle.left)
                }
                else -> throw Exception("Not gonna happen")
            }
        }

        while (edges.isNotEmpty()) {

            val zeroLengthEdges = edges.firstOrNull { it.length == 0L }
            if (zeroLengthEdges != null) {
                val idx = edges.indexOf(zeroLengthEdges)
                val edgeBefore = edges.getOrNull(idx - 1) ?: edges.last()
                val edgeAfter = edges.getOrNull(idx + 1) ?: edges.first()

                val edgeMerged = Edge(edgeBefore.from, edgeAfter.to)
                edges.add(idx, edgeMerged)
                edges.remove(zeroLengthEdges)
                edges.remove(edgeBefore)
                edges.remove(edgeAfter)

                continue
            }

            for ((e0, e1, e2, e3, e4) in (edges + edges.take(4)).windowed(5)) {

                val offset1 = e1.to - e1.from
                val offset2 = e2.to - e2.from
                val offset3 = e3.to - e3.from

                // Check if the edges goes in counter-clockwise direction
                if (offset1.y < 0 && offset2.x > 0  // U -> R
                    || offset1.x < 0 && offset2.y < 0   // L -> U
                    || offset1.y > 0 && offset2.x < 0   // D -> L
                    || offset1.x > 0 && offset2.y > 0   // R -> D
                ) {
                    continue
                }

                // Check if the edges goes in counter-clockwise direction
                if (offset2.y < 0 && offset3.x > 0  // U -> R
                    || offset2.x < 0 && offset3.y < 0   // L -> U
                    || offset2.y > 0 && offset3.x < 0   // D -> L
                    || offset2.x > 0 && offset3.y > 0   // R -> D
                ) {
                    continue
                }

                val rectangleCornerPair = when {
                    e1.length < e3.length -> Pair(e1.from, e2.to)
                    else -> Pair(e2.from, e3.to)
                }

                val rectangle = Rectangle(
                    min(rectangleCornerPair.first.x, rectangleCornerPair.second.x),
                    min(rectangleCornerPair.first.y, rectangleCornerPair.second.y),
                    max(rectangleCornerPair.first.x, rectangleCornerPair.second.x),
                    max(rectangleCornerPair.first.y, rectangleCornerPair.second.y)
                )

                if (edges.any { it.isCross(rectangle) }) {
                    continue
                }

                rectangles.add(rectangle)

                val insertIdx = edges.indexOf(e2)
                when {
                    e1.length < e3.length -> {
                        val newEdge = Edge(e0.from, e2.to + e1.from - e1.to)
                        val newEdge2 = Edge(newEdge.to, e4.from)
                        edges.add(insertIdx, newEdge2)
                        edges.add(insertIdx, newEdge)
                        edges.remove(e2)
                        edges.remove(e0)
                        edges.remove(e1)
                        edges.remove(e3)
                    }

                    e1.length > e3.length -> {
                        val newEdge = Edge(e2.from + e3.to - e3.from, e4.to)
                        val newEdge2 = Edge(e0.to, newEdge.from)
                        edges.add(insertIdx, newEdge)
                        edges.add(insertIdx, newEdge2)
                        edges.remove(e2)
                        edges.remove(e1)
                        edges.remove(e3)
                        edges.remove(e4)
                    }

                    else -> {
                        val newEdge = Edge(e0.from, e4.to)
                        edges.add(insertIdx, newEdge)
                        edges.remove(e2)
                        edges.remove(e0)
                        edges.remove(e1)
                        edges.remove(e3)
                        edges.remove(e4)
                    }
                }

                break
            }
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