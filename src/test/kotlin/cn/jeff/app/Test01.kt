package cn.jeff.app

import cn.jeff.utils.StreamAbleObjectDefiner
import cn.jeff.utils.StreamAbleObjectDefiner.FieldDef
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream

data class Book(
		var bookName: String,
		var pageCount: Long
) {

	private val definer = StreamAbleObjectDefiner(
			this,
			FieldDef("bookName"),
			FieldDef("pageCount")
	)

	fun saveToByteStream(bs: ByteOutputStream) {
		definer.saveToByteStream(bs)
	}

}

fun main() {
	val book1 = Book("天书", 1256)
	println(book1)
	val bs = ByteOutputStream()
	book1.saveToByteStream(bs)
	println(bs.bytes.joinToString {
		String.format("%02X", it)
	})
}
