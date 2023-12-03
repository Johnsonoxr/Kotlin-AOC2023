package day03

import readInput
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureNanoTime

private const val FOLDER = "day03"

fun main() {
    fun part1(input: List<String>): Int {

        data class Number(val value: Int, val x1: Int, val y: Int, val x2: Int)

        val symbols = input.joinToString("").groupBy { it }.keys.filterNot { it.isDigit() || it == '.' }

        val numbers = input.mapIndexed { idx, line ->
            "[0-9]+".toRegex().findAll(line).toList().map { Number(it.value.toInt(), it.range.first, idx, it.range.last) }
        }.flatten()

        val validNumbers = numbers.filter { number ->
            val xRange = IntRange(max(0, number.x1 - 1), min(input[0].length - 1, number.x2 + 1))

            if (input.getOrNull(number.y - 1)?.substring(xRange)?.any { it in symbols } == true) {
                return@filter true
            }
            if (input.getOrNull(number.y)?.substring(xRange)?.any { it in symbols } == true) {
                return@filter true
            }
            if (input.getOrNull(number.y + 1)?.substring(xRange)?.any { it in symbols } == true) {
                return@filter true
            }
            return@filter false
        }

        return validNumbers.sumOf { it.value }
    }

    fun part2(input: List<String>): Int {
        data class Joint(val y: Int, val x: Int)
        data class Number(val value: Int, val x1: Int, val y: Int, val x2: Int)

        val numbers: List<Number> = input.mapIndexed { idx, line ->
            "[0-9]+".toRegex().findAll(line).toList().map { Number(it.value.toInt(), it.range.first, idx, it.range.last) }
        }.flatten()

        val joints: List<Joint> = input.mapIndexed { idx, line ->
            "[*]".toRegex().findAll(line).toList().map { Joint(idx, it.range.first) }
        }.flatten()

        val jointNumbersMap: Map<Joint, MutableList<Number>> = joints.associateWith { mutableListOf() }

        numbers.forEach { number ->
            val xRange = IntRange(max(0, number.x1 - 1), min(input[0].length - 1, number.x2 + 1))

            joints.firstOrNull { it.y == number.y - 1 && it.x in xRange }?.let { upperJoint ->
                jointNumbersMap[upperJoint]?.add(number)
                return@forEach
            }
            joints.firstOrNull { it.y == number.y && it.x in xRange }?.let { currentJoint ->
                jointNumbersMap[currentJoint]?.add(number)
                return@forEach
            }
            joints.firstOrNull { it.y == number.y + 1 && it.x in xRange }?.let { lowerJoint ->
                jointNumbersMap[lowerJoint]?.add(number)
                return@forEach
            }
        }

        return jointNumbersMap.filter { it.value.size == 2 }.values.sumOf { it[0].value * it[1].value }
    }

    check(part1(readInput("$FOLDER/test")) == 4361)
    check(part2(readInput("$FOLDER/test")) == 467835)

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