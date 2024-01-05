package day24

import day24.LargeDouble.Companion.toLargeDouble
import println
import readInput
import kotlin.math.log10
import kotlin.math.pow
import kotlin.system.measureNanoTime

private const val FOLDER = "day24"

private val ZERO = LargeDouble(0.0)

class LargeDouble(val list: LongArray = LongArray(SIZE)) {

    companion object {
        private const val DIGIT = 18
        private const val SIZE = 11
        private val REV = 10.0.pow(DIGIT).toLong()
        private val REV_SQRT = 10.0.pow(DIGIT / 2).toLong()

        val MIN_VALUE = -REV.toDouble().pow(SIZE / 2)
        val MAX_VALUE = REV.toDouble().pow(SIZE / 2) - 1

        fun fromExponent(exponent: Int): LargeDouble {
            val arr = LongArray(SIZE)
            val f = exponent + DIGIT * (SIZE / 2)
            arr[SIZE - 1 - f / DIGIT] = (1..f % DIGIT).fold(1L) { acc, _ -> acc * 10 }
            return LargeDouble(arr)
        }

        fun Int.toLargeDoubleExponent() = fromExponent(this)

        fun Double.toLargeDouble() = LargeDouble(this)
    }

    constructor(double: Double) : this() {
        when {
            double < MIN_VALUE -> throw IllegalArgumentException("double $double is too small")
            double > MAX_VALUE -> throw IllegalArgumentException("double $double is too big")
            double == 0.0 -> return
            double > 0 -> {
                var rem = double
                for (index in list.indices) {
                    val factor = REV.toDouble().pow(index - SIZE / 2)
                    val l = (rem * factor).toLong()
                    list[index] = l
                    rem -= l / factor
                }
            }

            double < 0 -> {
                val n = -LargeDouble(-double)
                for (index in list.indices) {
                    list[index] = n.list[index]
                }
            }
        }
    }

    private fun approximate(): LargeDouble {
        val sign = this.sign()
        if (sign == 0) return this

        var nonZeroIdx = -1
        var nonNineIdx = -1
        list.drop(1).forEachIndexed { index, l ->
            if (l != 0L) {
                nonZeroIdx = index
            } else if (nonZeroIdx != -1) {
                val refinedList = list.take(index + 1) + List(SIZE - index - 1) { 0L }
                return LargeDouble(refinedList.toLongArray())
            }
            if (l != REV - 1) {
                nonNineIdx = index
            } else if (nonNineIdx != -1) {
                val refinedList = list.take(index + 1) + List(SIZE - index - 1) { 0L }
                return LargeDouble(refinedList.toLongArray()) + LargeDouble().also { it.list[index] = 1 }
            }
        }

        return this
    }

    fun toLong(): Long {
        return when {
            this > LargeDouble(Long.MAX_VALUE.toDouble()) -> throw IllegalStateException("Cannot convert $this to Long")
            this < LargeDouble(Long.MIN_VALUE.toDouble()) -> throw IllegalStateException("Cannot convert $this to Long")
            this == ZERO -> 0L
            this > ZERO -> list.drop(1).take(SIZE / 2).fold(0L) { acc, l -> acc * REV + l }
            else -> -((-this).toLong())
        }
    }

    fun sign(): Int {
        return when {
            list.all { it == 0L } -> 0
            list.first() > 1L -> -1
            else -> 1
        }
    }

    operator fun unaryMinus(): LargeDouble {
        if (this.sign() == 0) return this

        val arr = list.map { REV - 1 - it }.toLongArray()
        arr[SIZE - 1] = arr[SIZE - 1] + 1

        for (index in arr.lastIndex downTo 1) {
            val l = arr[index]
            if (l >= REV) {
                arr[index] = l - REV
                arr[index - 1] = arr[index - 1] + 1
            }
        }

        return LargeDouble(arr)
    }

    operator fun plus(other: LargeDouble): LargeDouble {
        val arr = list.zip(other.list).map { (a, b) -> a + b }.toLongArray()
        (arr.lastIndex downTo 1).forEach { bIndex ->
            val b = arr[bIndex]
            if (b >= REV) {
                arr[bIndex] = b - REV
                arr[bIndex - 1] = arr[bIndex - 1] + 1
            }
        }
        arr[0] = if (arr[0] % REV > 0) REV - 1 else 0
        return LargeDouble(arr)
    }

    operator fun minus(other: LargeDouble): LargeDouble {
        return this + (-other)
    }

