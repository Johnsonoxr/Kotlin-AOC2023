package day24

import println
import readInput
import kotlin.math.sign
import kotlin.system.measureNanoTime

private const val FOLDER = "day24"

data class P(val x: Double, val y: Double, val z: Double) {
    operator fun plus(other: P) = P(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: P) = P(x - other.x, y - other.y, z - other.z)
}

data class Hailstone(val position: P, val velocity: P)

fun main() {

    fun part1(input: List<String>, range: LongRange): Int {

        val hailstones = input.map { line ->
            val ns = "[-\\d]+".toRegex().findAll(line).map { it.value.toDouble() }.toList()
            Hailstone(P(ns[0], ns[1], ns[2]), P(ns[3], ns[4], ns[5]))
        }

        fun calcIntersectionXy(hailstone1: Hailstone, hailstone2: Hailstone): P? {
            val p1 = hailstone1.position
            val p2 = hailstone2.position
            val v1 = hailstone1.velocity
            val v2 = hailstone2.velocity

            val det = v2.x * v1.y - v2.y * v1.x

            if (det == 0.0) return null

            val x = p1.x * v1.y * v2.x - p1.y * v1.x * v2.x - p2.x * v2.y * v1.x + p2.y * v2.x * v1.x
            val y = p1.x * v1.y * v2.y - p1.y * v1.x * v2.y - p2.x * v2.y * v1.y + p2.y * v2.x * v1.y

            return P(x / det, y / det, 0.0)
        }

        val collisionPairs = mutableSetOf<Pair<Hailstone, Hailstone>>()

        for (index in 0..<hailstones.lastIndex) {
            val h1 = hailstones[index]

            for (h2 in hailstones.subList(index + 1, hailstones.size)) {

                val intersection = calcIntersectionXy(h1, h2) ?: continue

                if (intersection.x < range.first || intersection.x > range.last || intersection.y < range.first || intersection.y > range.last) {
                    continue
                }

                if ((intersection - h1.position).x.sign != h1.velocity.x.sign || (intersection - h2.position).x.sign != h2.velocity.x.sign) {
                    continue
                }

                collisionPairs.add(Pair(h1, h2))
            }
        }
        return collisionPairs.size
    }

    fun part2(input: List<String>): Long {
        return 1
    }

    check(part1(readInput("$FOLDER/test"), range = 7L..27L) == 2)
    check(part2(readInput("$FOLDER/test")) == 1L)

    val input = readInput("$FOLDER/input")
    val part1Result: Int
    val part1Time = measureNanoTime {
        part1Result = part1(input, range = 200000000000000L..400000000000000L)
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