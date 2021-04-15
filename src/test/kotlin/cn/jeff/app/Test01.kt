package cn.jeff.app

import cn.jeff.utils.StreamAbleObjectDefiner
import cn.jeff.utils.StreamAbleObjectDefiner.FieldDef
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import java.nio.charset.Charset

data class Book(
		var bookName: String?,
		var pageCount: Long?,
		var year: Int
) {

	private val definer = StreamAbleObjectDefiner(
			this,
			FieldDef(Book::bookName, 20,
					charset = Charset.forName("BIG5")),
			FieldDef(Book::pageCount,
					isBcd = true),
			FieldDef(Book::year,
					isBcd = true,
					isBigEndian = true)
	)

	fun saveToByteStream(bs: ByteOutputStream) {
		definer.saveToByteStream(bs)
	}

}

fun main() {
	val book1 = Book("天書", 1256, 2021)
	println(book1)
	val bs = ByteOutputStream(0)
	book1.saveToByteStream(bs)
	@Suppress("DEPRECATION")
	println(bs.toByteArray().joinToString {
		String.format("%02X", it)
	})
}
