package day10

import println
import readInput
import kotlin.system.exitProcess
import kotlin.system.measureNanoTime

private const val FOLDER = "day10"

class TwoDimenGraph<T>(graph: Collection<T>, val stride: Int) {

    inner class Position(val y: Int, val x: Int) {

        val info: MutableMap<String, Any> by lazy { mutableMapOf() }

        fun up(offset: Int = 1) = if (y - offset >= 0) Position(y - offset, x) else null
        fun down(offset: Int = 1) = if (y + offset < h) Position(y + offset, x) else null
        fun left(offset: Int = 1) = if (x - offset >= 0) Position(y, x - offset) else null
        fun right(offset: Int = 1) = if (x + offset < w) Position(y, x + offset) else null

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

    fun getPipeLoop(graph: TwoDimenGraph<Char>): List<TwoDimenGraph<Char>.Position> {
        val startIdx = graph.graph.indexOfFirst { it == 'S' }
        val start = graph.createPosition(startIdx / graph.w, startIdx % graph.w)
        val pipe = mutableListOf(start)

        fun TwoDimenGraph<Char>.Position.connections(): List<TwoDimenGraph<Char>.Position> {
            return when (graph[this]) {
                'S' -> neighbors().filter { this in it.connections() }
                'J' -> listOfNotNull(this.up(), this.left())
                'L' -> listOfNotNull(this.up(), this.right())
                'F' -> listOfNotNull(this.down(), this.right())
                '7' -> listOfNotNull(this.down(), this.left())
                '|' -> listOfNotNull(this.up(), this.down())
                '-' -> listOfNotNull(this.left(), this.right())
                else -> emptyList()
            }
        }

        var currentPosition = start
        while (true) {
            currentPosition = currentPosition.connections().firstOrNull { it !in pipe } ?: break
            pipe.add(currentPosition)
        }

        return pipe
    }

    fun part1(input: List<String>): Int {
        val graph = TwoDimenGraph(input.flatMap { it.toList() }, input[0].length)
        val pipe = getPipeLoop(graph)
        return pipe.size / 2
    }

    fun part2(input: List<String>): Int {
        val graph = TwoDimenGraph(input.flatMap { it.toList() }, input[0].length)
        val pipe = getPipeLoop(graph)

        val clockwiseTest = pipe.windowed(2).map { (p1, p2) ->
            when (graph[p2]) {
                'J' -> if (p1 == p2.up()) 1 else -1
                'L' -> if (p1 == p2.right()) 1 else -1
                'F' -> if (p1 == p2.down()) 1 else -1
                '7' -> if (p1 == p2.left()) 1 else -1
                else -> 0
            }
        }.sum()
        val isClockwise = clockwiseTest > 0

        val clockwisePipe = if (isClockwise) pipe else pipe.asReversed()

        val innerTiles = mutableSetOf<TwoDimenGraph<Char>.Position>()

        clockwisePipe.windowed(2).forEach { (p1, p2) ->
            when (p2) {
                p1.up() -> {
                    p1.right()?.takeIf { it !in clockwisePipe }?.let { innerTiles.add(it) }
                    p2.right()?.takeIf { it !in clockwisePipe }?.let { innerTiles.add(it) }
                }

                p1.right() -> {
                    p1.down()?.takeIf { it !in clockwisePipe }?.let { innerTiles.add(it) }
                    p2.down()?.takeIf { it !in clockwisePipe }?.let { innerTiles.add(it) }
                }

                p1.down() -> {
                    p1.left()?.takeIf { it !in clockwisePipe }?.let { innerTiles.add(it) }
                    p2.left()?.takeIf { it !in clockwisePipe }?.let { innerTiles.add(it) }
                }

                p1.left() -> {
                    p1.up()?.takeIf { it !in clockwisePipe }?.let { innerTiles.add(it) }
                    p2.up()?.takeIf { it !in clockwisePipe }?.let { innerTiles.add(it) }
                }
            }
        }

        var activeTileSeeds = innerTiles.toSet()
        while (activeTileSeeds.isNotEmpty()) {
            activeTileSeeds = activeTileSeeds.flatMap { seed ->
                seed.neighbors().filter { it !in clockwisePipe && it !in innerTiles }
            }.toSet()
            innerTiles.addAll(activeTileSeeds)
        }

        for (y in 0 until graph.h) {
            for (x in 0 until graph.w) {
                when (val position = graph.createPosition(y, x)) {
                    in pipe -> Unit
                    in innerTiles -> graph[position] = '#'
                    else -> graph[position] = '.'
                }
            }
        }

        fun TwoDimenGraph<Char>.print() {
            println(this.graph.chunked(this.stride).joinToString("\n") { it.joinToString("") })
        }

        graph.print()

        return innerTiles.size
    }

    check(part1(readInput("$FOLDER/test")) == 8)
    check(part2(readInput("$FOLDER/test2")) == 8)
    check(part2(readInput("$FOLDER/test3")) == 10)

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