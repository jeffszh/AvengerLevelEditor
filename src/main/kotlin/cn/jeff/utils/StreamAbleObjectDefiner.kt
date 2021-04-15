package cn.jeff.utils

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import java.nio.charset.Charset
import kotlin.reflect.*
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaSetter

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
			val hardOffset: Int? = null,
			val readOnly: Boolean = false
	)

	fun saveToByteStream(bs: ByteOutputStream) {
//		var offset = 0
		fieldDefs.forEach { fieldDef ->
			val field = fieldDef.field.javaField!!
			println(field.type)
			when (field.type) {
				in setOf(Int::class.java, Long::class.java,
						java.lang.Integer::class.java, java.lang.Long::class.java) -> {
					println("${field.name} 是 ${field.type}")
				}
				String::class.java -> {
					println("${field.name} 是字符串")
				}
			}
		}
	}

}
