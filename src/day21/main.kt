package day21

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day21"

data class P(val y: Long, val x: Long) {
    val info: MutableMap<String, Any> by lazy {
        mutableMapOf()
    }

    fun neighbors(): List<P> {
        return listOf(
            P(y - 1, x),
            P(y + 1, x),
            P(y, x - 1),
            P(y, x + 1)
        )
    }
}

class InfiniteGraph<T>(data: Map<P, T>, val height: Long, val width: Long) {
    val data = data.toMutableMap()

    operator fun get(p: P): T? {
        return data[p]
    }

    operator fun set(p: P, value: T) {
        data[p] = value
    }
}

fun Long.positiveMod(other: Long): Long {
    return ((this % other) + other) % other
}

fun main() {

    fun flood(rocks: Set<P>, height: Long, width: Long, start: P, stepCount: Int): Set<P> {
        var seeds = mutableSetOf(start)
        val visited = mutableSetOf<P>()

        repeat(stepCount) {

            val nextSeeds = mutableSetOf<P>()
            seeds.forEach { seed ->
                val neighborSeeds = seed.neighbors()

                neighborSeeds.forEach neighborLoop@{ neighborSeed ->

                    if (neighborSeed in visited) {
                        return@neighborLoop
                    }

                    val modNeighborSeed = P(neighborSeed.y.positiveMod(height), neighborSeed.x.positiveMod(width))
                    if (modNeighborSeed in rocks) {
                        return@neighborLoop
                    }

                    nextSeeds.add(neighborSeed)
                }
            }

            seeds = nextSeeds
            visited.addAll(nextSeeds)
        }

        return visited
    }

    fun solve(input: List<String>, stepCount: Long): Long {

        val map = InfiniteGraph(input.mapIndexed { y, s ->
            s.mapIndexed { x, c ->
                P(y.toLong(), x.toLong()) to c
            }
        }.flatten().toMap(), input.size.toLong(), input[0].length.toLong())

        val startP = map.data.filter { it.value == 'S' }.keys.first()
        val rocks = map.data.filter { it.value == '#' }.keys.toSet()

        val isStartAtEven = (startP.x % 2 == startP.y % 2)
        val countEven = isStartAtEven == (stepCount % 2 == 0L)

        if (stepCount < map.width.toInt() * 2) {
            return flood(rocks, map.height, map.width, startP, stepCount.toInt()).count { p ->
                when (countEven) {
                    true -> p.x % 2 == p.y % 2
                    else -> p.x % 2 != p.y % 2
                }
            }.toLong()
        }

        val len = map.width
        val chunkCount: Long = stepCount / len
        val oddChunkCount = ((chunkCount - 1) / 2 * 2 + 1).let { it * it }
        val evenChunkCount = (chunkCount / 2 * 2).let { it * it }

        "Chunk count: $chunkCount, odd chunk count: $oddChunkCount, even chunk count: $evenChunkCount".println()

        val edgeFilter = when (chunkCount % 2 == 0L) {
            true -> { p: P -> p.x % 2 == p.y % 2 }
            else -> { p: P -> p.x % 2 != p.y % 2 }
        }

        val range = 0..<len
        val doubleRange = 0..<(len * 2)

        fun LongRange.offset(offset: Long): LongRange {
            return (start + offset)..(endInclusive + offset)
        }

        val floodTest = flood(rocks, len, len, startP, (stepCount % len + len * 2).toInt())

        val oddFilled = floodTest.count { it.x in range && it.y in range && it.x % 2 == it.y % 2 }
        val evenFilled = floodTest.count { it.x in range && it.y in range && it.x % 2 != it.y % 2 }

        val upNonFilled = floodTest.count { it.x in range && it.y in doubleRange.offset(-3 * len) && edgeFilter(it) }
        val bottomNonFilled = floodTest.count { it.x in range && it.y in doubleRange.offset(2 * len) && edgeFilter(it) }
        val leftNonFilled = floodTest.count { it.x in doubleRange.offset(-3 * len) && it.y in range && edgeFilter(it) }
        val rightNonFilled = floodTest.count { it.x in doubleRange.offset(2 * len) && it.y in range && edgeFilter(it) }

        val leftTopSlope1 = floodTest.count { it.x in range.offset(-len) && it.y in range.offset(-2 * len) && edgeFilter(it) }
        val leftTopSlope2 = floodTest.count { it.x in range.offset(-len) && it.y in range.offset(-len) && edgeFilter(it) }
        val leftTopSlope = (leftTopSlope1 + leftTopSlope2) * chunkCount - leftTopSlope2

        val rightTopSlope1 = floodTest.count { it.x in range.offset(len) && it.y in range.offset(-2 * len) && edgeFilter(it) }
        val rightTopSlope2 = floodTest.count { it.x in range.offset(len) && it.y in range.offset(-len) && edgeFilter(it) }
        val rightTopSlope = (rightTopSlope1 + rightTopSlope2) * chunkCount - rightTopSlope2

        val leftBottomSlope1 = floodTest.count { it.x in range.offset(-len) && it.y in range.offset(2 * len) && edgeFilter(it) }
        val leftBottomSlope2 = floodTest.count { it.x in range.offset(-len) && it.y in range.offset(len) && edgeFilter(it) }
        val leftBottomSlope = (leftBottomSlope1 + leftBottomSlope2) * chunkCount - leftBottomSlope2

        val rightBottomSlope1 = floodTest.count { it.x in range.offset(len) && it.y in range.offset(2 * len) && edgeFilter(it) }
        val rightBottomSlope2 = floodTest.count { it.x in range.offset(len) && it.y in range.offset(len) && edgeFilter(it) }
        val rightBottomSlope = (rightBottomSlope1 + rightBottomSlope2) * chunkCount - rightBottomSlope2

        val filledCount = oddFilled * oddChunkCount + evenFilled * evenChunkCount
        val nonFilledCount = upNonFilled + bottomNonFilled + leftNonFilled + rightNonFilled +
                leftTopSlope + rightTopSlope + leftBottomSlope + rightBottomSlope

        "Upper non-filled: $upNonFilled, bottom non-filled: $bottomNonFilled, left non-filled: $leftNonFilled, right non-filled: $rightNonFilled".println()
        "Left top slope: $leftTopSlope, right top slope: $rightTopSlope, left bottom slope: $leftBottomSlope, right bottom slope: $rightBottomSlope".println()

        "Filled count: $filledCount, non-filled count: $nonFilledCount, total: ${filledCount + nonFilledCount}".println()

        return filledCount + nonFilledCount
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