    operator fun times(other: LargeDouble): LargeDouble {
        val thisSign = this.sign()
        val otherSign = other.sign()

        if (thisSign == 0 || otherSign == 0) return ZERO
        if (thisSign < 0 && otherSign < 0) return (-this * -other)
        if (thisSign < 0) return -(-this * other)
        if (otherSign < 0) return -(this * -other)

        val arr = LongArray(SIZE)
        list.forEachIndexed { aIndex, a ->
            other.list.forEachIndexed bLoop@{ bIndex, b ->
                val idx = aIndex + bIndex - SIZE / 2

                if (idx !in arr.indices) return@bLoop

                if (a < REV_SQRT && b < REV_SQRT) {
                    arr[idx] += a * b
                    return@bLoop
                }

                var curV = arr[idx]
                var overflowV = 0L

                for (i in 0..<DIGIT) {
                    val divider = 10.0.pow(i).toLong()

                    val dividedB = b / divider
                    if (dividedB == 0L) {
                        break
                    }

                    val divider2 = REV / divider

                    val v = (dividedB % 10) * a
                    curV += (v % divider2) * divider
                    overflowV += v / divider2

                    if (curV >= REV) {
                        overflowV += curV / REV
                        curV %= REV
                    }
                }

                arr[idx] = curV

                if (idx > 0) {
                    arr[idx - 1] += overflowV % REV
                }

                for (i in idx - 1 downTo 1) {
                    if (arr[i] >= REV) {
                        arr[i - 1] += arr[i] / REV
                        arr[i] %= REV
                    }
                }
            }
        }
        arr[0] = when {
            arr.drop(1).all { it == 0L } -> 0
            this.sign() == other.sign() -> 0
            else -> REV - 1
        }
        return LargeDouble(arr).approximate()
    }

    operator fun div(other: LargeDouble): LargeDouble {
        if (this.sign() == 0) return this
        if (other.sign() == 0) throw ArithmeticException("Divide by zero")
        if (this.sign() > 0 && other.sign() < 0) return -(this / -other)
        if (this.sign() < 0 && other.sign() > 0) return -(-this / other)
        if (this.sign() < 0 && other.sign() < 0) return (-this / -other)

        var rem = this
        var rst = LargeDouble()

        var exp = exp() - other.exp() + 1

        while (true) {
            val pow = exp.toLargeDoubleExponent()
            var d = 0
            for (i in 1..10) {
                val m = LargeDouble(i.toDouble()) * pow * other
                if (m > rem) {
                    d = i - 1
                    break
                }
            }

            if (d != 0) {
                val mul = LargeDouble(d.toDouble()) * pow
                rst += mul
                rem -= mul * other
            }

            if (rem == ZERO) {
                return rst
            }
            exp--

            if (exp < -(DIGIT * SIZE / 2)) {
                break
            }
        }
        return rst.approximate()
    }

    operator fun compareTo(other: LargeDouble): Int {
        val thisSign = this.sign()
        val otherSign = other.sign()
        return when {
            thisSign < otherSign -> -1
            thisSign > otherSign -> 1
            else -> {
                var rst: Int? = null
                for ((l1, l2) in list.zip(other.list).drop(1)) {
                    if (l1 < l2) {
                        rst = -1
                        break
                    }
                    if (l1 > l2) {
                        rst = 1
                        break
                    }
                }
                rst ?: 0
            }
        }
    }

    fun exp(): Int {
        val notZeroIdx = this.list.drop(1).indexOfFirst { it != 0L }
        return log10(list[notZeroIdx + 1].toDouble()).toInt() + (SIZE / 2 - 1 - notZeroIdx) * DIGIT
    }

    fun isZero(): Boolean {
        return list.drop(1).all { it == 0L } && list.first() == 0L || list.first() == REV
    }

    override fun toString(): String {
        return when (this.sign()) {
            0 -> "0"
            1 -> {
                val subList = list.drop(1)
                val integerStr = subList.take(SIZE / 2).joinToString("", transform = { it.toString().padStart(DIGIT, '0') }).trimStart('0')
                val decimalStr = subList.drop(SIZE / 2).joinToString("", transform = { it.toString().padStart(DIGIT, '0') }).trimEnd('0')
                val paddedIntegerStr = integerStr.ifEmpty { "0" }
                val paddedDecimalStr = decimalStr.ifEmpty { "0" }
                "$paddedIntegerStr.${paddedDecimalStr}"
            }

            else -> "-${-this}"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is LargeDouble) return false
        return this.compareTo(other) == 0
    }

    override fun hashCode(): Int {
        return list.hashCode()
    }
}

data class P(val x: LargeDouble, val y: LargeDouble, val z: LargeDouble) {
    operator fun plus(other: P) = P(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: P) = P(x - other.x, y - other.y, z - other.z)
    operator fun div(other: LargeDouble) = P(x / other, y / other, z / other)
    operator fun times(other: LargeDouble) = P(x * other, y * other, z * other)
}

data class Hailstone(val position: P, val velocity: P)

