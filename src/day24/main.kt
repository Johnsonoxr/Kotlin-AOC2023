package day24

import com.johnsonoxr.exnumber.ExFloat
import com.johnsonoxr.exnumber.ExFloat.Companion.toExFloat
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day24"

private val ZERO = 0.toExFloat()

fun main() {

    data class P(val x: ExFloat, val y: ExFloat, val z: ExFloat) {
        operator fun plus(other: P) = P(x + other.x, y + other.y, z + other.z)
        operator fun minus(other: P) = P(x - other.x, y - other.y, z - other.z)
        operator fun div(other: ExFloat) = P(x / other, y / other, z / other)
        operator fun times(other: ExFloat) = P(x * other, y * other, z * other)
    }

    data class Hailstone(val position: P, val velocity: P)

    fun calcIntersectionXy(hailstone1: Hailstone, hailstone2: Hailstone): P? {
        val p1 = hailstone1.position
        val p2 = hailstone2.position
        val v1 = hailstone1.velocity
        val v2 = hailstone2.velocity

        val det = v2.x * v1.y - v2.y * v1.x

        if (det.isZero()) return null

        val p1xV1yMinusP1yV1x = p1.x * v1.y - p1.y * v1.x
        val p2xV2yMinusP2yV2x = p2.x * v2.y - p2.y * v2.x

        val x = p1xV1yMinusP1yV1x * v2.x - p2xV2yMinusP2yV2x * v1.x
        val y = p1xV1yMinusP1yV1x * v2.y - p2xV2yMinusP2yV2x * v1.y

        return P(x / det, y / det, ZERO)
    }

    fun calcIntersectionXyz(stone1: Hailstone, stone2: Hailstone): P? {

        val x1 = stone1.position.x
        val y1 = stone1.position.y
        val z1 = stone1.position.z

        val x2 = stone2.position.x
        val y2 = stone2.position.y
        val z2 = stone2.position.z

        val dx1 = stone1.velocity.x
        val dy1 = stone1.velocity.y
        val dz1 = stone1.velocity.z

        val dx2 = stone2.velocity.x
        val dy2 = stone2.velocity.y
        val dz2 = stone2.velocity.z

        var t: ExFloat? = null
        var u: ExFloat? = null

        //  t * dx1 + x1 = u * dx2 + x2
        //  t * dy1 + y1 = u * dy2 + y2
        //  t * dz1 + z1 = u * dz2 + z2
        when {
            dx1.isZero() && !dx2.isZero() -> u = (x1 - x2) / dx2
            dy1.isZero() && !dy2.isZero() -> u = (y1 - y2) / dy2
            dz1.isZero() && !dz2.isZero() -> u = (z1 - z2) / dz2
        }

        when {
            dx2.isZero() && !dx1.isZero() -> t = (x2 - x1) / dx1
            dy2.isZero() && !dy1.isZero() -> t = (y2 - y1) / dy1
            dz2.isZero() && !dz1.isZero() -> t = (z2 - z1) / dz1
        }

        if (t != null && u == null) {
            when {
                !dx2.isZero() -> u = (t * dx1 + x1 - x2) / dx2
                !dy2.isZero() -> u = (t * dy1 + y1 - y2) / dy2
                !dz2.isZero() -> u = (t * dz1 + z1 - z2) / dz2
            }
        }

        if (u != null && t == null) {
            when {
                !dx1.isZero() -> t = (u * dx2 + x2 - x1) / dx1
                !dy1.isZero() -> t = (u * dy2 + y2 - y1) / dy1
                !dz1.isZero() -> t = (u * dz2 + z2 - z1) / dz1
            }
        }

        //  t * dy1 + y1 = u * dy2 + y2
        //  t = (u * dy2 + y2 - y1) / dy1

        //  u * dx2 + x2 = t * dx1 + x1
        //  u * dx2 + x2 - x1 = dx1 * (t)
        //  u * dx2 + x2 - x1 = dx1 * (u * dy2 + y2 - y1) / dy1
        //  u * dx2 + x2 - x1 = u * dx1 * dy2 / dy1 + y2 * dx1 / dy1 - y1 * dx1 / dy1
        //  u * dx2 - u * dx1 * dy2 / dy1 = y2 * dx1 / dy1 - y1 * dx1 / dy1 - x2 + x1
        //  u * (dx2 - dx1 * dy2 / dy1) = (y2 * dx1 / dy1 - y1 * dx1 / dy1 - x2 + x1)
        if (t == null && u == null) {
            u = (y2 * dx1 / dy1 - y1 * dx1 / dy1 - x2 + x1) / (dx2 - dy2 * dx1 / dy1)
            t = (u * dy2 + y2 - y1) / dy1
        }

        if (t == null || u == null) throw IllegalStateException("Cannot happen")

        val intersectionFrom1 = P(t * dx1 + x1, t * dy1 + y1, t * dz1 + z1)
        val intersectionFrom2 = P(u * dx2 + x2, u * dy2 + y2, u * dz2 + z2)

        return when {
            intersectionFrom1 != intersectionFrom2 -> null
            else -> intersectionFrom1
        }
    }

    fun parseHailstones(input: List<String>): List<Hailstone> {
        return input.map { line ->
            val ns = "[-\\d]+".toRegex().findAll(line).map { it.value.toExFloat() }.toList()
            Hailstone(P(ns[0], ns[1], ns[2]), P(ns[3], ns[4], ns[5]))
        }
    }

    fun part1(input: List<String>, range: LongRange): Int {

        val hailstones = parseHailstones(input)

        val collisionPairs = mutableSetOf<Pair<Hailstone, Hailstone>>()

        val rangeStart = range.first.toExFloat()
        val rangeEnd = range.last.toExFloat()

        for (index in 0..<hailstones.lastIndex) {
            val h1 = hailstones[index]

            for (h2 in hailstones.subList(index + 1, hailstones.size)) {

                val intersection = calcIntersectionXy(h1, h2) ?: continue

                if (intersection.x < rangeStart
                    || intersection.x > rangeEnd
                    || intersection.y < rangeStart
                    || intersection.y > rangeEnd
                ) {
                    continue
                }

                if ((intersection.x - h1.position.x).sign != h1.velocity.x.sign
                    || (intersection.x - h2.position.x).sign != h2.velocity.x.sign
                ) {
                    continue
                }

                collisionPairs.add(Pair(h1, h2))
            }
        }
        return collisionPairs.size
    }

    fun part2(input: List<String>): Long {

        fun crossProduct(p1: P, p2: P): P {
            return P(
                p1.y * p2.z - p1.z * p2.y,
                p1.z * p2.x - p1.x * p2.z,
                p1.x * p2.y - p1.y * p2.x
            )
        }

        val hailstones = parseHailstones(input)

        val stone0 = hailstones[1]

        val shiftedStones = (hailstones - stone0).map { stone ->
            val p = stone.position - stone0.position
            val v = stone.velocity - stone0.velocity
            return@map when {
                p.x.isZero() || v.x.isZero() -> null
                else -> Hailstone(p, v)
            }
        }.filterNotNull()

        val shiftedStone1 = shiftedStones[0]
        val shiftedStone2 = shiftedStones[1]

        val planeNormal1 = crossProduct(shiftedStone1.position, shiftedStone1.position + shiftedStone1.velocity)
        val planeNormal2 = crossProduct(shiftedStone2.position, shiftedStone2.position + shiftedStone2.velocity)

        val rockNormalizedV = crossProduct(planeNormal1, planeNormal2)

        val rockNormalized = Hailstone(P(ZERO, ZERO, ZERO), rockNormalizedV)

        val intersectionOfStone1 = calcIntersectionXyz(rockNormalized, shiftedStone1)!!
        val intersectionOfStone2 = calcIntersectionXyz(rockNormalized, shiftedStone2)!!

        val time1 = (intersectionOfStone1 - shiftedStone1.position).x / shiftedStone1.velocity.x
        val time2 = (intersectionOfStone2 - shiftedStone2.position).x / shiftedStone2.velocity.x

        val rockV = (intersectionOfStone2 - intersectionOfStone1) / (time2 - time1)
        val rockP = intersectionOfStone1 - rockV * time1

        val originalRockP = rockP + stone0.position

        return originalRockP.x.toLong() + originalRockP.y.toLong() + originalRockP.z.toLong()
    }

    check(part1(readInput("$FOLDER/test"), range = 7L..27L) == 2)
    check(part2(readInput("$FOLDER/test")) == 47L)

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