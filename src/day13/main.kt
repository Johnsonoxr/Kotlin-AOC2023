package day13

import println
import readInput
import kotlin.math.pow
import kotlin.system.measureNanoTime

private const val FOLDER = "day13"

fun main() {
    fun part1(input: List<String>): Int {

        val graphs = mutableListOf<MutableList<String>>(mutableListOf())
        input.forEach { line ->
            if (line.isBlank()) {
                graphs.add(mutableListOf())
            } else {
                graphs.last().add(line)
            }
        }

        var rowScore = 0
        var columnScore = 0
        graphs.forEach { graph ->
            val rows = graph
            for (i in 1..<rows.size) {
                if (rows.subList(0, i).reversed().zip(rows.subList(i, rows.size)).all { (left, right) -> left == right }) {
                    rowScore += i
                    break
                }
            }

            val columns = graph[0].indices.map { index -> graph.map { it[index] }.joinToString("") }
            for (i in 1..<columns.size) {
                if (columns.subList(0, i).reversed().zip(columns.subList(i, columns.size)).all { (left, right) -> left == right }) {
                    columnScore += i
                    break
                }
            }
        }

        "Row score: $rowScore, column score: $columnScore".println()

        return (rowScore * 100 + columnScore)
    }

    fun part2(input: List<String>): Int {
        val graphs = mutableListOf<MutableList<String>>(mutableListOf())
        input.forEach { line ->
            if (line.isBlank()) {
                graphs.add(mutableListOf())
            } else {
                graphs.last().add(line)
            }
        }

        var rowScore = 0
        var columnScore = 0
        graphs.forEach { graph ->
            val rows = graph
            for (i in 1..<rows.size) {
                if (rows.subList(0, i).reversed().zip(rows.subList(i, rows.size)).map { (left, right) -> if (left == right) 0 else 1 }.sum() <= 1) {
                    rowScore += i
                    break
                }
            }

            val columns = graph[0].indices.map { index -> graph.map { it[index] }.joinToString("") }
            for (i in 1..<columns.size) {
                if (columns.subList(0, i).reversed().zip(columns.subList(i, columns.size)).map { (left, right) -> if (left == right) 0 else 1 }.sum() <= 1) {
                    columnScore += i
                    break
                }
            }
        }

        "Row score: $rowScore, column score: $columnScore".println()

        return (rowScore * 100 + columnScore)
    }

    check(part1(readInput("$FOLDER/test")) == 405)
    check(part2(readInput("$FOLDER/test")) == 400)

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