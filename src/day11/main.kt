package day11

import println
import readInput
import kotlin.math.abs
import kotlin.system.measureNanoTime

private const val FOLDER = "day11"

data class Point(val x: Long, val y: Long)

fun main() {

    fun universeExpansion(input: List<String>, expansionFactor: Int): Long {
        val galaxies = mutableListOf<Point>()
        input.forEachIndexed { y, line ->
            line.forEachIndexed { x, c ->
                if (c == '#') {
                    galaxies.add(Point(x.toLong(), y.toLong()))
                }
            }
        }

        val horizontalGalaxyIndices = galaxies.map { g -> g.x }.toSet().toList().sorted()
        (horizontalGalaxyIndices.last() - 1 downTo horizontalGalaxyIndices.first() + 1).forEach { x ->
            if (x in horizontalGalaxyIndices) {
                return@forEach
            }
            galaxies.filter { g -> g.x > x }.forEach { g ->
                galaxies[galaxies.indexOf(g)] = Point(g.x + expansionFactor - 1, g.y)
            }
        }

        val verticalGalaxyIndices = galaxies.map { g -> g.y }.toSet().toList().sorted()
        (verticalGalaxyIndices.last() - 1 downTo verticalGalaxyIndices.first() + 1).forEach { y ->
            if (y in verticalGalaxyIndices) {
                return@forEach
            }
            galaxies.filter { g -> g.y > y }.forEach { g ->
                galaxies[galaxies.indexOf(g)] = Point(g.x, g.y + expansionFactor - 1)
            }
        }

        var distance = 0L
        for (idx1 in galaxies.indices) {
            for (idx2 in idx1 + 1 until galaxies.size) {
                val g1 = galaxies[idx1]
                val g2 = galaxies[idx2]
                distance += abs(g1.x - g2.x) + abs(g1.y - g2.y)
            }
        }

        return distance
    }

    fun part1(input: List<String>): Long {
        return universeExpansion(input, 2)
    }

    fun part2(input: List<String>, expansionFactor: Int = 1000000): Long {
        return universeExpansion(input, expansionFactor)
    }

    check(part1(readInput("$FOLDER/test")) == 374L)
    check(part2(readInput("$FOLDER/test"), expansionFactor = 100) == 8410L)

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