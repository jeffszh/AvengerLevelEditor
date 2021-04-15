package cn.jeff.utils

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import java.io.IOException
import java.nio.charset.Charset
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.jvm.javaField

/**
 * # Stream-able object definer
 *
 * 用于对象与字节流的双向转换。
 * ------
 * 实际工作中经常需要进行诸如字节流数据收发之类，
 * 因此很有必要做一种通用的方法来处理字节流和Java/Kotlin对象的相互转换。
 */
class StreamAbleObjectDefiner(
		private val managedObject: Any,
		private vararg val fieldDefs: FieldDef) {

//	constructor(managedObject: Any) {
//	}

	class FieldDef(
			val field: KMutableProperty1<out Any, out Any?>,
			val fieldSize: Int? = null,
			val fractionLen: Int? = null,
			val charset: Charset? = null,
			val isBigEndian: Boolean = false,
			val isBcd: Boolean = false,
			val hardOffset: Int? = null,
			val readOnly: Boolean = false
	)

	fun saveToByteStream(bs: ByteOutputStream) {
//		var offset = 0
		fieldDefs.forEach { fieldDef ->
			if (!fieldDef.readOnly) { // 跳过只读的字段。
				// 处理指定偏移量
				if (fieldDef.hardOffset != null) {
					bs.write(ByteArray(fieldDef.hardOffset - bs.size()))
				}

				val field = fieldDef.field.javaField!!
//				println(field.type)

				when (field.type) {
					in setOf(Int::class.java, Long::class.java,
							java.lang.Integer::class.java, java.lang.Long::class.java) -> {
						println("${field.name} 是 ${field.type}")
						// 若没有指定fieldSize，从类型计算出来。
						val fieldSize = fieldDef.fieldSize
								?: when (field.type) {
									in setOf(Int::class.java, java.lang.Integer::class.java) -> 4
									in setOf(Long::class.java, java.lang.Long::class.java) -> 8
									else -> throw StreamingException("不可能运行到这里。")
								}
						var fieldValue = (fieldDef.field.getter.call(managedObject) as Number).toLong()
						val ba = ByteArray(fieldSize)
						val factor = (if (fieldDef.isBcd) 10 else 16).toLong()
						for (i in ba.indices) {
							val l = fieldValue % factor
							fieldValue /= factor
							val h = fieldValue % factor
							fieldValue /= factor
							ba[i] = ((h shl 4) + l).toByte()
						}
						if (fieldDef.isBigEndian) {
							ba.reverse()
						}
						bs.write(ba)
					}
					String::class.java -> {
						println("${field.name} 是字符串")
						val fieldSize = fieldDef.fieldSize ?: throw StreamingException(
								"${field.name} - 字符串必须指定长度！")
//					val fieldValue = field.get(managedObject) as String
						val fieldValue = fieldDef.field.getter.call(managedObject) as String
						val charset = fieldDef.charset ?: Charsets.UTF_8
						val ba = fieldValue.toByteArray(charset)
						if (ba.size < fieldSize) {
							bs.write(ba)
							bs.write(ByteArray(fieldSize - ba.size))
						} else {
							bs.write(ba, 0, fieldSize)
						}
					}
				}
			}
		}
	}

	class StreamingException(msg: String) : IOException(msg)

}
