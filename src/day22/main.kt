package day22

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day22"

data class P(val x: Int, val y: Int, var z: Int)

data class Brick(val p1: P, val p2: P) {
    val supportingBricks = mutableListOf<Brick>()
    val landingBricks = mutableListOf<Brick>()
}

fun main() {

    fun dropBricks(input: List<String>): List<Brick> {
        val bricks = input.map { line ->
            val xyz = "\\d+".toRegex().findAll(line).map { it.value.toInt() }.toList()
            Brick(P(xyz[0], xyz[1], xyz[2]), P(xyz[3], xyz[4], xyz[5]))
        }

        val fallingBricks = bricks.sortedBy { it.p1.z }.toMutableList()

        val landedBricks = mutableListOf<Brick>()

        while (fallingBricks.isNotEmpty()) {
            val postLandBricks = mutableListOf<Brick>()
            for (i in fallingBricks.indices) {
                val brick = fallingBricks[i]

                if (brick.p1.z == 1) {
                    postLandBricks.add(brick)
                    continue
                }

                val supportingBricks = (landedBricks + postLandBricks).filter { landedBrick ->
                    brick.p1.z == landedBrick.p2.z + 1 &&
                            brick.p1.x <= landedBrick.p2.x &&
                            brick.p2.x >= landedBrick.p1.x &&
                            brick.p1.y <= landedBrick.p2.y &&
                            brick.p2.y >= landedBrick.p1.y
                }

                if (supportingBricks.isNotEmpty()) {
                    postLandBricks.add(brick)
                    supportingBricks.forEach { it.supportingBricks.add(brick) }
                    brick.landingBricks.addAll(supportingBricks)
                    continue
                }

                brick.p1.z--
                brick.p2.z--
            }
            landedBricks.addAll(postLandBricks)
            fallingBricks.removeAll(postLandBricks)
        }
        return landedBricks
    }

    fun part1(input: List<String>): Int {

        val bricks = dropBricks(input)

        return bricks.count {
            it.supportingBricks.isEmpty()
                    || it.supportingBricks.all { supportingBrick -> supportingBrick.landingBricks.size > 1 }
        }
    }

    fun part2(input: List<String>): Int {

        val bricks = dropBricks(input)

        return bricks.sortedBy { it.p2.z }.sumOf { brick ->

            val landingCountMap = bricks.associateWith { it.landingBricks.size }.toMutableMap()

            var fallingCount = 0

            var fallingBricks = brick.supportingBricks.filter { supportingBrick ->
                supportingBrick.landingBricks.size == 1
            }.toSet()

            while (fallingBricks.isNotEmpty()) {
                fallingCount += fallingBricks.size

                fallingBricks.forEach { fb ->
                    fb.supportingBricks.forEach { sb ->
                        landingCountMap[sb] = landingCountMap[sb]!! - 1
                    }
                }

                val nextFallingBricks = fallingBricks.flatMap { fb ->
                    fb.supportingBricks.filter { sb ->
                        landingCountMap[sb] == 0
                    }
                }.toSet()

                fallingBricks = nextFallingBricks
            }

            fallingCount
        }
    }

    check(part1(readInput("$FOLDER/test")) == 5)
    check(part2(readInput("$FOLDER/test")) == 7)

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