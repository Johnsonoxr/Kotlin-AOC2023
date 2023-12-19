package day19

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day19"

data class Xmas(val x: Int, val m: Int, val a: Int, val s: Int)

data class Rule(val id: String, val param: Char, val op: Char, val v: Int, val acceptId: String, val rejectId: String)

fun main() {

    fun parseRules(input: List<String>): List<Rule> {
        val sep = input.indexOfFirst { it.isBlank() }
        var customRuleIdx = 1000

        val rules = mutableListOf<Rule>()

        input.subList(0, sep).forEach { line ->

            fun analyzeRules(id: String? = null, content: String): String {

                val (discriminant, destination) = content.split(":", limit = 2)
                val (acceptId, unknown) = destination.split(",", limit = 2)

                val (param, op, v) = "(\\w+)([<|>])(\\d+)".toRegex().find(discriminant)!!.destructured

                val rejectId = when {
                    unknown.count { it == ',' } == 0 -> unknown
                    else -> analyzeRules(content = unknown)
                }

                val idReturn = id ?: customRuleIdx++.toString()
                rules.add(Rule(idReturn, param[0], op[0], v.toInt(), acceptId, rejectId))

                return idReturn
            }

            analyzeRules(id = line.split("{")[0], content = line.split("{")[1].split("}")[0])
        }

        return rules
    }

    fun part1(input: List<String>): Int {

        val sep = input.indexOfFirst { it.isBlank() }
        val xmasList = input.subList(sep + 1, input.size).map {
            val (x, m, a, s) = "\\{x=(\\d+),m=(\\d+),a=(\\d+),s=(\\d+)}".toRegex().find(it)!!.destructured
            return@map Xmas(x.toInt(), m.toInt(), a.toInt(), s.toInt())
        }
        val rules = parseRules(input)

        fun Xmas.followRule(rule: Rule): String {

            val v = when (rule.param) {
                'x' -> x
                'm' -> m
                'a' -> a
                's' -> s
                else -> throw Exception("Unknown parameter ${rule.param}")
            }

            val accept = when (rule.op) {
                '>' -> v > rule.v
                '<' -> v < rule.v
                else -> throw Exception("Unknown operator ${rule.op}")
            }

            val nextId = if (accept) rule.acceptId else rule.rejectId

            return when {
                nextId in setOf("A", "R") -> nextId
                else -> followRule(rules.first { it.id == nextId })
            }
        }

        val firstRule = rules.first { it.id == "in" }

        val acceptedXmasList = xmasList.filter { xmas ->
            return@filter xmas.followRule(firstRule) == "A"
        }

        return acceptedXmasList.sumOf { it.x + it.m + it.a + it.s }
    }

    fun part2(input: List<String>): Long {

        val rules = parseRules(input)

        data class XmasRange(
            var x: IntRange = 1..4000,
            var m: IntRange = 1..4000,
            var a: IntRange = 1..4000,
            var s: IntRange = 1..4000,
        )

        val xmasRanges = mutableListOf<XmasRange>()


        fun IntRange.intersect(other: IntRange): IntRange {
            return (this.first.coerceAtLeast(other.first))..(this.last.coerceAtMost(other.last))
        }

        val ranges = mutableListOf<Pair<XmasRange, Rule>>()



        rules.filter { it.acceptId == "A" || it.rejectId == "A" }.forEach { finalRule ->
            val range = XmasRange()
            xmasRanges.add(range)

            val ruleRange = when {
                finalRule.acceptId == "A" && finalRule.rejectId == "A" -> 1..4000
                finalRule.acceptId == "A" && finalRule.op == '>' -> finalRule.v + 1..4000
                finalRule.acceptId == "A" && finalRule.op == '<' -> 1..<finalRule.v
                finalRule.rejectId == "A" && finalRule.op == '>' -> 1..finalRule.v
                finalRule.rejectId == "A" && finalRule.op == '<' -> finalRule.v..4000
                else -> throw Exception("Unknown rule $finalRule")
            }

            when (finalRule.param) {
                'x' -> range.x = range.x.intersect(ruleRange)
                'm' -> range.m = range.m.intersect(ruleRange)
                'a' -> range.a = range.a.intersect(ruleRange)
                's' -> range.s = range.s.intersect(ruleRange)
                else -> throw Exception("Unknown parameter ${finalRule.param}")
            }

            rules.filter { it.id == finalRule.id }.forEach { rule ->
                val copyRange = range.copy()
            }
        }

        return 1
    }

    check(part1(readInput("$FOLDER/test")) == 19114)
    check(part2(readInput("$FOLDER/test")) == 167409079868000L)

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