fun main() {

    "1: ${1.0.toLargeDouble()}".println()
    "2: ${2.0.toLargeDouble()}".println()
    "3: ${3.0.toLargeDouble()}".println()
    "-1: ${(-1.0).toLargeDouble()}".println()
    "-38: ${(-38.0).toLargeDouble()}".println()
    "-123456789123456: ${(-123456789123456.0).toLargeDouble()}".println()
    "321987456873*159357852697 = ${321987456873.0.toLargeDouble() * 159357852697.0.toLargeDouble()}".println()
    "123456789123456/522162348: ${123456789123456.0.toLargeDouble() / 522162348.0.toLargeDouble()}".println()

    fun calcIntersectionXy(hailstone1: Hailstone, hailstone2: Hailstone): P? {
        val p1 = hailstone1.position
        val p2 = hailstone2.position
        val v1 = hailstone1.velocity
        val v2 = hailstone2.velocity

        val det = v2.x * v1.y - v2.y * v1.x

        if (det.isZero()) return null

        val x = p1.x * v1.y * v2.x - p1.y * v1.x * v2.x - p2.x * v2.y * v1.x + p2.y * v2.x * v1.x
        val y = p1.x * v1.y * v2.y - p1.y * v1.x * v2.y - p2.x * v2.y * v1.y + p2.y * v2.x * v1.y

        return P(x / det, y / det, ZERO)
    }

    fun calcIntersectionXyz(stone1: Hailstone, stone2: Hailstone): P? {
        "=========================\ncalcIntersectionXyz".println()
        "stone1: $stone1".println()
        "stone2: $stone2".println()

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

        var t: LargeDouble? = null
        var u: LargeDouble? = null

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

        "part1 t: $t, u: $u".println()

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

        "part2 t: $t, u: $u".println()

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
            val ns = "[-\\d]+".toRegex().findAll(line).map { LargeDouble(it.value.toDouble()) }.toList()
            Hailstone(P(ns[0], ns[1], ns[2]), P(ns[3], ns[4], ns[5]))
        }
    }

    fun part1(input: List<String>, range: LongRange): Int {

        val hailstones = parseHailstones(input)

        val collisionPairs = mutableSetOf<Pair<Hailstone, Hailstone>>()

        for (index in 0..<hailstones.lastIndex) {
            "index: $index".println()
            val h1 = hailstones[index]

            for (h2 in hailstones.subList(index + 1, hailstones.size)) {

                val intersection = calcIntersectionXy(h1, h2) ?: continue

                if ((intersection.x - range.first.toDouble().toLargeDouble() < ZERO)
                    || (intersection.x - range.last.toDouble().toLargeDouble() > ZERO)
                    || (intersection.y - range.first.toDouble().toLargeDouble() < ZERO)
                    || (intersection.y - range.last.toDouble().toLargeDouble() > ZERO)
                ) {
                    continue
                }

                if ((intersection - h1.position).x.sign() != h1.velocity.x.sign()
                    || (intersection - h2.position).x.sign() != h2.velocity.x.sign()
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

        "---------------------------------------------".println()

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

        "shiftedStone1: $shiftedStone1".println()
        "shiftedStone2: $shiftedStone2".println()

        val planeNormal1 = crossProduct(shiftedStone1.position, shiftedStone1.position + shiftedStone1.velocity)
        val planeNormal2 = crossProduct(shiftedStone2.position, shiftedStone2.position + shiftedStone2.velocity)

        "planeNormal1: $planeNormal1".println()
        "planeNormal2: $planeNormal2".println()

        val rockNormalizedV = crossProduct(planeNormal1, planeNormal2)
        "rockNormalizedV: $rockNormalizedV".println()

//        (-rockNormalizedV.y / rockNormalizedV.x).println()
//
//        if (hailstones.size > 100) {
//            exitProcess(0)
//        }

        val rockNormalized = Hailstone(P(ZERO, ZERO, ZERO), rockNormalizedV)

        "rockNormalizedP: $rockNormalized".println()

        val intersectionOfStone1 = calcIntersectionXyz(rockNormalized, shiftedStone1)!!
        val intersectionOfStone2 = calcIntersectionXyz(rockNormalized, shiftedStone2)!!

        "intersectionOfStone1: $intersectionOfStone1".println()
        "intersectionOfStone2: $intersectionOfStone2".println()

        val time1 = (intersectionOfStone1 - shiftedStone1.position).x / shiftedStone1.velocity.x
        val time2 = (intersectionOfStone2 - shiftedStone2.position).x / shiftedStone2.velocity.x

        "time1: $time1, time2: $time2".println()

        val rockV = (intersectionOfStone2 - intersectionOfStone1) / (time2 - time1)
        val rockP = intersectionOfStone1 - rockV * time1

        "rockP: $rockP, rockV: $rockV".println()

        val originalRockP = rockP + stone0.position
        val originalRockV = rockV + stone0.velocity

        "originalRockP: $originalRockP".println()
        "originalRockV: $originalRockV".println()

        return originalRockP.x.toLong() + originalRockP.y.toLong() + originalRockP.z.toLong()
    }

    check(part1(readInput("$FOLDER/test"), range = 7L..27L) == 2)
    check(part2(readInput("$FOLDER/test")) == 47L)

    val input = readInput("$FOLDER/input")
//    val part1Result: Int
//    val part1Time = measureNanoTime {
//        part1Result = part1(input, range = 200000000000000L..400000000000000L)
//    }
    val part2Result: Long
    val part2Time = measureNanoTime {
        part2Result = part2(input)
    }

//    println("Part 1 result: $part1Result")
    println("Part 2 result: $part2Result")
//    println("Part 1 takes ${part1Time / 1e6f} milliseconds.")
    println("Part 2 takes ${part2Time / 1e6f} milliseconds.")
}