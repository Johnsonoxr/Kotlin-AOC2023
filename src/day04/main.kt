package day04

import readInput
import kotlin.math.pow
import kotlin.system.measureNanoTime

private const val FOLDER = "day04"

fun main() {
    fun part1(input: List<String>): Int {
        return input.map { line ->
            val (winingNumbersStr, numbersStr) = line.split(": ")[1].split(" | ")
            val winNs = winingNumbersStr.split(" ").filter { it.isNotEmpty() }.map { it.toInt() }
            val ns = numbersStr.split(" ").filter { it.isNotEmpty() }.map { it.toInt() }
            val win = winNs.count { it in ns }
            val score = if (win > 0) {
                2.0.pow(win - 1).toInt()
            } else {
                0
            }
            return@map score
        }.sum()
    }

    fun part2(input: List<String>): Int {
        val scratchCards = input.map { line ->
            val (winNsStr, nsStr) = line.split(": ")[1].split(" | ")
            return@map Pair(
                winNsStr.split(" ").filter { it.isNotEmpty() }.map { it.toInt() },
                nsStr.split(" ").filter { it.isNotEmpty() }.map { it.toInt() }
            )
        }
        val cardCounts = scratchCards.indices.associateWith { 1 }.toMutableMap()
        for (cardIdx in scratchCards.indices) {
            val currentCard = scratchCards[cardIdx]
            val win = currentCard.first.count { it in currentCard.second }
            for (winIdx in (cardIdx + 1)..(cardIdx + win)) {
                cardCounts[winIdx] = (cardCounts[winIdx] ?: 0) + cardCounts[cardIdx]!!
            }
        }
        return cardCounts.values.sum()
    }

    check(part1(readInput("$FOLDER/test")) == 13)
    check(part2(readInput("$FOLDER/test")) == 30)

    val input = readInput("$FOLDER/input")
    val part1Result: Int
    val part1Time = measureNanoTime {
        part1Result = part1(input)
    }
    val part2Result: Int
    val part2Time = measureNanoTime {
        part2Result = part2(input)
    }

    println("Part 1 result: $part1Result")
    println("Part 2 result: $part2Result")
    println("Part 1 takes ${part1Time / 1e6f} milliseconds.")
    println("Part 2 takes ${part2Time / 1e6f} milliseconds.")
}