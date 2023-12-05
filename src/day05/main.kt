package day05

import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day05"

fun main() {

    data class Factory(val map: Map<LongRange, Long>) {
        operator fun get(input: Long): Long {
            val rngEntry = map.entries.firstOrNull { input in it.key } ?: return input
            return rngEntry.value + (input - rngEntry.key.first)
        }

        operator fun get(input: List<LongRange>): List<LongRange> {
            val outputRanges = mutableListOf<LongRange>()
            input.forEach { range ->
                map.entries.firstOrNull {range.first in it.key && range.last in it.key }?.let {
                    outputRanges.add(get(range.first)..get(range.last))
                    return@forEach
                }
                map.entries.filter { range.first in it.key }.maxByOrNull { it.key.last }?.let {
                    outputRanges.add(get(it.key.first)..get(range.first - 1))
                    outputRanges.add(get(range.first)..get(it.key.last))
                    return@forEach
                }
                map.entries.filter { range.last in it.key }.minByOrNull { it.key.first }?.let {
                    outputRanges.add(get(it.key.first)..get(range.last))
                    outputRanges.add(get(range.last + 1)..get(it.key.last))
                    return@forEach
                }
            }
        }
    }

    fun getFactory(lines: List<String>): Factory {
        val productionMap = mutableMapOf<LongRange, Long>()
        for (line in lines) {
            val (dst, src, cnt) = line.split(" ")
            productionMap[src.toLong()..<(src.toLong() + cnt.toLong())] = dst.toLong()
        }
        return Factory(productionMap)
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
            var number = seed
            factories.forEach { productionMap ->
                number = productionMap[number]
            }
            numbers.add(number)
        }

        return numbers.min()
    }

    fun part2(input: List<String>): Long {
        val seeds = "\\d+".toRegex().findAll(input.first()).map { it.value.toLong() }.toList()

        val factories = getFactories(input)

        val numbers = mutableListOf<Long>()
        return 1
    }

    check(part1(readInput("$FOLDER/test")) == 35L)
    check(part2(readInput("$FOLDER/test")) == 1L)

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