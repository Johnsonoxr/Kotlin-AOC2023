package day15

import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day15"

fun main() {

    fun String.hash(): Int {
        var hash = 0
        for (i in indices) {
            hash = (hash + this[i].code) * 17 % 256
        }
        return hash
    }

    fun part1(input: List<String>): Int {
        return input.first().split(",").sumOf { it.hash() }
    }

    fun part2(input: List<String>): Int {

        fun String.name(): String {
            return "[a-z]+".toRegex().find(this)!!.value
        }

        val boxes = Array(256) { mutableListOf<String>() }

        input.first().split(",").forEach { operation ->
            val name = operation.name()
            val box = boxes[name.hash()]
            val op = operation.substring(name.length)
            when (op) {
                "-" -> box.removeIf { it.startsWith(name) }
                else -> {
                    val replaceIdx = box.indexOfFirst { it.name() == name }
                    if (replaceIdx == -1) {
                        box.add(operation)
                    } else {
                        box[replaceIdx] = operation
                    }
                }
            }
        }

        return boxes.withIndex().sumOf { (boxIdx, box) ->
            var sum = 0
            box.withIndex().forEach { (slotIdx, operation) ->
                val focalLength = operation.split("=")[1].toInt()
                sum += (boxIdx + 1) * (slotIdx + 1) * focalLength
            }
            return@sumOf sum
        }
    }

    check(part1(readInput("$FOLDER/test")) == 1320)
    check(part2(readInput("$FOLDER/test")) == 145)

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