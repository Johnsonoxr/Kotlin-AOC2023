package day01

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day01"

fun main() {
    fun part1(input: List<String>): Int {
        val regex = "[0-9]".toRegex()
        val calibrationValues = input.map { regex.findAll(it).map { rst -> rst.value }.toList() }
            .map { nStrList -> "${nStrList.first()}${nStrList.last()}".toInt() }
        return calibrationValues.sum()
    }

    fun part2(input: List<String>): Int {

        val convertMap = mapOf(
            "one" to 1,
            "two" to 2,
            "three" to 3,
            "four" to 4,
            "five" to 5,
            "six" to 6,
            "seven" to 7,
            "eight" to 8,
            "nine" to 9,
            "1" to 1,
            "2" to 2,
            "3" to 3,
            "4" to 4,
            "5" to 5,
            "6" to 6,
            "7" to 7,
            "8" to 8,
            "9" to 9,
        )

        val cvtRegex = convertMap.keys.joinToString("|").toRegex()
        val revRegex = convertMap.keys.joinToString("|").reversed().toRegex()

        val calibrationValues = input.map { line ->
            val firstMatchResult = cvtRegex.find(line)!!
            val lastMatchResult = revRegex.find(line.reversed())!!
            return@map convertMap[firstMatchResult.value]!! * 10 + convertMap[lastMatchResult.value.reversed()]!!
        }

        return calibrationValues.sum()
    }

    check(part1(readInput("${FOLDER}/test")) == 142)
    check(part2(readInput("${FOLDER}/test2")) == 281)

    val input = readInput("${FOLDER}/input")
    val part1Time = measureNanoTime {
        part1(input).println()
    }
    val part2Time = measureNanoTime {
        part2(input).println()
    }

    println("Part 1 takes ${part1Time / 1e6f} milliseconds.")
    println("Part 2 takes ${part2Time / 1e6f} milliseconds.")
}
