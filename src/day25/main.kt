package day25

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day25"

fun main() {
    fun test(input: List<String>): Int {
        val forwardLinks = input.associate { line ->
            val (device, connections) = line.split(": ")
            val cs = connections.split(" ")
            device to cs.toMutableList()
        }

        val backwardLinks = mutableMapOf<String, MutableList<String>>()

        forwardLinks.forEach { (device, connections) ->
            connections.forEach { c ->
                backwardLinks.getOrPut(c) { mutableListOf() }.add("-$device")
            }
        }

        val links = forwardLinks.toMutableMap()

        backwardLinks.forEach { (device, backwardConnections) ->
            links.getOrPut(device) { mutableListOf() }.addAll(backwardConnections)
        }
        val devices = links.keys.toList()

        val connections = links.map { (device, connections) ->
            connections.map { device to it }
        }.flatten().toList()

        val startDevice = devices.first()

        fun findPath(startDevice: String, endDevice: String, invalidConnections: List<Pair<String, String>>): List<Pair<String, String>>? {

            var activatedPaths = setOf(listOf("" to startDevice))

            do {
                val nextActivatedPaths = mutableSetOf<List<Pair<String, String>>>()

                activatedPaths.forEach { activatedPath ->
                    val lastDevice = activatedPath.last().second.removePrefix("-")

                    links[lastDevice]?.forEach { connection ->

                        if (invalidConnections.any { it.first == lastDevice && it.second == connection }) {
                            return@forEach
                        }

                        when (connection.removePrefix("-")) {
                            endDevice -> {
                                return (activatedPath + (lastDevice to connection)).drop(1)
                            }

                            in activatedPath.map { it.second.removePrefix("-") } -> {
                                return@forEach
                            }

                            else -> {
                                nextActivatedPaths.add(activatedPath + (lastDevice to connection))
                            }
                        }
                    }
                }

                activatedPaths = nextActivatedPaths
            } while (activatedPaths.isNotEmpty())

            return null
        }

        for (endDevice in devices.drop(1)) {
            "Testing $startDevice -> $endDevice".println()

            val cutOutConnections = mutableListOf<Pair<String, String>>()

            val path0 = findPath(startDevice, endDevice, cutOutConnections)!!

            path0.forEach { connection1 ->

                "Trying cut out connection: $connection1".println()

                cutOutConnections.add(connection1)

                val path1 = findPath(startDevice, endDevice, cutOutConnections)!!

                path1.forEach { connection2 ->
                    cutOutConnections.add(connection2)

                    val path2 = findPath(startDevice, endDevice, cutOutConnections)!!

                    path2.forEach { connection3 ->
                        cutOutConnections.add(connection3)

                        val path3 = findPath(startDevice, endDevice, cutOutConnections)

                        if (path3 == null) {
                            "Found connection: $connection1, $connection2, $connection3".println()
                            return 1
                        }

                        cutOutConnections.remove(connection3)
                    }

                    cutOutConnections.remove(connection2)
                }

                cutOutConnections.remove(connection1)
            }
        }

        return 1
    }

    fun part1(input: List<String>): Int {

        return test(input)

        val forwardLinks = input.associate { line ->
            val (device, connections) = line.split(": ")
            val cs = connections.split(" ")
            device to cs.toMutableList()
        }

        val backwardLinks = mutableMapOf<String, MutableList<String>>()

        forwardLinks.forEach { (device, connections) ->
            connections.forEach { c ->
                backwardLinks.getOrPut(c) { mutableListOf() }.add("-$device")
            }
        }

        val links = forwardLinks.toMutableMap()

        backwardLinks.forEach { (device, backwardConnections) ->
            links.getOrPut(device) { mutableListOf() }.addAll(backwardConnections)
        }

        fun linkTrace(links: Map<String, List<String>>, startingDevice: String = links.keys.first()): Set<String> {
            var currentDevice = listOf(startingDevice)
            val visitedDevices = mutableSetOf<String>()

            while (currentDevice.isNotEmpty()) {
                val nextDevices = currentDevice.flatMap { devices ->
                    visitedDevices.add(devices)
                    links[devices]?.map { it.removePrefix("-") } ?: emptyList()
                }.filter { it !in visitedDevices }
                currentDevice = nextDevices
            }

            return visitedDevices
        }

        val devices = links.keys
        val connections = links.map { (device, connections) ->
            connections.map { device to it }
        }.flatten().toList()

        "Devices count: ${devices.size}, links count: ${connections.size}".println()

        var iter = 0

        for (i in 0..connections.lastIndex - 2) {
            val c1 = connections[i]
            for (j in i + 1..<connections.lastIndex) {
                val c2 = connections[j]
                for (k in j + 1..connections.lastIndex) {
                    val c3 = connections[k]

                    if (++iter % 1000 == 0) {
                        "iter: $iter, cutting out $c1, $c2, $c3".println()
                    }

                    val newLinks: MutableMap<String, List<String>> = links.toMutableMap()
                    newLinks[c1.first] = newLinks[c1.first]!!.filter { !it.endsWith(c1.second) }
                    newLinks[c2.first] = newLinks[c2.first]!!.filter { !it.endsWith(c2.second) }
                    newLinks[c3.first] = newLinks[c3.first]!!.filter { !it.endsWith(c3.second) }

                    val connectedDevices = linkTrace(newLinks)
                    if (connectedDevices.size != devices.size) {
                        "connectedDevices part1: $connectedDevices".println()
                        "connectedDevices part2: ${devices - connectedDevices}".println()
                        return connectedDevices.size * (devices.size - connectedDevices.size)
                    }
                }
            }
        }

        return 1
    }

    fun part2(input: List<String>): Int {
        return 1
    }

//    check(part1(readInput("$FOLDER/test")) == 54)
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