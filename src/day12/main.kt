package day12

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day12"

fun main() {
    fun part1(input: List<String>): Int {

        data class DamagedSegment(val offset: Int, val length: Int)

        fun List<DamagedSegment>.toPattern(): String {
            return this.joinToString("") { ds -> ".".repeat(ds.offset) + "#".repeat(ds.length) + "." }
        }

        fun fits(pattern: String, springRepresentation: String): Boolean {
            if (pattern.length > springRepresentation.length) return false
            return pattern.zip(springRepresentation).all { (p, s) ->
                '?' == s || p == s
            }
        }

        val validPatternsCountList = input.map { line ->
            val springRepresentation = line.split(" ")[0] + "."
            val damagedGroups = line.split(" ")[1].split(",").map { it.toInt() }

            fun getPossiblePatternCount(list: List<DamagedSegment>): Int {
                if (list.isNotEmpty()) {
                    val pattern = list.toPattern()
                    if (pattern.length > springRepresentation.length) return -1
                    val isPatternFits = fits(pattern, springRepresentation)
                    if (list.size == damagedGroups.size) {
                        return if (isPatternFits) 1 else 0
                    } else if (!isPatternFits) {
                        return 0
                    }
                }

                var nextOffset = 0
                var validPatternSum = 0
                while (true) {
                    val cnt = getPossiblePatternCount(list + DamagedSegment(nextOffset++, damagedGroups[list.size]))
                    if (cnt < 0) {
                        break
                    }
                    validPatternSum += cnt
                }
                return validPatternSum
            }

            val patternsFound = getPossiblePatternCount(emptyList())

            "$patternsFound patterns found for $line".println()

            return@map patternsFound
        }

        return validPatternsCountList.sum()
    }

    fun part2(input: List<String>): Int {
        return 1
    }

    check(part1(readInput("$FOLDER/test")) == 21)
    check(part2(readInput("$FOLDER/test")) == 1)

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