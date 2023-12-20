package day20

import println
import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day20"

sealed class Module(val connections: MutableList<Module> = mutableListOf()) {
    data class FlipFlop(val n: String, var isHigh: Boolean = false) : Module()
    data class Conj(val n: String, val conjMap: MutableMap<String, Boolean> = mutableMapOf()) : Module()
    data class Broadcaster(val n: String = "broadcaster") : Module()
    data class Button(val n: String = "button") : Module()
    data class Dummy(val n: String) : Module()

    val name: String
        get() = when (this) {
            is FlipFlop -> n
            is Conj -> n
            is Broadcaster -> n
            is Button -> n
            is Dummy -> n
        }
}

data class Signal(val isHigh: Boolean, val module: Module)

fun main() {

    fun parseModules(input: List<String>): List<Module> {
        val modules = mutableListOf<Module>()
        modules.add(Module.Button())
        modules.add(Module.Broadcaster())

        fun findName(line: String): String {
            return "\\w+".toRegex().find(line)!!.value
        }

        input.forEach { line ->
            when {
                line.startsWith("%") -> modules.add(Module.FlipFlop(findName(line)))
                line.startsWith("&") -> modules.add(Module.Conj(findName(line)))
            }
        }

        input.forEach { line ->
            val module = when {
                line.startsWith("%") -> modules.first { it is Module.FlipFlop && it.n == findName(line) }
                line.startsWith("&") -> modules.first { it is Module.Conj && it.n == findName(line) }
                else -> modules.first { it is Module.Broadcaster }
            }

            val connectionNames = line.split(" -> ")[1].split(", ")
            val connections = connectionNames.map { name ->
                modules.firstOrNull { it.name == name } ?: Module.Dummy(name).also { modules.add(it) }
            }

            module.connections.addAll(connections)
        }

        modules.filterIsInstance<Module.Conj>().forEach { conj ->
            conj.conjMap.putAll(modules.filter { it.connections.contains(conj) }.associate { it.name to false })
        }

        modules.first { it is Module.Button }.connections.add(modules.first { it is Module.Broadcaster })

        return modules
    }

    fun part1(input: List<String>): Long {

        val modules = parseModules(input)

        var signalHigh = 0
        var signalLow = 0

        repeat(1000) {

            var signals = mutableListOf(Signal(false, modules.first { it is Module.Button }))

            while (signals.isNotEmpty()) {
                val newSignals = mutableListOf<Signal>()

                val postConjUpdates = mutableListOf<Triple<Module.Conj, String, Boolean>>()

                signals.forEach { signal ->

                    when (signal.module) {
                        is Module.Button, is Module.Broadcaster, is Module.Dummy -> {
                            val signalSending = signal.isHigh
                            signal.module.connections.forEach {
                                newSignals.add(Signal(signalSending, it))
                                if (it is Module.Conj) {
                                    postConjUpdates.add(Triple(it, signal.module.name, signalSending))
                                }
                            }
                        }

                        is Module.FlipFlop -> {
                            if (signal.isHigh) {
                                return@forEach
                            }
                            signal.module.isHigh = !signal.module.isHigh
                            val signalSending = signal.module.isHigh
                            signal.module.connections.forEach {
                                newSignals.add(Signal(signalSending, it))
                                if (it is Module.Conj) {
                                    postConjUpdates.add(Triple(it, signal.module.name, signalSending))
                                }
                            }
                        }

                        is Module.Conj -> {
                            val signalSending = !signal.module.conjMap.all { it.value }
                            signal.module.connections.forEach {
                                newSignals.add(Signal(signalSending, it))
                                if (it is Module.Conj) {
                                    postConjUpdates.add(Triple(it, signal.module.name, signalSending))
                                }
                            }
                        }
                    }

                }

                postConjUpdates.forEach { (conj, name, signalSending) ->
                    conj.conjMap[name] = signalSending
                }
                postConjUpdates.clear()

                signalHigh += newSignals.count { it.isHigh }
                signalLow += newSignals.count { !it.isHigh }

                signals = newSignals
            }
        }

        return signalHigh.toLong() * signalLow.toLong()
    }

    fun part2(input: List<String>): Long {
        val modules = parseModules(input)

        var signalHigh = 0
        var signalLow = 0

        var buttonPressCount = 0L
        while (true) {
            buttonPressCount++

            if (buttonPressCount % 100000 == 0L) {
                println("Button press count: $buttonPressCount")
            }

            var signals = mutableListOf(Signal(false, modules.first { it is Module.Button }))

            while (signals.isNotEmpty()) {
                val newSignals = mutableListOf<Signal>()

                val postConjUpdates = mutableListOf<Triple<Module.Conj, String, Boolean>>()

                signals.forEach { signal ->

                    when (signal.module) {
                        is Module.Button, is Module.Broadcaster, is Module.Dummy -> {
                            val signalSending = signal.isHigh
                            signal.module.connections.forEach {
                                newSignals.add(Signal(signalSending, it))
                                if (it is Module.Conj) {
                                    postConjUpdates.add(Triple(it, signal.module.name, signalSending))
                                }
                            }
                        }

                        is Module.FlipFlop -> {
                            if (signal.isHigh) {
                                return@forEach
                            }
                            signal.module.isHigh = !signal.module.isHigh
                            val signalSending = signal.module.isHigh
                            signal.module.connections.forEach {
                                newSignals.add(Signal(signalSending, it))
                                if (it is Module.Conj) {
                                    postConjUpdates.add(Triple(it, signal.module.name, signalSending))
                                }
                            }
                        }

                        is Module.Conj -> {
                            val signalSending = !signal.module.conjMap.all { it.value }
                            signal.module.connections.forEach {
                                newSignals.add(Signal(signalSending, it))
                                if (it is Module.Conj) {
                                    postConjUpdates.add(Triple(it, signal.module.name, signalSending))
                                }
                            }
                        }
                    }

                }

                postConjUpdates.forEach { (conj, name, signalSending) ->
                    conj.conjMap[name] = signalSending
                }
                postConjUpdates.clear()

                signalHigh += newSignals.count { it.isHigh }
                signalLow += newSignals.count { !it.isHigh }

                signals = newSignals
            }

            if (signals.any { !it.isHigh && it.module.name == "rx" }) {
                break
            }
        }

        return buttonPressCount
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