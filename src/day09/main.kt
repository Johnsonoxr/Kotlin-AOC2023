package day09

import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day09"

fun main() {
    fun part1(input: List<String>): Long {
        val seqList = input.map { it.split(" ").map { n -> n.toLong() } }

        val seqNextN = seqList.map { seq ->
            val lastDiffList = mutableListOf<Long>()
            var squeezedSeq = seq
            while (true) {
                val seqDiffList = squeezedSeq.windowed(2).map { (first, second) -> second - first }
                lastDiffList.add(seqDiffList.last())
                if (squeezedSeq.distinct().size == 1) {
                    break
                }
                squeezedSeq = seqDiffList
            }
            return@map seq.last() + lastDiffList.sum()
        }

        return seqNextN.sum()
    }

    fun part2(input: List<String>): Long {
        val seqList = input.map { it.split(" ").map { n -> n.toLong() } }

        val seqNextN = seqList.map { seq ->
            val firstDiffList = mutableListOf<Long>()
            var squeezedSeq = seq
            while (true) {
                val seqDiffList = squeezedSeq.windowed(2).map { (first, second) -> second - first }
                firstDiffList.add(seqDiffList.first())
                if (squeezedSeq.distinct().size == 1) {
                    break
                }
                squeezedSeq = seqDiffList
            }

            return@map seq.first() - firstDiffList.asReversed().reduce { acc, l -> l - acc }
        }

        return seqNextN.sum()
    }

    check(part1(readInput("$FOLDER/test")) == 114L)
    check(part2(readInput("$FOLDER/test")) == 2L)

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