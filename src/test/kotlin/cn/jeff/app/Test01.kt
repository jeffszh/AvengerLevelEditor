package cn.jeff.app

import cn.jeff.utils.StreamAbleObjectDefiner
import cn.jeff.utils.StreamAbleObjectDefiner.Companion.bcdBigEndianField
import cn.jeff.utils.StreamAbleObjectDefiner.Companion.bcdLittleEndianField
import cn.jeff.utils.StreamAbleObjectDefiner.FieldDef
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import java.nio.charset.Charset

data class Book(
		var bookName: String?,
		var pageCount: Long?,
		var year: Int,
		var fidelity: Float,
		var rate: Double
) {

	private val definer = StreamAbleObjectDefiner(
			this,
			FieldDef(Book::bookName, 20,
					charset = Charset.forName("BIG5")),
			FieldDef(Book::pageCount,
					isBcd = true),
			FieldDef(Book::year,
					isBcd = true,
					isBigEndian = true),
			bcdLittleEndianField(Book::fidelity, 2, 2),
			bcdBigEndianField(Book::rate, 5, 3)
	)

	fun saveToByteStream(bs: ByteOutputStream) {
		definer.saveToByteStream(bs)
	}

}

@Suppress("DEPRECATION")
fun ByteOutputStream.asByteArray(): ByteArray = toByteArray()

fun main() {
	val book1 = Book("天書", 1256, 2021, 98.1f, 345.6789)
	println(book1)
	val bs = ByteOutputStream(0)
	book1.saveToByteStream(bs)
	println(bs.asByteArray().joinToString {
		String.format("%02X", it)
	})
}
