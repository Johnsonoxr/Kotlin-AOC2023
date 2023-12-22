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
        val visited = mutableSetOf(start)

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

        var step = stepCount % (2 * len)

        val diff1 = mutableListOf<Long>()
        val diff2 = mutableListOf<Long>()
        val results = mutableListOf<Long>()

        while (diff2.lastOrNull() == null || diff2.last() != diff2.getOrNull(diff2.size - 2)) {

            step += 2 * len

            val rst = flood(rocks, len, len, startP, step.toInt()).count { p ->
                when (countEven) {
                    true -> p.x.positiveMod(2) == p.y.positiveMod(2)
                    else -> p.x.positiveMod(2) != p.y.positiveMod(2)
                }
            }.toLong()

            val diff = rst - (results.lastOrNull() ?: 0L)
            results.add(rst)
            diff1.add(diff)
            if (diff1.size > 1) {
                diff2.add(diff - diff1[diff1.size - 2])
            }

            "Step: $step, diff: ${diff1.lastOrNull()}, diff2: ${diff2.lastOrNull()}, rst: $rst".println()
        }

        val diffOfDiff = diff1.last() - diff1.getOrNull(diff1.size - 2)!!
        var cnt = results.last()
        var diff = diff1.last()

        while (step < stepCount) {
            step += 2 * len
            diff += diffOfDiff
            cnt += diff
        }

        return cnt
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