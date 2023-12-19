package day19

import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day19"

data class Xmas(val x: Int, val m: Int, val a: Int, val s: Int)
data class Rule(val id: String, val rule: String)

fun main() {
    fun part1(input: List<String>): Int {

        val sep = input.indexOfFirst { it.isBlank() }
        val xmasList = input.subList(sep + 1, input.size).map {
            val (x, m, a, s) = "\\{x=(\\d+),m=(\\d+),a=(\\d+),s=(\\d+)}".toRegex().find(it)!!.destructured
            return@map Xmas(x.toInt(), m.toInt(), a.toInt(), s.toInt())
        }
        val rules = input.subList(0, sep).map { Rule(it.split("{")[0], it.split("{")[1].split("}")[0]) }

        fun Xmas.followRule(rule: String): String {

            if (":" !in rule) {
                return rule
            }

            val (discriminant, destination) = rule.split(":", limit = 2)

            val par = "\\w".toRegex().find(discriminant)!!.value
            val opIdx = discriminant.indexOf(par)
            val op = discriminant.substring(opIdx + 1, opIdx + 2)
            val opVar = discriminant.substring(opIdx + 2).toInt()

            val v = when (par) {
                "x" -> x
                "m" -> m
                "a" -> a
                "s" -> s
                else -> throw Exception("Unknown parameter $par")
            }

            val result = when (op) {
                ">" -> v > opVar
                "<" -> v < opVar
                else -> throw Exception("Unknown operator $op")
            }

            val (accepted, rejected) = destination.split(",", limit = 2)

            if (result) {
                return accepted
            }

            return followRule(rejected)
        }

        val acceptedXmasList = xmasList.filter { xmas ->
            var nextRule = xmas.followRule(rules.first { it.id == "in" }.rule)
            while (nextRule != "A" && nextRule != "R") {
                nextRule = xmas.followRule(rules.first { it.id == nextRule }.rule)
            }
            return@filter nextRule == "A"
        }

        return acceptedXmasList.sumOf { it.x + it.m + it.a + it.s }
    }

    fun part2(input: List<String>): Int {
        return 1
    }

    check(part1(readInput("$FOLDER/test")) == 19114)
    check(part2(readInput("$FOLDER/test")) == 1)

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