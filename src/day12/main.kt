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

                    if (list.size == damagedGroups.size) {
                        return if (fits(pattern.padEnd(springRepresentation.length, '.'), springRepresentation)) 1 else 0
                    } else if (!fits(pattern, springRepresentation)) {
                        return 0
                    }
                }

                var validPatternSum = 0
                val maxOffset = springRepresentation.length - damagedGroups.subList(list.size, damagedGroups.size).sumOf { it + 1 }
                for (offset in 0..maxOffset) {
                    val len = damagedGroups[list.size]
                    val damagedSegment = DamagedSegment(offset, len)
                    val cnt = getPossiblePatternCount(list + damagedSegment)
                    validPatternSum += cnt
                }

                return validPatternSum
            }

            val patternsFound = getPossiblePatternCount(emptyList())

//            "$patternsFound patterns found for $line".println()

            return@map patternsFound
        }

        return validPatternsCountList.sum()
    }

    fun part2(input: List<String>): Long {
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
            "Processing $line".println()
            val springRepresentation = List(5) { line.split(" ")[0] }.joinToString("?") + "."
            "$springRepresentation".println()
            val damagedGroups = List(5) { line.split(" ")[1].split(",").map { it.toInt() } }.flatten()
            "$damagedGroups".println()

            fun getPossiblePatternCount(list: List<DamagedSegment>): Long {
                if (list.isNotEmpty()) {
                    val pattern = list.toPattern()

                    if (list.size == damagedGroups.size) {
                        return if (fits(pattern.padEnd(springRepresentation.length, '.'), springRepresentation)) {
//                            "$line: $pattern".println()
                            1
                        } else 0
                    } else if (!fits(pattern, springRepresentation)) {
                        return 0
                    } else {
                        val patternLeft = springRepresentation.removeRange(0, pattern.length)
                        if (patternLeft.count { it == '#' } > damagedGroups.subList(list.size, damagedGroups.size).sum()) {
                            return 0
                        }
                    }
                }

                var validPatternSum = 0L
                val maxOffset = springRepresentation.length - damagedGroups.subList(list.size, damagedGroups.size).sumOf { it + 1 }
                for (offset in 0..maxOffset) {
                    val len = damagedGroups[list.size]
                    val damagedSegment = DamagedSegment(offset, len)
                    val cnt = getPossiblePatternCount(list + damagedSegment)
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

    check(part1(readInput("$FOLDER/test")) == 21)
    check(part2(readInput("$FOLDER/test")) == 525152L)

    val input = readInput("$FOLDER/input")
    val part1Result: Int
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