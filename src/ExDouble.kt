import ExDouble.Companion.toExDouble
import kotlin.math.*
import kotlin.system.measureNanoTime

class ExDouble private constructor(
    val partitions: List<Int> = emptyList(),
    val expOffset: Int = 0,
    private val positive: Boolean = true
) {

    companion object {
        const val EXP_MAX = 100000
        const val DIGIT = 5
        const val DIV_PRECISION = 100

        private var globalStringConverter: StringConverter = StringConverter.DEFAULT

        fun setGlobalStringConverter(stringConverter: StringConverter) {
            globalStringConverter = stringConverter
        }

        fun Long.toExDouble() = fromString(this.toString())
        fun Double.toExDouble() = fromString(this.toString())
        fun Float.toExDouble() = fromString(this.toString())
        fun Int.toExDouble() = fromString(this.toString())
        fun String.toExDouble() = fromString(this)

        private fun fromString(string: String): ExDouble {
            val floatMatch = "(-?)(\\d+)\\.(\\d+)".toRegex().matchEntire(string)
            if (floatMatch != null) {
                val (signStr, intStr, decimalStr) = floatMatch.destructured
                val exp = -decimalStr.length
                val expRem = (exp % DIGIT + DIGIT) % DIGIT
                var nStr = (intStr + decimalStr) + "0".repeat(expRem)
                nStr = "0".repeat(DIGIT - nStr.length % DIGIT) + nStr
                val nList = nStr.chunked(DIGIT).map { it.toInt() }.asReversed()
                val expOffset = (expRem - exp) / DIGIT
                val (trimmedList, newExpOffset) = trim(nList, expOffset)
                return ExDouble(
                    partitions = trimmedList,
                    expOffset = newExpOffset,
                    positive = signStr.isEmpty()
                )
            }

            val intMatch = "(-?)(\\d+)".toRegex().matchEntire(string)
            if (intMatch != null) {
                val (signStr, intStr) = intMatch.destructured
                val nList = intStr.padStart(((intStr.length - 1) / DIGIT + 1) * DIGIT, '0').chunked(DIGIT).map { it.toInt() }.asReversed()
                val (trimmedList, expOffset) = trim(nList, 0)
                return ExDouble(
                    partitions = trimmedList,
                    expOffset = expOffset,
                    positive = signStr.isEmpty()
                )
            }

            val expIntMatch = "(-?)(\\d+)[e|E](-?\\d+)".toRegex().matchEntire(string)
            if (expIntMatch != null) {
                val (signStr, intStr, expStr) = expIntMatch.destructured
                val exp = expStr.toInt()
                val expRem = (exp % DIGIT + DIGIT) % DIGIT
                var nStr = intStr + "0".repeat(expRem)
                nStr = "0".repeat(DIGIT - nStr.length % DIGIT) + nStr
                val nList = nStr.chunked(DIGIT).map { it.toInt() }.asReversed()
                val expOffset = (expRem - exp) / DIGIT
                val (trimmedList, newExpOffset) = trim(nList, expOffset)
                return ExDouble(
                    partitions = trimmedList,
                    expOffset = newExpOffset,
                    positive = signStr.isEmpty()
                )
            }

            val expFloatMatch = "(-?)(\\d+)\\.(\\d+)[e|E](-?\\d+)".toRegex().matchEntire(string)
            if (expFloatMatch != null) {
                val (signStr, intStr, decimalStr, expStr) = expFloatMatch.destructured
                val exp = expStr.toInt() - decimalStr.length
                val expRem = (exp % DIGIT + DIGIT) % DIGIT
                var nStr = intStr + decimalStr + "0".repeat(expRem)
                nStr = "0".repeat(DIGIT - nStr.length % DIGIT) + nStr
                val nList = nStr.chunked(DIGIT).map { it.toInt() }.asReversed()
                val expOffset = (expRem - exp) / DIGIT
                val (trimmedList, newExpOffset) = trim(nList, expOffset)
                return ExDouble(
                    partitions = trimmedList,
                    expOffset = newExpOffset,
                    positive = signStr.isEmpty()
                )
            }

            throw IllegalArgumentException("Invalid string: $string")
        }

        private fun trim(partitions: List<Int>, exp: Int): Pair<List<Int>, Int> {
            val trimStart = partitions.indexOfFirst { it != 0 }
            return when (trimStart) {
                -1 -> emptyList<Int>() to 0
                else -> partitions.subList(trimStart, partitions.indexOfLast { it != 0 } + 1) to exp - trimStart
            }
        }
    }

    fun toLong(): Long {
        if (this.sign == 0) return 0L

        val expEnd = max(this.partitions.size - this.expOffset, 0)

        var result = 0L
        for (i in expEnd - 1 downTo 0) {
            result = result * EXP_MAX + this.getPartition(i)
        }
        return if (this.positive) result else -result
    }

    fun toDouble(): Double {
        if (this.sign == 0) return 0.0

        val expEnd = max(this.partitions.size - this.expOffset, 0)
        val expStart = min(-this.expOffset, 0)

        var result = 0.0
        for (i in expEnd - 1 downTo expStart) {
            result = result * EXP_MAX + this.getPartition(i)
        }
        result *= 10.0.pow(expStart * DIGIT)
        return if (this.positive) result else -result
    }

    private fun getPartition(exp: Int): Int {
        return partitions.getOrElse(expOffset + exp) { 0 }
    }

    private fun expShiftLeft(exp: Int): ExDouble {
        return ExDouble(partitions, expOffset - exp, positive)
    }

    operator fun plus(other: ExDouble): ExDouble {
        if (this.sign == 0) return other
        if (other.sign == 0) return this
        if (this.positive && !other.positive) return this - -other
        if (!this.positive && other.positive) return other - -this

        val partitions = mutableListOf<Int>()
        val expStart = min(-this.expOffset, -other.expOffset)
        val expEnd = max(this.partitions.size - this.expOffset, other.partitions.size - other.expOffset)

        var carry = 0
        for (i in expStart until expEnd) {
            val sum = this.getPartition(i) + other.getPartition(i) + carry
            partitions.add(sum % EXP_MAX)
            carry = sum / EXP_MAX
        }
        if (carry != 0) {
            partitions.add(carry)
        }

        val (trimpartitions, trimExp) = trim(partitions, -expStart)

        return ExDouble(trimpartitions, trimExp, this.positive)
    }

    operator fun minus(other: ExDouble): ExDouble {
        if (this.sign == 0) return -other
        if (other.sign == 0) return this
        if (this.positive != other.positive) return this + -other

        if (this.compareTo(other) == 0) return ExDouble()

        val partitions = mutableListOf<Int>()
        val expStart = min(-this.expOffset, -other.expOffset)
        val expEnd = max(this.partitions.size - this.expOffset, other.partitions.size - other.expOffset)

        val forwardMinus = this > other == this.positive
        val (minuend, subtrahend) = if (forwardMinus) this to other else other to this

        var carry = 0
        for (i in expStart until expEnd) {
            val diff = minuend.getPartition(i) - subtrahend.getPartition(i) - carry
            partitions.add(if (diff < 0) diff + EXP_MAX else diff)
            carry = if (diff < 0) 1 else 0
        }
        if (carry != 0) {
            partitions.add(carry)
        }

        val (trimpartitions, trimExp) = trim(partitions, -expStart)

        return ExDouble(trimpartitions, trimExp, forwardMinus == this.positive)
    }

    operator fun times(other: ExDouble): ExDouble {
        if (this.sign == 0 || other.sign == 0) return ExDouble()

        val partitions = mutableListOf<Int>()

        this.partitions.forEachIndexed { thisIdx, n ->
            other.partitions.forEachIndexed { otherIdx, m ->
                val product = n.toLong() * m.toLong()
                var idx = thisIdx + otherIdx
                val sum = product + partitions.getOrElse(idx) { 0 }

                when {
                    idx < partitions.size -> partitions[idx] = (sum % EXP_MAX).toInt()
                    else -> partitions.add((sum % EXP_MAX).toInt())
                }
                var carry = (sum / EXP_MAX).toInt()

                while (carry != 0) {
                    val sum2 = carry + partitions.getOrElse(++idx) { 0 }
                    when {
                        idx < partitions.size -> partitions[idx] = sum2 % EXP_MAX
                        else -> partitions.add(sum2 % EXP_MAX)
                    }
                    carry = sum2 / EXP_MAX
                }
            }
        }

        val (trimpartitions, trimExp) = trim(partitions, this.expOffset + other.expOffset)

        return ExDouble(trimpartitions, trimExp, this.positive == other.positive)
    }

    operator fun div(other: ExDouble): ExDouble {
        if (other.sign == 0) throw ArithmeticException("Division by zero")
        if (this.sign == 0) return this

        val positiveThis = if (this.positive) this else -this
        val positiveOther = if (other.positive) other else -other

        val revPartitions = mutableListOf<Int>()

        val thisMaxExp = positiveThis.partitions.size - positiveThis.expOffset
        val otherMaxExp = positiveOther.partitions.size - positiveOther.expOffset
        var expDecay = thisMaxExp - otherMaxExp + 1

        var remainder = positiveThis

        val divHead: Long = positiveOther.partitions.last().toLong()

        while (remainder.sign != 0 && revPartitions.size * DIGIT < DIV_PRECISION) {

            val remHead: Long = remainder.getPartition(expDecay + otherMaxExp).toLong() * EXP_MAX * EXP_MAX +
                    remainder.getPartition(expDecay + otherMaxExp - 1).toLong() * EXP_MAX +
                    remainder.getPartition(expDecay + otherMaxExp - 2).toLong()

            expDecay--

            var divUpperBound = remHead / divHead

            if (divUpperBound == 0L) {
                revPartitions.add(0)
                continue
            }

            val upperBoundValue = positiveOther * divUpperBound.toExDouble().expShiftLeft(expDecay)

            if (upperBoundValue <= remainder) {
                revPartitions.add(divUpperBound.toInt())
                for (idx in revPartitions.lastIndex downTo 1) {
                    if (revPartitions[idx] < EXP_MAX) break
                    val carry = revPartitions[idx] / EXP_MAX
                    revPartitions[idx] %= EXP_MAX
                    revPartitions[idx - 1] += carry
                }
                remainder -= upperBoundValue
                continue
            }

            var divLowerBound = remHead / (divHead + 1)

            while (divLowerBound + 1 < divUpperBound) {
                val dMiddle = (divUpperBound + divLowerBound) / 2
                val m = positiveOther * dMiddle.toExDouble().expShiftLeft(expDecay)

                when (remainder.compareTo(m)) {
                    1 -> divLowerBound = dMiddle
                    -1 -> divUpperBound = dMiddle

                    0 -> {
                        divLowerBound = dMiddle
                        divUpperBound = dMiddle
                    }
                }
            }

            if (divLowerBound == 0L) {
                revPartitions.add(0)
                continue
            }

            revPartitions.add(divLowerBound.toInt())
            for (idx in revPartitions.lastIndex downTo 1) {
                if (revPartitions[idx] < EXP_MAX) break
                val carry = revPartitions[idx] / EXP_MAX
                revPartitions[idx] %= EXP_MAX
                revPartitions[idx - 1] += carry
            }

            remainder -= positiveOther * divLowerBound.toExDouble().expShiftLeft(expDecay)
        }

        val (trimpartitions, trimExp) = trim(revPartitions.asReversed(), -expDecay)

        return ExDouble(trimpartitions, trimExp, this.positive == other.positive)
    }

    operator fun compareTo(other: ExDouble): Int {
        if (this.sign != other.sign) return this.sign - other.sign

        val expStart = min(-this.expOffset, -other.expOffset)
        val expEnd = max(this.partitions.size - this.expOffset, other.partitions.size - other.expOffset)

        for (i in expEnd - 1 downTo expStart) {
            val thispartition = this.getPartition(i)
            val otherpartition = other.getPartition(i)
            if (thispartition != otherpartition) {
                return if (this.positive) (thispartition - otherpartition).sign else (otherpartition - thispartition).sign
            }
        }
        return 0
    }

    operator fun unaryMinus(): ExDouble {
        return ExDouble(partitions, expOffset, !positive)
    }

    override fun toString(): String {
        return globalStringConverter.convert(this)
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is ExDouble -> this.compareTo(other) == 0
            else -> false
        }
    }

    override fun hashCode(): Int {
        return this.toString().hashCode()
    }

    fun content(): String {
        return "ExDouble(pos=$positive, expOffset=$expOffset, partitions=$partitions)"
    }

    val sign: Int = when {
        partitions.isEmpty() || partitions.all { it == 0 } -> 0
        positive -> 1
        else -> -1
    }

    fun isZero(): Boolean {
        return sign == 0
    }

    interface StringConverter {

        companion object {
            val DEFAULT: StringConverter = object : StringConverter {
                override fun convert(exDouble: ExDouble): String {
                    val expStart = min(-exDouble.expOffset, 0)
                    val expEnd = max(exDouble.partitions.size - exDouble.expOffset, 0)

                    val decimalStr = (-1 downTo expStart)
                        .joinToString("") { exDouble.getPartition(it).toString().padStart(DIGIT, '0') }
                        .trimEnd('0').ifEmpty { '0' }

                    val intStr = (expEnd - 1 downTo 0)
                        .joinToString("") { exDouble.getPartition(it).toString().padStart(DIGIT, '0') }
                        .trimStart('0').ifEmpty { '0' }

                    return "${if (exDouble.positive) "" else "-"}${intStr}.${decimalStr}"
                }
            }
        }

        fun convert(exDouble: ExDouble): String
    }
}

