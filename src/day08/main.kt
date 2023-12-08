package day08

import readInput
import kotlin.math.pow
import kotlin.system.measureNanoTime

private const val FOLDER = "day08"

fun main() {
    fun part1(input: List<String>): Int {

        val orders = input.first().map { if (it == 'L') 0 else 1 }

        val instr = input.drop(2).map {
            val ins = "[0-9A-Z]+".toRegex().findAll(it).map { s -> s.value }.toList()
            return@map ins[0] to ins.subList(1, 3)
        }.toMap()

        var current = "AAA"
        var step = 0
        while (current != "ZZZ") {
            current = instr[current]!![orders[step++ % orders.size]]
        }

        return step
    }

    fun part2(input: List<String>): Long {
        val orders = input.first().map { if (it == 'L') 0 else 1 }

        val instr = input.drop(2).map {
            val ins = "[0-9A-Z]+".toRegex().findAll(it).map { s -> s.value }.toList()
            return@map ins[0] to ins.subList(1, 3)
        }.toMap()

        val starts = instr.keys.filter { it.last() == 'A' }
        //  Try to calculate the offset that goes from "**A" to "**Z", and the repeat step that goes from "**Z" to "**Z".
        val loopInfoList = starts.map { start ->
            var offset = 0
            var current = start
            do {
                current = instr[current]!![orders[offset % orders.size]]
                offset++
            } while (!current.endsWith('Z'))
            val end = current

            var repeatStep = 0
            do {
                current = instr[current]!![orders[repeatStep % orders.size]]
                repeatStep++
            } while (current != end)

            return@map offset to repeatStep
        }
        loopInfoList.forEach { println(it) }

        //  Seems the offset and repeatStep are the same for all starts, so I'll just use the repeatStep.

        fun Int.factoring(): List<Int> {
            val list = mutableListOf<Int>()
            var n = this
            var i = 2
            while (i <= n) {
                if (n % i == 0) {
                    list.add(i)
                    n /= i
                } else {
                    i++
                }
            }
            return list
        }

        val factors = mutableMapOf<Int, Int>()
        loopInfoList.map { it.second.factoring().groupBy { f -> f } }.forEach {
            it.entries.forEach { entry ->
                factors[entry.key] = maxOf(factors.getOrDefault(entry.key, 0), entry.value.size)
            }
        }

        return factors.entries.map { (k, v) -> k.toDouble().pow(v).toLong() }.reduce { acc, l -> acc * l }
    }

    check(part1(readInput("$FOLDER/test")) == 2)
    check(part2(readInput("$FOLDER/test2")) == 6L)

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