package day20

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day20"

sealed class M(var fromConnections: List<M> = emptyList(), var toConnections: List<M> = emptyList(), var state: Boolean = false) {
    data class General(val n: String) : M()
    data class FlipFlop(val n: String) : M()
    data class Conj(val n: String) : M()

    val name: String
        get() = when (this) {
            is General -> n
            is FlipFlop -> n
            is Conj -> n
        }
}

fun main() {

    fun parseModules(input: List<String>): List<M> {
        val modules = mutableListOf<M>()
        modules.add(M.General("button"))
        modules.add(M.General("broadcaster"))

        fun findName(line: String): String {
            return "\\w+".toRegex().find(line)!!.value
        }

        input.forEach { line ->
            when {
                line.startsWith("%") -> modules.add(M.FlipFlop(findName(line)))
                line.startsWith("&") -> modules.add(M.Conj(findName(line)))
            }
        }

        input.forEach { line ->

            val module = modules.first { it.name == findName(line) }

            val connectionNames = line.split(" -> ")[1].split(", ")
            val connections = connectionNames.map { name ->
                modules.firstOrNull { it.name == name } ?: M.General(name).also { modules.add(it) }
            }

            module.toConnections = connections
        }

        modules.forEach { conj ->
            conj.fromConnections = modules.filter { it.toConnections.contains(conj) }
        }

        modules.first { it.name == "button" }.toConnections = modules.filter { it.name == "broadcaster" }

        return modules
    }

    fun part1(input: List<String>): Long {

        val modules = parseModules(input)

        data class Signal(val isHigh: Boolean, val module: M)

        var signalHigh = 0
        var signalLow = 0

        repeat(1000) {

            var signals = mutableListOf(Signal(false, modules.first { it.name == "button" }))

            while (signals.isNotEmpty()) {
                val newSignals = mutableListOf<Signal>()

                signals.forEach { signal ->

                    val newSignal: Boolean? = when (signal.module) {
                        is M.General -> signal.isHigh

                        is M.FlipFlop -> {
                            if (signal.isHigh) {
                                null
                            } else {
                                val signalSending = !signal.module.state
                                signal.module.state = signalSending
                                signalSending
                            }
                        }

                        is M.Conj -> {
                            val signalSending = !signal.module.fromConnections.all { it.state }
                            signal.module.state = signalSending
                            signalSending
                        }
                    }

                    if (newSignal != null) {
                        newSignals.addAll(signal.module.toConnections.map { Signal(newSignal, it) })
                    }
                }

                signalHigh += newSignals.count { it.isHigh }
                signalLow += newSignals.count { !it.isHigh }

                signals = newSignals
            }
        }

        return signalHigh.toLong() * signalLow.toLong()
    }

    fun part2(input: List<String>): Long {
        return 1
    }

    check(part1(readInput("$FOLDER/test")) == 11687500L)

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