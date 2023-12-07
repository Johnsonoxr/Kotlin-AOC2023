package day07

import readInput
import kotlin.system.measureNanoTime

private const val FOLDER = "day07"

fun main() {

    fun part1(input: List<String>): Int {

        fun Char.order() = when (this) {
            'A' -> 14
            'K' -> 13
            'Q' -> 12
            'J' -> 11
            'T' -> 10
            else -> this.toString().toInt()
        }

        data class Hand(val card: List<Char>, val bid: Int) {
            val orders: List<Int> by lazy {
                val cardOrders = card.map { it.order() }
                val group = card.groupBy { it }
                val g5 = group.filter { it.value.size == 5 }
                val g4 = group.filter { it.value.size == 4 }
                val g3 = group.filter { it.value.size == 3 }
                val g2 = group.filter { it.value.size == 2 }

                when {
                    g5.size == 1 -> listOf(7) + cardOrders
                    g4.size == 1 -> listOf(6) + cardOrders
                    g3.size == 1 && g2.size == 1 -> listOf(5) + cardOrders
                    g3.size == 1 -> listOf(4) + cardOrders
                    g2.size == 2 -> listOf(3) + cardOrders
                    g2.size == 1 -> listOf(2) + cardOrders
                    else -> listOf(1) + cardOrders
                }
            }
        }

        val hands = input.map { line ->
            val (card, bid) = line.split(" ")
            Hand(card.toCharArray().toList(), bid.toInt())
        }

        val sortedHands = hands.sortedWith(object : Comparator<Hand> {
            override fun compare(o1: Hand, o2: Hand): Int {
                val o1Orders = o1.orders
                val o2Orders = o2.orders
                for (i in o1Orders.indices) {
                    if (o1Orders[i] != o2Orders[i]) {
                        return o1Orders[i] - o2Orders[i]
                    }
                }
                return 0
            }
        })

        return sortedHands.mapIndexed { index, hand -> hand.bid * (index + 1) }.sum()
    }

    fun part2(input: List<String>): Int {
        fun Char.order() = when (this) {
            'A' -> 14
            'K' -> 13
            'Q' -> 12
            'J' -> 1
            'T' -> 10
            else -> this.toString().toInt()
        }

        data class Hand(val card: List<Char>, val bid: Int) {
            val orders: List<Int> by lazy {
                val cardOrders = card.map { it.order() }
                val group = card.groupBy { it }
                val g5 = group.filter { it.value.size == 5 }
                val g4 = group.filter { it.value.size == 4 }
                val g3 = group.filter { it.value.size == 3 }
                val g2 = group.filter { it.value.size == 2 }

                return@lazy if ('J' !in card) {
                    when {
                        g5.size == 1 -> listOf(7) + cardOrders
                        g4.size == 1 -> listOf(6) + cardOrders
                        g3.size == 1 && g2.size == 1 -> listOf(5) + cardOrders
                        g3.size == 1 -> listOf(4) + cardOrders
                        g2.size == 2 -> listOf(3) + cardOrders
                        g2.size == 1 -> listOf(2) + cardOrders
                        else -> listOf(1) + cardOrders
                    }
                } else {
                    when {
                        g5.size == 1 -> listOf(7) + cardOrders
                        g4.size == 1 -> listOf(7) + cardOrders
                        g3.size == 1 && g2.size == 1 -> listOf(7) + cardOrders
                        g3.size == 1 -> listOf(6) + cardOrders
                        g2.size == 2 -> {
                            if ('J' in g2.keys) {
                                listOf(6) + cardOrders
                            } else {
                                listOf(5) + cardOrders
                            }
                        }
                        g2.size == 1 -> listOf(4) + cardOrders
                        else -> listOf(2) + cardOrders
                    }
                }
            }
        }

        val hands = input.map { line ->
            val (card, bid) = line.split(" ")
            Hand(card.toCharArray().toList(), bid.toInt())
        }

        val sortedHands = hands.sortedWith(object : Comparator<Hand> {
            override fun compare(o1: Hand, o2: Hand): Int {
                val o1Orders = o1.orders
                val o2Orders = o2.orders
                for (i in o1Orders.indices) {
                    if (o1Orders[i] != o2Orders[i]) {
                        return o1Orders[i] - o2Orders[i]
                    }
                }
                return 0
            }
        })

        return sortedHands.mapIndexed { index, hand -> hand.bid * (index + 1) }.sum()
    }

    check(part1(readInput("$FOLDER/test")) == 6440)
    check(part2(readInput("$FOLDER/test")) == 5905)

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