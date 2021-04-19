package cn.jeff.app

import kotlin.math.pow

infix fun Int.pow(p: Int): Int =
		if (p <= 0) 1 else this * (this pow (p - 1))

fun main() {
	println("开始。")
	println(3 pow 4)
	println(2.0.pow(5))
	val ba = ByteArray(12)
	ba[0] = 65
	ba[1] = 65
	ba[2] = 65
//	ba[3] = 65
	ba[4] = 65
	ba[9] = 66
	println(ba.dropLast(ba.size - ba.indexOfFirst {
		it == 0.toByte()
	}).size)
	println(ba.takeWhile {
		it != 0.toByte()
	}.size)
	println("结束。")
}
