package day21

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day21"

class TwoDimenGraph(graph: Collection<Char>, val stride: Int) {

    inner class Position(val y: Int, val x: Int) {

        val info: MutableMap<String, Any> by lazy { mutableMapOf() }

        fun up(offset: Int = 1) = if (y - offset >= 0) Position(y - offset, x) else null
        fun down(offset: Int = 1) = if (y + offset < h) Position(y + offset, x) else null
        fun left(offset: Int = 1) = if (x - offset >= 0) Position(y, x - offset) else null
        fun right(offset: Int = 1) = if (x + offset < w) Position(y, x + offset) else null

        fun neighbors() = listOfNotNull(up(), down(), left(), right())

        override fun toString() = "P($y, $x)"

        override fun equals(other: Any?): Boolean {
            if (other !is TwoDimenGraph.Position) return false
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

    operator fun get(y: Int, x: Int): Char {
        return graph[y * stride + x]
    }

    operator fun get(position: Position): Char {
        return get(position.y, position.x)
    }

    operator fun set(position: Position, value: Char): Boolean {
        graph[position.y * stride + position.x] = value
        return true
    }

    override fun toString(): String {
        return graph.chunked(stride).joinToString("\n") { it.joinToString("") }
    }
}


fun main() {
    fun part1(input: List<String>, stepCount: Int): Long {

        val graph = TwoDimenGraph(input.flatMap { it.toList() }, input[0].length)

        var positions = graph.graph.mapIndexed { index, c ->
            if (c == 'S') {
                graph.createPosition(index / graph.w, index % graph.w)
            } else {
                null
            }
        }.filterNotNull().toSet()

        repeat(stepCount) {
            val newPositions = mutableSetOf<TwoDimenGraph.Position>()
            for (position in positions) {
                for (neighbor in position.neighbors()) {
                    if (graph[neighbor] == '.' || graph[neighbor] == 'S') {
                        newPositions.add(neighbor)
                    }
                }
            }
            "Step $it: ${newPositions}".println()
            positions = newPositions
        }

        return positions.size.also { println(positions.size) }.toLong()
    }

    fun part2(input: List<String>, stepCount: Long): Long {
        return 1
    }

    check(part1(readInput("$FOLDER/test"), 6) == 16L)
    check(part2(readInput("$FOLDER/test"), 5000) == 16733044L)

    val input = readInput("$FOLDER/input")
    val part1Result: Long
    val part1Time = measureNanoTime {
        part1Result = part1(input, 64)
    }
    val part2Result: Long
    val part2Time = measureNanoTime {
        part2Result = part2(input, 26501365)
    }

    println("Part 1 result: $part1Result")
    println("Part 2 result: $part2Result")
    println("Part 1 takes ${part1Time / 1e6f} milliseconds.")
    println("Part 2 takes ${part2Time / 1e6f} milliseconds.")
}