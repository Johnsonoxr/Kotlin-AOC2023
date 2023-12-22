package day21

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day21"

data class P(val y: Long, val x: Long) {
    fun neighbors(): List<P> {
        return listOf(
            P(y - 1, x),
            P(y + 1, x),
            P(y, x - 1),
            P(y, x + 1)
        )
    }
}

fun Long.positiveMod(other: Long): Long {
    return ((this % other) + other) % other
}

fun main() {

    fun solve(input: List<String>, stepCount: Long): Long {

        val rocks = input.flatMapIndexed { y, line ->
            line.mapIndexed { x, c ->
                if (c == '#') P(y.toLong(), x.toLong()) else null
            }.filterNotNull()
        }.toSet()

        val len = input[0].length.toLong()
        val startP = P(len / 2, len / 2)

        val visitedInactivated = mutableSetOf<P>()
        var visitedActivated = setOf<P>()
        var flooding = setOf(startP)

        val patternRecorder = mutableListOf<Long>()

        var step = 1L
        while (true) {
            val nextFlooding = mutableSetOf<P>()
            flooding.forEach { floodingP ->
                val neighborPs = floodingP.neighbors()

                neighborPs.forEach neighborLoop@{ neighborP ->

                    if (neighborP in visitedActivated) {
                        return@neighborLoop
                    }

                    val modNeighborP = P(neighborP.y.positiveMod(len), neighborP.x.positiveMod(len))
                    if (modNeighborP in rocks) {
                        return@neighborLoop
                    }

                    nextFlooding.add(neighborP)
                }
            }

            visitedInactivated.addAll(visitedActivated)
            visitedActivated = flooding
            flooding = nextFlooding

            if ((stepCount - step) % (2 * len) == 0L) {
                val flooded = visitedActivated + visitedInactivated + flooding
                val countEven = step % 2 == 0L
                val floodedSize = flooded.count {
                    when (countEven) {
                        true -> it.x.positiveMod(2) == it.y.positiveMod(2)
                        else -> it.x.positiveMod(2) != it.y.positiveMod(2)
                    }
                }.toLong()

                "Step $step/$stepCount: $floodedSize".println()

                patternRecorder.add(floodedSize)
                if (step == stepCount) {
                    return floodedSize
                }

                if (patternRecorder.size >= 4) {
                    val degree1 = patternRecorder.windowed(2).map { it[1] - it[0] }
                    val degree2 = degree1.windowed(2).map { it[1] - it[0] }
                    if (degree2.size >= 2 && degree2.last() == degree2[degree2.size - 2]) {
                        "Pattern found: degree 2 with \"${degree2.last()}\"".println()
                        patternRecorder.joinToString("") { it.toString().padStart(10, ' ') }.println()
                        degree1.joinToString("", prefix = " ".repeat(5)) { it.toString().padStart(10, ' ') }.println()
                        degree2.joinToString("", prefix = " ".repeat(10)) { it.toString().padStart(10, ' ') }.println()
                        break
                    }
                }
            }

            step++
        }

        val largeStepCount = (stepCount - step) / (2 * len)

        val last3 = patternRecorder.takeLast(3)
        var plotCount = last3[2]
        var incDiff = last3[2] - last3[1]
        val constDiff = (last3[2] - last3[1]) - (last3[1] - last3[0])

        for (i in 1..largeStepCount) {
            incDiff += constDiff
            plotCount += incDiff
        }

        return plotCount
    }

    fun part1(input: List<String>, stepCount: Long): Long {
        return solve(input, stepCount)
    }

    fun part2(input: List<String>, stepCount: Long): Long {
        return solve(input, stepCount)
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