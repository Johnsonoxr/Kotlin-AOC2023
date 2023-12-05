package day05

import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day05"

fun main() {

    data class Range(val start: Long, val end: Long) {
        operator fun contains(other: Range): Boolean {
            return this.start <= other.start && this.end >= other.end
        }

        fun isIntersect(other: Range): Boolean {
            return this.start <= other.end && this.end >= other.start
        }

        fun intersect(other: Range): Range {
            return Range(
                start = maxOf(this.start, other.start),
                end = minOf(this.end, other.end)
            )
        }
    }

    data class Aabb(val value: Long, val isStart: Boolean)

    fun merge(ranges1: List<Range>, ranges2: List<Range>, revRange2: Boolean = false, depthCriteria: Int = 1): List<Range> {
        val result = mutableListOf<Range>()
        val aabbLists = ranges1.map { Aabb(it.start, true) } +
                ranges1.map { Aabb(it.end + 1, false) } +
                ranges2.map { Aabb(it.start, !revRange2) } +
                ranges2.map { Aabb(it.end + 1, revRange2) }
        val sortedAabbPtList = aabbLists.sortedBy { it.value }
        var start: Long? = null
        var depth = 0
        for (aabb in sortedAabbPtList) {
            depth += if (aabb.isStart) 1 else -1
            if (depth >= depthCriteria && start == null) {
                start = aabb.value
            } else if (depth < depthCriteria && start != null) {
                if (start < aabb.value) {
                    result.add(Range(start, aabb.value - 1))
                }
                start = null
            }
        }
        return result
    }

    fun List<Range>.cutout(other: List<Range>): List<Range> {
        return merge(this, other, revRange2 = true)
    }

    fun List<Range>.union(other: List<Range>): List<Range> {
        return merge(this, other)
    }

    fun List<Range>.intersect(other: List<Range>): List<Range> {
        return merge(this, other, depthCriteria = 2)
    }

    data class Factory(private val rangePairs: List<Pair<Range, Long>>) {
        private val opRanges = rangePairs.map { it.first }

        fun operate(input: List<Range>): List<Range> {
            val noOpRanges = input.cutout(opRanges)
            val intersectRanges = input.cutout(noOpRanges)

            val outputRanges = mutableListOf<Range>()
            intersectRanges.forEach { intersectRange ->
                rangePairs.filter { intersectRange.isIntersect(it.first) }.forEach { (iptRange, optStart) ->
                    val partialIntersectRange = intersectRange.intersect(iptRange)
                    val opRange = Range(
                        start = partialIntersectRange.start + optStart - iptRange.start,
                        end = partialIntersectRange.end + optStart - iptRange.start
                    )
                    outputRanges.add(opRange)
                }
            }
            return outputRanges.union(noOpRanges)
        }
    }

    fun getFactory(lines: List<String>): Factory {
        val rangePairList = mutableListOf<Pair<Range, Long>>()
        for (line in lines) {
            val (dst, src, cnt) = line.split(" ")
            rangePairList.add(
                Pair(
                    Range(
                        start = src.toLong(),
                        end = src.toLong() + cnt.toLong() - 1
                    ),
                    dst.toLong()
                )
            )
        }
        return Factory(rangePairList)
    }

    fun getFactories(input: List<String>): List<Factory> {
        val factories = mutableListOf<Factory>()
        val lines = mutableListOf<String>()
        input.drop(2).forEach { line ->
            if (line.endsWith(":")) {
                return@forEach
            }
            if (line.isEmpty()) {
                factories.add(getFactory(lines))
                lines.clear()
            } else {
                lines.add(line)
            }
        }
        factories.add(getFactory(lines))
        return factories
    }

    fun part1(input: List<String>): Long {

        val seeds = "\\d+".toRegex().findAll(input.first()).map { it.value.toLong() }.toList()

        val factories = getFactories(input)

        val numbers = mutableListOf<Long>()

        seeds.forEach { seed ->
            var seedRanges = listOf(Range(seed, seed))
            factories.forEach { factory ->
                seedRanges = factory.operate(seedRanges)
            }
            numbers.add(seedRanges.first().start)
        }

        return numbers.min()
    }

    fun part2(input: List<String>): Long {
        val seedExpressions = "\\d+".toRegex().findAll(input.first()).map { it.value.toLong() }.toList()
        val seedRangeList = seedExpressions.chunked(2).map { Range(it[0], it[0] + it[1] - 1) }

        val factories = getFactories(input)

        val outputRanges = mutableListOf<Range>()

        seedRangeList.forEach { seedRange ->
            var seedRanges = listOf(seedRange)
            factories.forEach { factory ->
                seedRanges = factory.operate(seedRanges)
            }
            outputRanges.addAll(seedRanges)
        }

        return outputRanges.minOf { it.start }
    }

    check(part1(readInput("$FOLDER/test")) == 35L)
    check(part2(readInput("$FOLDER/test")) == 46L)

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