package day17

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day17"

class TwoDimenGraph<T>(graph: Collection<T>, val stride: Int) {

    inner class Position(val y: Int, val x: Int, val dir: String = "") {

        val info: MutableMap<String, Any> by lazy { mutableMapOf() }

        fun up(offset: Int = 1) = if (y - offset >= 0) Position(y - offset, x, "U") else null
        fun down(offset: Int = 1) = if (y + offset < h) Position(y + offset, x, "D") else null
        fun left(offset: Int = 1) = if (x - offset >= 0) Position(y, x - offset, "L") else null
        fun right(offset: Int = 1) = if (x + offset < w) Position(y, x + offset, "R") else null

        fun neighbors() = listOfNotNull(up(), down(), left(), right())

        override fun toString() = "P($y, $x, $dir)"

        override fun equals(other: Any?): Boolean {
            if (other !is TwoDimenGraph<*>.Position) return false
            return y == other.y && x == other.x && dir == other.dir
        }

        override fun hashCode(): Int {
            return stride * y + x + dir.hashCode()
        }
    }

    fun createPosition(y: Int, x: Int, dir: String = "") = Position(y, x, dir)

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

var TwoDimenGraph<Int>.Position.heat: Int
    get() = info["heat"] as? Int ?: 0
    set(value) {
        info["heat"] = value
    }

fun main() {

    fun solve(input: List<String>, stepRange: IntRange): Int {
        val heatGraph = TwoDimenGraph(input.flatMap { it.map { c -> c.toString().toInt() } }, input[0].length)

        var nextPositions = setOf(heatGraph.createPosition(0, 0))

        val lowestAccHeatMap = mutableMapOf<TwoDimenGraph<Int>.Position, Int>()
        var lowestHeatAtTarget = Int.MAX_VALUE
        val targetY = heatGraph.h - 1
        val targetX = heatGraph.w - 1

        while (nextPositions.isNotEmpty()) {
            "nextPositions: ${nextPositions.size}".println()

            val possibleNextPositions = mutableSetOf<TwoDimenGraph<Int>.Position>()

            nextPositions.forEach { currentP ->

                if (lowestHeatAtTarget <= currentP.heat) {
                    return@forEach
                }

                val nextDirs = when (currentP.dir) {
                    "U", "D" -> listOf("L", "R")
                    "L", "R" -> listOf("U", "D")
                    else -> listOf("U", "D", "L", "R")
                }
                val nextPs = nextDirs.map { dir ->
                    when (dir) {
                        "U" -> stepRange.mapNotNull { offset -> currentP.up(offset) }
                        "D" -> stepRange.mapNotNull { offset -> currentP.down(offset) }
                        "L" -> stepRange.mapNotNull { offset -> currentP.left(offset) }
                        "R" -> stepRange.mapNotNull { offset -> currentP.right(offset) }
                        else -> error("Unknown direction")
                    }
                }.flatten()

                nextPs.forEach nextPCheck@{ nextP ->

                    val heatDiff = when {
                        nextP.x < currentP.x -> (nextP.x..<currentP.x).sumOf { x -> heatGraph[currentP.y, x] }
                        nextP.x > currentP.x -> (currentP.x + 1..nextP.x).sumOf { x -> heatGraph[currentP.y, x] }
                        nextP.y < currentP.y -> (nextP.y..<currentP.y).sumOf { y -> heatGraph[y, currentP.x] }
                        nextP.y > currentP.y -> (currentP.y + 1..nextP.y).sumOf { y -> heatGraph[y, currentP.x] }
                        else -> error("Unknown direction")
                    }

                    nextP.heat = currentP.heat + heatDiff

                    if (lowestAccHeatMap[nextP]?.let { it <= nextP.heat } == true) {
                        return@nextPCheck
                    }

                    lowestAccHeatMap[nextP] = nextP.heat

                    possibleNextPositions.remove(nextP)
                    possibleNextPositions.add(nextP)

                    if (nextP.x == targetX && nextP.y == targetY && nextP.heat < lowestHeatAtTarget) {
                        lowestHeatAtTarget = nextP.heat
                    }
                }
            }

            nextPositions = possibleNextPositions
        }

        val destinationResults = lowestAccHeatMap.entries.filter { (k, _) -> k.x == targetX && k.y == targetY }

        return destinationResults.minOf { it.value }
    }

    fun part1(input: List<String>): Int {
        return solve(input, 1..3)
    }

    fun part2(input: List<String>): Int {
        return solve(input, 4..10)
    }

    check(part1(readInput("$FOLDER/test")) == 102)
    check(part2(readInput("$FOLDER/test")) == 94)

    val input = readInput("$FOLDER/input")
    val part1Result: Int
    val part1Time = measureNanoTime {
        part1Result = part1(input)
    }
    val part2Result: Int
    val part2Time = measureNanoTime {
        part2Result = part2(input)
    }

    println("Part 1 result: $part1Result")
    println("Part 2 result: $part2Result")
    println("Part 1 takes ${part1Time / 1e6f} milliseconds.")
    println("Part 2 takes ${part2Time / 1e6f} milliseconds.")
}