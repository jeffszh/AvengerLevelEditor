package cn.jeff.app

import java.io.RandomAccessFile

data class RoleProperties(
		var name: String = "",
		var level: Int = 0
) {

	val nameOffset = 0x0042
	val levelOffset = 0x0160
	val recordSize = 0x0127

	fun loadFromFile(file: RandomAccessFile, recordNo: Int) {
	}

	fun saveLevelToFile(file: RandomAccessFile, recordNo: Int) {
	}

}
