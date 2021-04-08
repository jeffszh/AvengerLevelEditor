package cn.jeff.app

import java.io.RandomAccessFile
import java.nio.charset.Charset

data class RoleProperties(
		val index: Int,
		var name: String = "",
		var level: Int = 0
) {

	companion object {
		const val nameOffset = 0x00CE
		const val levelOffset = 0x0160
		const val recordSize = 0x0127
		val charSet: Charset = Charset.forName("BIG5")
	}

	fun loadFromFile(file: RandomAccessFile) {
		if (file.length() >= (index + 1) * recordSize) {
			file.seek(index.toLong() * recordSize + nameOffset)
			val ba = mutableListOf<Byte>()
			for (i in 0 until 8) {
				val b = file.readByte()
				if (b == 0.toByte()) {
					break
				} else {
					ba.add(b)
				}
			}
			name = ba.toByteArray().toString(charSet)

			file.seek(index.toLong() * recordSize + levelOffset)
			level = file.readByte().toInt()
		}
	}

	fun saveLevelToFile(file: RandomAccessFile) {
		if (file.length() >= (index + 1) * recordSize) {
			file.seek(index.toLong() * recordSize + levelOffset)
			file.writeByte(level)
		}
	}

}