fun main() {
    // int test

//    val a = "-6.542117824767197E11"
//    val b = "-8.241934351445524E28"
//    "$a + $b = ${a.toDouble() + b.toDouble()}".println()
//    "${a.toExDouble().content()} vs ${b.toExDouble().content()}".println()
//    val rst = a.toExDouble() + b.toExDouble()
//    rst.println()
//    rst.content().println()
//    rst.toDouble().println()
//    exitProcess(0)
//
//    (10e17 * Math.PI).toExDouble().println()
//    exitProcess(0)

    val testCount = 50000
    val intRange = Int.MIN_VALUE..Int.MAX_VALUE
    val longRange = Long.MIN_VALUE..Long.MAX_VALUE

    val intPlusMinusTime = measureNanoTime {
        repeat(testCount) {
            val n1 = intRange.random()
            val n2 = intRange.random()
            val l1 = n1.toLong()
            val l2 = n2.toLong()
            val ex1 = n1.toExDouble()
            val ex2 = n2.toExDouble()
            val plusResult = ex1 + ex2
            if (plusResult.toLong() != (l1 + l2)) {
                "Test #${it + 1} failed".println()
                throw Exception("plus test failed on $n1 + $n2, expected: ${l1 + l2}, result: ${plusResult.toLong()}, content: ${plusResult.content()}")
            }
            val minusResult = ex1 - ex2
            if (minusResult.toLong() != (l1 - l2)) {
                "Test #${it + 1} failed".println()
                throw Exception("minus test failed on $n1 - $n2, expected: ${l1 - l2}, result: ${minusResult.toLong()}, content: ${minusResult.content()}")
            }
        }
    }
    "int plus/minus test passed, average time spent: ${intPlusMinusTime / 1000 / testCount}us".println()
    val intMultiplyTime = measureNanoTime {
        repeat(testCount) {
            val n1 = intRange.random()
            val n2 = intRange.random()
            val l1 = n1.toLong()
            val l2 = n2.toLong()
            val ex1 = n1.toExDouble()
            val ex2 = n2.toExDouble()
            val timesResult = ex1 * ex2
            if (timesResult.toLong() != (l1 * l2)) {
                "Test #${it + 1} failed".println()
                throw Exception("times test failed on $n1 * $n2, expected: ${l1 * l2}, result: ${timesResult.toLong()}, content: ${timesResult.content()}")
            }
        }
    }
    "int multiply test passed, average time spent: ${intMultiplyTime / 1000 / testCount}us".println()
    val intDivideTime = measureNanoTime {
        repeat(testCount) {
            val n1 = intRange.random()
            val n2 = intRange.random()
            val l1 = n1.toLong()
            val l2 = n2.toLong()
            val ex1 = n1.toExDouble()
            val ex2 = n2.toExDouble()
            val divideResult = ex1 / ex2
            if (divideResult.toLong() != (l1 / l2)) {
                "Test #${it + 1} failed".println()
                throw Exception("divide test failed on $n1 / $n2, expected: ${l1 / l2}, result: ${divideResult.toLong()}, content: ${divideResult.content()}")
            }
        }
    }
    "int divide test passed, average time spent: ${intDivideTime / 1000 / testCount}us".println()

    val doubleExpRange = -20..20
    val doublePlusMinusTime = measureNanoTime {
        repeat(testCount) {
            val l1 = longRange.random().toFloat() * 10.0.pow(doubleExpRange.random())
            val l2 = longRange.random().toFloat() * 10.0.pow(doubleExpRange.random())
            val ex1 = l1.toExDouble()
            val ex2 = l2.toExDouble()
            val expect = l1 + l2
            val plusResult = ex1 + ex2
            if (abs(plusResult.toDouble() - expect) > abs(expect) * 1e-10) {
                "Test #${it + 1} failed".println()
                throw Exception("plus test failed on $l1 + $l2, expected: ${l1 + l2}, result: ${plusResult.toDouble()}, content: ${plusResult.content()}")
            }
            val expectedMinus = l1 - l2
            val minusResult = ex1 - ex2
            if (abs(minusResult.toDouble() - expectedMinus) > abs(expectedMinus) * 1e-10) {
                "Test #${it + 1} failed".println()
                throw Exception("minus test failed on $l1 - $l2, expected: ${l1 - l2}, result: ${minusResult.toDouble()}, content: ${minusResult.content()}")
            }
        }
    }
    "double plus/minus test passed, average time spent: ${doublePlusMinusTime / 1000 / testCount}us".println()

    val doubleMultiplyTime = measureNanoTime {
        repeat(testCount) {
            val l1 = longRange.random().toFloat() * 10.0.pow(doubleExpRange.random())
            val l2 = longRange.random().toFloat() * 10.0.pow(doubleExpRange.random())
            val ex1 = l1.toExDouble()
            val ex2 = l2.toExDouble()
            val expect = l1 * l2
            val timesResult = ex1 * ex2
            if (abs(timesResult.toDouble() - expect) > abs(expect) * 1e-10) {
                "Test #${it + 1} failed".println()
                throw Exception("times test failed on $l1 * $l2, expected: ${l1 * l2}, result: ${timesResult.toDouble()}, content: ${timesResult.content()}")
            }
        }
    }
    "double multiply test passed, average time spent: ${doubleMultiplyTime / 1000 / testCount}us".println()

    val doubleDivideTime = measureNanoTime {
        repeat(testCount) {
            val l1 = longRange.random().toFloat() * 10.0.pow(doubleExpRange.random())
            val l2 = longRange.random().toFloat() * 10.0.pow(doubleExpRange.random())
            val ex1 = l1.toExDouble()
            val ex2 = l2.toExDouble()
            val expect = l1 / l2
            val divideResult = ex1 / ex2
            if (abs(divideResult.toDouble() - expect) > abs(expect) * 1e-10) {
                "Test #${it + 1} failed".println()
                throw Exception("divide test failed on $l1 / $l2, expected: ${l1 / l2}, result: ${divideResult.toDouble()}, content: ${divideResult.content()}")
            }
        }
    }
    "double divide test passed, average time spent: ${doubleDivideTime / 1000 / testCount}us".println()
}
