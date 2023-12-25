package day25

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day25"

fun main() {

    fun part1(input: List<String>): Int {
        val forwardLinks = input.associate { line ->
            val (device, connections) = line.split(": ")
            val cs = connections.split(" ")
            device to cs.toMutableList()
        }

        val links = forwardLinks.toMutableMap()

        forwardLinks.forEach { (device, connections) ->
            connections.forEach { c ->
                links.getOrPut(c) { mutableListOf() }.add(device)
            }
        }

        fun findPath(startDevice: String, endDevice: String, invalidConnections: List<Pair<String, String>>): List<String>? {

            //  part 1: verify that there is a path from startDevice to endDevice

            val visitedDevices = mutableSetOf(startDevice)
            var activatedDevices = setOf(startDevice)
            var found = false

            while (activatedDevices.isNotEmpty()) {
                val nextActivatedDevices = mutableSetOf<String>()

                for (activatedDevice in activatedDevices) {
                    for (connection in links[activatedDevice]!!) {

                        if (connection in visitedDevices || invalidConnections.any { it.first == activatedDevice && it.second == connection }) {
                            continue
                        }

                        if (connection == endDevice) {
                            found = true
                            break
                        }

                        nextActivatedDevices.add(connection)
                        visitedDevices.add(connection)
                    }
                }

                if (found) {
                    break
                }

                activatedDevices = nextActivatedDevices
            }

            if (!found) {
                return null
            }

            //  part 2: find the shortest path from startDevice to endDevice

            var activatedPaths = setOf(listOf(startDevice))
            visitedDevices.clear()

            while (activatedPaths.isNotEmpty()) {

                val nextActivatedPaths = mutableSetOf<List<String>>()

                for (activatedPath in activatedPaths) {
                    val lastDevice = activatedPath.last()

                    links[lastDevice]?.forEach { connection ->

                        if (connection in visitedDevices || invalidConnections.any { it.first == lastDevice && it.second == connection }) {
                            return@forEach
                        }

                        visitedDevices.add(lastDevice)

                        when (connection) {
                            endDevice -> {
                                return activatedPath + connection
                            }

                            in activatedPath -> {
                                return@forEach
                            }

                            else -> {
                                nextActivatedPaths.add(activatedPath + connection)
                            }
                        }
                    }
                }

                activatedPaths = nextActivatedPaths
            }

            return null
        }

        val devices = links.keys.toMutableList()
        val centerDevice = devices.removeFirst()

        val centerGroup = mutableSetOf(centerDevice)
        val otherGroup = mutableSetOf<String>()

        while (devices.isNotEmpty()) {
            "${centerGroup.size} vs ${otherGroup.size}".println()

            val connectedPairsOfCenterGroup = centerGroup.flatMap { s -> links[s]!!.map { t -> s to t } }
                .filter { (_, t) -> t !in centerGroup && t !in otherGroup }

            if (connectedPairsOfCenterGroup.isEmpty()) {
                otherGroup += devices
                devices.clear()
                break
            }

            for ((source, target) in connectedPairsOfCenterGroup.filter { (_, t) -> t !in otherGroup }) {

                devices.remove(target)

                val invalidConnections = mutableListOf(source to target)

                val path1 = findPath(centerDevice, target, invalidConnections.toList())!!

                path1.windowed(2).forEach { (from, to) ->
                    invalidConnections.add(from to to)
                }

                val path2 = findPath(centerDevice, target, invalidConnections.toList())!!

                path2.windowed(2).forEach { (from, to) ->
                    invalidConnections.add(from to to)
                }

                val path3 = findPath(centerDevice, target, invalidConnections.toList())

                if (path3 == null) {
                    otherGroup += target
                    continue
                }

                centerGroup += target
                break
            }
        }

        return centerGroup.size * otherGroup.size
    }

    check(part1(readInput("$FOLDER/test")) == 54)

    val input = readInput("$FOLDER/input")
    val part1Result: Int
    val part1Time = measureNanoTime {
        part1Result = part1(input)
    }

    println("Part 1 result: $part1Result")
    println("Part 1 takes ${part1Time / 1e6f} milliseconds.")
}