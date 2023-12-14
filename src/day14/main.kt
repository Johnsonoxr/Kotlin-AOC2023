package day14

import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day14"

class TwoDimenGraph<T>(graph: Collection<T>, val stride: Int) {

    inner class Position(val y: Int, val x: Int) {

        val info: MutableMap<String, Any> by lazy { mutableMapOf() }

        fun up(offset: Int = 1) = if (y - offset >= 0) Position(y - offset, x) else null
        fun down(offset: Int = 1) = if (y + offset < h) Position(y + offset, x) else null
        fun left(offset: Int = 1) = if (x - offset >= 0) Position(y, x - offset) else null
        fun right(offset: Int = 1) = if (x + offset < w) Position(y, x + offset) else null

        fun offset(offsetY: Int, offsetX: Int) = when {
            offsetY > 0 -> down(offsetY)
            offsetY < 0 -> up(-offsetY)
            offsetX > 0 -> right(offsetX)
            offsetX < 0 -> left(-offsetX)
            else -> this
        }

        fun neighbors() = listOfNotNull(up(), down(), left(), right())

        override fun toString() = "P($y, $x)"

        fun positionString() = "$y,$x"

        override fun equals(other: Any?): Boolean {
            if (other !is TwoDimenGraph<*>.Position) return false
            return y == other.y && x == other.x
        }

        override fun hashCode(): Int {
            return stride * y + x
        }
    }

    fun createPosition(y: Int, x: Int) = Position(y, x)

    val graph = graph.toMutableList()
    val w = stride
    val h = graph.size / stride

    operator fun get(position: Position): T {
        return graph[position.y * stride + position.x]
    }

    operator fun set(position: Position, value: T): Boolean {
        graph[position.y * stride + position.x] = value
        return true
    }

    override fun toString(): String {
        return graph.chunked(stride).joinToString("\n") { it.joinToString("") }
    }
}

fun main() {

    fun TwoDimenGraph<Char>.getRocks(): MutableList<TwoDimenGraph<Char>.Position> {
        val rocks = mutableListOf<TwoDimenGraph<Char>.Position>()
        for (y in 0..<h) {
            for (x in 0..<w) {
                val position = createPosition(y, x)
                if (this[position] == 'O') {
                    rocks.add(position)
                }
            }
        }
        return rocks
    }

    fun part1(input: List<String>): Long {

        val graph = TwoDimenGraph(input.flatMap { it.toList() }, input[0].length)

        val rocks = graph.getRocks()

        rocks.indices.forEach { i ->
            val roundRock = rocks[i]

            var nextPosition = roundRock

            while (true) {
                val northNeighbor = nextPosition.up()?.takeIf { graph[it] == '.' } ?: break
                nextPosition = northNeighbor
            }

            rocks[i] = nextPosition
            graph[roundRock] = '.'
            graph[nextPosition] = 'O'
        }

        return rocks.sumOf { graph.h - it.y }.toLong()
    }

    fun part2(input: List<String>): Long {
        val graph = TwoDimenGraph(input.flatMap { it.toList() }, input[0].length)
        val rocks = graph.getRocks()

        val groupPatterns = mutableMapOf<String, Int>()

        for (iter in 1..1_000_000_000) {

            val offsets = listOf(
                -1 to 0,
                0 to -1,
                1 to 0,
                0 to 1
            )

            offsets.forEach { (dy, dx) ->
                rocks.sortBy {
                    when {
                        dy > 0 -> -it.y
                        dy < 0 -> it.y
                        dx > 0 -> -it.x
                        dx < 0 -> it.x
                        else -> 0
                    }
                }
                rocks.indices.forEach { i ->
                    val roundRock = rocks[i]
                    var nextPosition = roundRock

                    while (true) {
                        val northNeighbor = nextPosition.offset(dy, dx)?.takeIf { graph[it] == '.' } ?: break
                        nextPosition = northNeighbor
                    }

                    rocks[i] = nextPosition
                    graph[roundRock] = '.'
                    graph[nextPosition] = 'O'
                }
            }

            val groupPattern = graph.toString()
            groupPatterns[groupPattern]?.let { cycleStartIter ->
                val cycleLength = iter - cycleStartIter
                val targetIter = cycleStartIter + ((1_000_000_000 - iter) % cycleLength)
                val targetPattern = groupPatterns.entries.first { it.value == targetIter }.key
                val targetGraph = TwoDimenGraph(targetPattern.split('\n').flatMap { it.toList() }, input[0].length)
                val targetRocks = targetGraph.getRocks()
                return targetRocks.sumOf { targetGraph.h - it.y }.toLong()
            }
            groupPatterns[groupPattern] = iter
        }

        throw IllegalStateException("Should not reach here")
    }

    check(part1(readInput("$FOLDER/test")) == 136L)
    check(part2(readInput("$FOLDER/test")) == 64L)

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