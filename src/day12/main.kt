package day12

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day12"

fun main() {

    val matchMap = mutableMapOf<String, Long>()

    fun String.isMatch(targetPattern: String): Boolean {
        if (this.length != targetPattern.length) return false
        return zip(targetPattern).all { (src, tgt) -> '?' == tgt || src == tgt }
    }

    fun calcPattern(pattern: String, groups: List<Int>): Long {
        if (groups.isEmpty()) {
            return if ('#' in pattern) 0 else 1
        }
        val groupsPattern = groups.joinToString(".") { "#".repeat(it) }
        if (groupsPattern.length > pattern.length) {
            return -1
        }
        if (groupsPattern.length == pattern.length) {
            return if (groupsPattern.isMatch(pattern)) 1 else 0
        }

        if (groups.size == 1) {
            return (0..(pattern.length - groupsPattern.length)).count { offset ->
                groupsPattern.padStart(offset + groupsPattern.length, '.').padEnd(pattern.length, '.').isMatch(pattern)
            }.toLong()
        }

        val cntList = mutableListOf<Long>()

        val offsetEnd = pattern.indexOfFirst { it == '#' }.takeIf { it >= 0 } ?: pattern.length
        for (offset in 0..offsetEnd) {
            val testPattern1 = ".".repeat(offset) + "#".repeat(groups.first()) + "."
            if (testPattern1.length > pattern.length) {
                break
            }
            val targetPattern1 = pattern.substring(0, testPattern1.length)
            if (!testPattern1.isMatch(targetPattern1)) {
                continue
            }

            val nextTestPattern = pattern.substring(testPattern1.length)
            val nextTestGroups = groups.drop(1)
            val key = "$nextTestPattern-$nextTestGroups"
            val cnt = matchMap.getOrPut(key) {
                calcPattern(nextTestPattern, nextTestGroups)
            }
            if (cnt < 0) {
                break
            }
            cntList.add(cnt)
        }

        return cntList.sum()
    }

    fun calcPatternList(patterns: List<String>, groups: List<Int>): Long {
        if (patterns.size == 1) {
            return calcPattern(patterns.first(), groups)
        }

        val cntList = mutableListOf<Long>()
        for (groupIdx in 0..groups.size) {

            val groups1 = groups.subList(0, groupIdx)
            val groups2 = groups.subList(groupIdx, groups.size)
            val part1Cnt = calcPattern(patterns.first(), groups1)

            if (part1Cnt == 0L) {   // no match
                continue
            }

            if (part1Cnt < 0) { // overflow
                break
            }

            val pattern2 = patterns.drop(1)
            val key = "${pattern2.joinToString(".")}-$groups2"
            val part2Cnt = matchMap.getOrPut(key) {
                calcPatternList(pattern2, groups2)
            }

            if (part2Cnt <= 0) {
                continue
            }
            cntList.add(part1Cnt * part2Cnt)
        }
        return cntList.sum()
    }

    fun calc(pattern: String, groups: List<Int>): Long {
        val patternList = pattern.split(".").filter { it.isNotBlank() }
        return calcPatternList(patternList, groups)
    }

    fun part1(input: List<String>): Long {

        val cntList = mutableListOf<Long>()
        input.forEach { line ->
            val pattern = line.split(" ")[0]
            val damagedGroups = line.split(" ")[1].split(",").map { it.toInt() }
            val cnt = calc(pattern, damagedGroups)
            cntList.add(cnt)
        }

        return cntList.sum()
    }

    fun part2(input: List<String>): Long {
        val cntList = mutableListOf<Long>()
        input.forEachIndexed { idx, line ->
            val pattern = line.split(" ")[0]
            val extendedPattern = List(5) { pattern }.joinToString("?")
            val damagedGroups = line.split(" ")[1].split(",").map { it.toInt() }
            val extendedGroups = List(5) { damagedGroups }.flatten()
            "#$idx: $extendedPattern  vs  $extendedGroups".println()
            val cnt = calc(extendedPattern, extendedGroups)
            cntList.add(cnt)
        }

        return cntList.sum()
    }

    check(part1(readInput("$FOLDER/test")) == 21L)
    check(part2(readInput("$FOLDER/test")) == 525152L)

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