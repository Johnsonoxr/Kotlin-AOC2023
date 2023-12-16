package day16

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day16"

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
    data class Beam(val position: TwoDimenGraph<Char>.Position, val direction: String)

    fun solve(graph: TwoDimenGraph<Char>, startBeam: Beam): Map<TwoDimenGraph<Char>.Position, Set<String>> {
        val uncheckedBeams = mutableSetOf(startBeam)

        val beamDirsInGraph = mutableMapOf<TwoDimenGraph<Char>.Position, MutableSet<String>>()

        while (uncheckedBeams.isNotEmpty()) {

            val nextBeams = mutableSetOf<Beam>()

            uncheckedBeams.forEach { beam ->

                if (beamDirsInGraph[beam.position]?.contains(beam.direction) == true) {
                    //  Already checked
                    return@forEach
                }

                beamDirsInGraph.getOrPut(beam.position) { mutableSetOf() }.add(beam.direction)

                when (graph[beam.position]) {
                    '.' -> when (beam.direction) {
                        "right" -> beam.position.right()?.let { nextBeams.add(Beam(it, "right")) }
                        "down" -> beam.position.down()?.let { nextBeams.add(Beam(it, "down")) }
                        "left" -> beam.position.left()?.let { nextBeams.add(Beam(it, "left")) }
                        "up" -> beam.position.up()?.let { nextBeams.add(Beam(it, "up")) }
                    }

                    '-' -> when (beam.direction) {
                        "right" -> beam.position.right()?.let { nextBeams.add(Beam(it, "right")) }
                        "left" -> beam.position.left()?.let { nextBeams.add(Beam(it, "left")) }
                        "up", "down" -> {
                            beam.position.right()?.let { nextBeams.add(Beam(it, "right")) }
                            beam.position.left()?.let { nextBeams.add(Beam(it, "left")) }
                        }
                    }

                    '|' -> when (beam.direction) {
                        "up" -> beam.position.up()?.let { nextBeams.add(Beam(it, "up")) }
                        "down" -> beam.position.down()?.let { nextBeams.add(Beam(it, "down")) }
                        "left", "right" -> {
                            beam.position.up()?.let { nextBeams.add(Beam(it, "up")) }
                            beam.position.down()?.let { nextBeams.add(Beam(it, "down")) }
                        }
                    }

                    '\\' -> when (beam.direction) {
                        "up" -> beam.position.left()?.let { nextBeams.add(Beam(it, "left")) }
                        "down" -> beam.position.right()?.let { nextBeams.add(Beam(it, "right")) }
                        "left" -> beam.position.up()?.let { nextBeams.add(Beam(it, "up")) }
                        "right" -> beam.position.down()?.let { nextBeams.add(Beam(it, "down")) }
                    }

                    '/' -> when (beam.direction) {
                        "up" -> beam.position.right()?.let { nextBeams.add(Beam(it, "right")) }
                        "down" -> beam.position.left()?.let { nextBeams.add(Beam(it, "left")) }
                        "left" -> beam.position.down()?.let { nextBeams.add(Beam(it, "down")) }
                        "right" -> beam.position.up()?.let { nextBeams.add(Beam(it, "up")) }
                    }
                }
            }

            uncheckedBeams.clear()
            uncheckedBeams.addAll(nextBeams)
        }

        return beamDirsInGraph
    }

    fun part1(input: List<String>): Int {
        val graph = TwoDimenGraph(input.joinToString("").toMutableList(), input[0].length)
        return solve(graph, Beam(graph.createPosition(0, 0), "right")).size
    }

    fun part2(input: List<String>): Int {

        data class Beam(val position: TwoDimenGraph<Char>.Position, val direction: String)

        val graph = TwoDimenGraph(input.joinToString("").toMutableList(), input[0].length)

        var maxLighten = 0

        val beamsCheckedMap = mutableMapOf<TwoDimenGraph<Char>.Position, MutableSet<String>>()

        graph.createPositionIterator().forEach { position ->

            listOf(
                "right",
                "down",
                "left",
                "up"
            ).forEach dirTest@{ direction ->

                if (beamsCheckedMap[position]?.contains(direction) == true) {
                    //  Pair position and direction already checked before
                    return@dirTest
                }

                val prevPosition = when (direction) {
                    "right" -> position.left()
                    "down" -> position.up()
                    "left" -> position.right()
                    "up" -> position.down()
                    else -> throw Exception("Unknown direction: $direction")
                }

                if (prevPosition != null && graph[prevPosition] == '.') {
                    //  No need to check this position since starting from prePosition is better
                    return@dirTest
                }

                val graphBeams = mutableMapOf<TwoDimenGraph<Char>.Position, MutableSet<String>>()

                val uncheckedBeams = mutableSetOf(Beam(position, direction))

                while (uncheckedBeams.isNotEmpty()) {

                    val nextBeams = mutableSetOf<Beam>()

                    uncheckedBeams.forEach beamCheck@{ beam ->

                        if (graphBeams[beam.position]?.contains(beam.direction) == true) {
                            //  Already checked
                            return@beamCheck
                        }

                        beamsCheckedMap.getOrPut(beam.position) { mutableSetOf() }.add(beam.direction)
                        graphBeams.getOrPut(beam.position) { mutableSetOf() }.add(beam.direction)

                        when (graph[beam.position]) {
                            '.' -> when (beam.direction) {
                                "right" -> beam.position.right()?.let { nextBeams.add(Beam(it, "right")) }
                                "down" -> beam.position.down()?.let { nextBeams.add(Beam(it, "down")) }
                                "left" -> beam.position.left()?.let { nextBeams.add(Beam(it, "left")) }
                                "up" -> beam.position.up()?.let { nextBeams.add(Beam(it, "up")) }
                            }

                            '-' -> when (beam.direction) {
                                "right" -> beam.position.right()?.let { nextBeams.add(Beam(it, "right")) }
                                "left" -> beam.position.left()?.let { nextBeams.add(Beam(it, "left")) }
                                "up", "down" -> {
                                    beam.position.right()?.let { nextBeams.add(Beam(it, "right")) }
                                    beam.position.left()?.let { nextBeams.add(Beam(it, "left")) }
                                }
                            }

                            '|' -> when (beam.direction) {
                                "up" -> beam.position.up()?.let { nextBeams.add(Beam(it, "up")) }
                                "down" -> beam.position.down()?.let { nextBeams.add(Beam(it, "down")) }
                                "left", "right" -> {
                                    beam.position.up()?.let { nextBeams.add(Beam(it, "up")) }
                                    beam.position.down()?.let { nextBeams.add(Beam(it, "down")) }
                                }
                            }

                            '\\' -> when (beam.direction) {
                                "up" -> beam.position.left()?.let { nextBeams.add(Beam(it, "left")) }
                                "down" -> beam.position.right()?.let { nextBeams.add(Beam(it, "right")) }
                                "left" -> beam.position.up()?.let { nextBeams.add(Beam(it, "up")) }
                                "right" -> beam.position.down()?.let { nextBeams.add(Beam(it, "down")) }
                            }

                            '/' -> when (beam.direction) {
                                "up" -> beam.position.right()?.let { nextBeams.add(Beam(it, "right")) }
                                "down" -> beam.position.left()?.let { nextBeams.add(Beam(it, "left")) }
                                "left" -> beam.position.down()?.let { nextBeams.add(Beam(it, "down")) }
                                "right" -> beam.position.up()?.let { nextBeams.add(Beam(it, "up")) }
                            }
                        }
                    }

                    uncheckedBeams.clear()
                    uncheckedBeams.addAll(nextBeams)
                }

                if (graphBeams.size > maxLighten) {
                    maxLighten = graphBeams.size
                    "Max lighten: $maxLighten at $position, $direction".println()
                }
            }
        }


        return maxLighten
    }

    check(part1(readInput("$FOLDER/test")) == 46)
    check(part2(readInput("$FOLDER/test")) == 51)

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