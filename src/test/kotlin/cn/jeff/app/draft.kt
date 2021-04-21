package cn.jeff.app

import kotlin.math.pow

//infix fun Int.pow(p: Int): Int =
//		if (p <= 0) 1 else this * (this pow (p - 1))

// 改用尾递归来实现
tailrec fun intPow(base: Int, p: Int, v: Int): Int =
		if (p <= 0) v else intPow(base, p - 1, base * v)

infix fun Int.pow(p: Int) = intPow(this, p, 1)

// 用集合来实现
infix fun Int.power(p: Int): Int =
		(1..p + 1).reduce { acc, _ ->
			acc * this
		}

fun Int.factorial(): Int =
		if (this <= 0)
			1
		else
			(1..this).reduce { acc, i ->
				acc * i
			}

fun main() {
	println("开始。")
	println(3 pow 4)
	println(2.0.pow(5))
	println(5 power 4)
	println(5 power 3)
	println(5 power 2)
	println(5 power 1)
	println(5 power 0)
	println("---------------------")
	println(0.factorial())
	println(1.factorial())
	println(2.factorial())
	println(3.factorial())
	println(4.factorial())
	println(5.factorial())
	println("---------------------")
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
