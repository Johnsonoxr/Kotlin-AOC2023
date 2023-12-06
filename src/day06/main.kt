package day06

import readInput
import kotlin.math.max
import kotlin.system.measureNanoTime

private const val FOLDER = "day06"

fun main() {
    fun part1(input: List<String>): Long {
        val timeList = "\\d+".toRegex().findAll(input[0]).map { it.value.toLong() }.toList()
        val distanceList = "\\d+".toRegex().findAll(input[1]).map { it.value.toLong() }.toList()
        val recordMap = timeList.zip(distanceList).toMap()

        fun distanceMarch(buttonPressTime: Long, totalTime: Long): Long {
            return buttonPressTime * max(0, totalTime - buttonPressTime)
        }

        val solutionCountList = mutableListOf<Long>()
        recordMap.forEach { (time, record) ->
            val solutionCount = (1..time).count { distanceMarch(it, time) > record }
            solutionCountList.add(solutionCount.toLong())
        }

        return solutionCountList.reduce { acc, l -> acc * l }
    }

    fun part2(input: List<String>): Long {
        val time = "\\d+".toRegex().findAll(input[0]).map { it.value }.joinToString("").toLong()
        val record = "\\d+".toRegex().findAll(input[1]).map { it.value }.joinToString("").toLong()

        fun distanceMarch(buttonPressTime: Long, totalTime: Long): Long {
            return buttonPressTime * (totalTime - buttonPressTime)
        }

        val solutionCountList = mutableListOf<Long>()
        val solutionCount = (1..time).count { distanceMarch(it, time) > record }
        solutionCountList.add(solutionCount.toLong())

        return solutionCountList.reduce { acc, l -> acc * l }
    }

    check(part1(readInput("$FOLDER/test")) == 288L)
    check(part2(readInput("$FOLDER/test")) == 71503L)

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