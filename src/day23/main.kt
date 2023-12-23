package day23

import println
import readInput
import kotlin.coroutines.CoroutineContext
import kotlin.math.max
import kotlin.system.measureNanoTime

private const val FOLDER = "day23"

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

    fun createPositionIterator() = object : Iterator<TwoDimenGraph<T>.Position> {
        var y = 0
        var x = 0

        override fun hasNext(): Boolean {
            return y < h && x < w
        }

        override fun next(): TwoDimenGraph<T>.Position {
            val result = Position(y, x)
            x++
            if (x == w) {
                x = 0
                y++
            }
            return result
        }
    }

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
    fun part1(input: List<String>): Int {

        val graph = TwoDimenGraph(input.flatMap { it.toList() }, input[0].length)

        val startP = graph.createPosition(0, 1)
        val endP = graph.createPosition(graph.h - 1, graph.w - 2)

        var paths = listOf(listOf(startP))

        val finishedPaths = mutableListOf<List<TwoDimenGraph<Char>.Position>>()

        while (paths.isNotEmpty()) {
            val newPaths = mutableListOf<List<TwoDimenGraph<Char>.Position>>()

            paths.forEach { path ->
                val lastP = path.last()
                val possibleNextPs = when (graph[lastP]) {
                    '.' -> lastP.neighbors()
                    '>' -> listOfNotNull(lastP.right())
                    '<' -> listOfNotNull(lastP.left())
                    '^' -> listOfNotNull(lastP.up())
                    'v' -> listOfNotNull(lastP.down())
                    else -> error("Unknown char ${graph[lastP]}")
                }
                val nextPs = possibleNextPs.filter { p ->
                    graph[p] != '#' && p != path.getOrNull(path.size - 2) && p !in path
                }
                val validPaths = nextPs.map { path + it }

                validPaths.forEach { validPath ->
                    if (validPath.last() == endP) {
                        finishedPaths.add(validPath)
                    } else {
                        newPaths.add(validPath)
                    }
                }
            }

            paths = newPaths
        }

        return finishedPaths.maxOf { it.size - 1 }
    }

    fun part2(input: List<String>): Int {
        val graph = TwoDimenGraph(input.flatMap { it.toList() }, input[0].length)

        val startP = graph.createPosition(0, 1)
        val endP = graph.createPosition(graph.h - 1, graph.w - 2)

        fun TwoDimenGraph<Char>.Position.isCross() = neighbors().count { graph[it] != '#' } >= 3

        data class Tracer(
            val currentP: TwoDimenGraph<Char>.Position,
            val prevP: TwoDimenGraph<Char>.Position,
            val lastCrossP: TwoDimenGraph<Char>.Position,
            val steps: Int
        )

        val connections = mutableMapOf<Pair<TwoDimenGraph<Char>.Position, TwoDimenGraph<Char>.Position>, Int>()

        var tracers = setOf(Tracer(startP, startP, startP, 0))


        while (tracers.isNotEmpty()) {
            val nextTracers = mutableSetOf<Tracer>()

            tracers.forEach { tracer ->
                val nextPs = tracer.currentP.neighbors().filter { p ->
                    graph[p] != '#' && p != tracer.prevP && p != tracer.lastCrossP
                }

                nextPs.forEach { nextP ->

                    if (nextP.isCross()) {
                        val isCrossVisited = connections.any {
                            it.key == tracer.lastCrossP to nextP && it.value >= tracer.steps
                        }

                        if (!isCrossVisited) {
                            listOf(
                                tracer.lastCrossP to nextP,
                                nextP to tracer.lastCrossP
                            ).forEach { trace ->
                                val storedSteps = connections[trace]
                                connections[trace] = max(storedSteps ?: 0, tracer.steps + 1)
                            }

                            nextP.neighbors().filter { p ->
                                graph[p] != '#' && p != tracer.currentP
                            }.map { nextP2 ->
                                Tracer(
                                    currentP = nextP2,
                                    prevP = nextP,
                                    lastCrossP = nextP,
                                    steps = 1
                                )
                            }.forEach { nextTracers.add(it) }
                        }
                    } else if (nextP == endP) {
                        listOf(
                            tracer.lastCrossP to nextP,
                            nextP to tracer.lastCrossP
                        ).forEach { trace ->
                            val storedSteps = connections[trace]
                            connections[trace] = max(storedSteps ?: 0, tracer.steps + 1)
                        }
                    } else {
                        nextTracers.add(
                            Tracer(
                                currentP = nextP,
                                prevP = tracer.currentP,
                                lastCrossP = tracer.lastCrossP,
                                steps = tracer.steps + 1
                            )
                        )
                    }
                }
            }

            tracers = nextTracers
        }

        data class Path(val nodes: List<TwoDimenGraph<Char>.Position>, val steps: Int)

        var paths = listOf(Path(listOf(startP), 0))
        val finishedPaths = mutableListOf<Path>()

        "Testing graph size: ${graph.w}x${graph.h}".println()

        while (paths.isNotEmpty()) {

            "Path count: ${paths.size}, wait...".println()

            val nextPaths = mutableListOf<Path>()

            paths.forEach { path ->
                val lastNode = path.nodes.last()

                val nextNodes = connections.keys.filter { it.first == lastNode && it.second !in path.nodes }.map { it.second }

                val validPaths = nextNodes.map { Path(path.nodes + it, path.steps + connections[lastNode to it]!!) }

                validPaths.forEach { validPath ->
                    if (validPath.nodes.last() == endP) {
                        finishedPaths.add(validPath)
                    } else {
                        nextPaths.add(validPath)
                    }
                }
            }

            paths = nextPaths
        }

        return finishedPaths.maxOf { it.steps }
    }

    check(part1(readInput("$FOLDER/test")) == 94)
    check(part2(readInput("$FOLDER/test")) == 154)

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