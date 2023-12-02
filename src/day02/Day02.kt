package day02

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day02"

fun main() {

    data class GameSet(
        val red: Int,
        val blue: Int,
        val green: Int
    )

    data class Game(
        val id: Int,
        val gameSets: List<GameSet>
    )

    val idRegex = "Game ([0-9]+):".toRegex()
    val redRegex = "([0-9]+) red".toRegex()
    val blueRegex = "([0-9]+) blue".toRegex()
    val greenRegex = "([0-9]+) green".toRegex()

    fun parseGame(line: String): Game {
        val id = idRegex.find(line)!!.groupValues.last().toInt()
        val gameSets = line.split(":")[1].split(";").map gameSetsParser@{ gameSetStr ->
            val red = redRegex.find(gameSetStr)?.groupValues?.last()?.toInt() ?: 0
            val blue = blueRegex.find(gameSetStr)?.groupValues?.last()?.toInt() ?: 0
            val green = greenRegex.find(gameSetStr)?.groupValues?.last()?.toInt() ?: 0
            return@gameSetsParser GameSet(red, blue, green)
        }
        return Game(id, gameSets)
    }

    fun part1(input: List<String>, criteria: GameSet): Int {

        val games = input.map { parseGame(it) }

        return games.filter {
            it.gameSets.all { gameSet ->
                gameSet.red <= criteria.red && gameSet.green <= criteria.green && gameSet.blue <= criteria.blue
            }
        }.sumOf { it.id }
    }

    fun part2(input: List<String>): Int {

        val games = input.map { parseGame(it) }

        return games.map { game ->
            val maxRed = game.gameSets.maxOf { it.red }
            val maxGreen = game.gameSets.maxOf { it.green }
            val maxBlue = game.gameSets.maxOf { it.blue }
            return@map maxRed * maxGreen * maxBlue
        }.sum()
    }

    check(part1(readInput("${FOLDER}/test"), GameSet(red = 12, green = 13, blue = 14)) == 8)
    check(part2(readInput("${FOLDER}/test")) == 2286)

    val input = readInput("${FOLDER}/input")
    val part1Time = measureNanoTime {
        part1(input, GameSet(red = 12, green = 13, blue = 14)).println()
    }
    val part2Time = measureNanoTime {
        part2(input).println()
    }

    println("Part 1 takes ${part1Time / 1e6f} milliseconds.")
    println("Part 2 takes ${part2Time / 1e6f} milliseconds.")
}
