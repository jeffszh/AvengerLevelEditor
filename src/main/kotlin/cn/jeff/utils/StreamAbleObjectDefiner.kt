package cn.jeff.utils

import java.io.*
import java.nio.charset.Charset
import kotlin.math.pow
import kotlin.math.roundToLong
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

	companion object {

		fun bcdLittleEndianField(
				field: KMutableProperty1<out Any, out Number?>,
				fieldSize: Int,
				fractionLen: Int? = null,
				hardOffset: Int? = null,
				readOnly: Boolean = false
		) = FieldDef(
				field,
				fieldSize,
				fractionLen,
				null,
				isBigEndian = false,
				isBcd = true,
				hardOffset = hardOffset,
				readOnly = readOnly
		)

		fun bcdBigEndianField(
				field: KMutableProperty1<out Any, out Number?>,
				fieldSize: Int,
				fractionLen: Int? = null,
				hardOffset: Int? = null,
				readOnly: Boolean = false
		) = FieldDef(
				field,
				fieldSize,
				fractionLen,
				null,
				isBigEndian = true,
				isBcd = true,
				hardOffset = hardOffset,
				readOnly = readOnly
		)

	}

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

	fun saveToByteStream(bs: ByteArrayOutputStream) {
		saveTo(object : AbstractWriter() {
			// 实际上 ByteArrayOutputStream 不能跳过若干字节，
			// 所以 saveToByteStream 不能处理只读字段。
			override val supportReadonly = false

			override fun seek(n: Int) {
				// 用Skip来代替seek。
				skip(n - bs.size())
			}

			override fun skip(n: Int) {
				// ByteArrayOutputStream只能用空写来代替跳过。
				write(ByteArray(n))
			}

			override fun write(bytes: ByteArray) {
				bs.write(bytes)
			}

			override fun write(bytes: ByteArray, start: Int, len: Int) {
				bs.write(bytes, start, len)
			}
		})
	}

	private fun saveTo(writer: AbstractWriter) {
		fieldDefs.forEach { fieldDef ->
			if (fieldDef.readOnly && !writer.supportReadonly) {
				throw StreamingException("不能处理只读字段。")
			}

			// 处理指定偏移量
			if (fieldDef.hardOffset != null) {
				writer.seek(fieldDef.hardOffset)
			}

			val field = fieldDef.field.javaField!!

			when (field.type) {
				// 整数
				Int::class.java, Long::class.java,
				java.lang.Integer::class.java, java.lang.Long::class.java -> {
					println("${field.name} 是 ${field.type}")
					// 若没有指定fieldSize，从类型计算出来。
					val fieldSize = fieldDef.fieldSize
							?: when (field.type) {
								Int::class.java, java.lang.Integer::class.java -> 4
								Long::class.java, java.lang.Long::class.java -> 8
								else -> throw StreamingException("不可能运行到这里。")
							}
					val ba = ByteArray(fieldSize)
					var fieldValue = (fieldDef.field.getter.call(managedObject) as Number).toLong()
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
					writer.write(ba)
				}

				// 定点小数
				Float::class.java, Double::class.java,
				java.lang.Float::class.java, java.lang.Double::class.java -> {
					if (fieldDef.fieldSize == null) {
						throw StreamingException("${field.name} - 非整数数值必须指定长度！")
					}
					if (fieldDef.fractionLen == null) {
						throw StreamingException("${field.name} - 必须指定小数位数！")
					}
					val doubleValue = (fieldDef.field.getter.call(managedObject) as Number).toDouble()
					val p = 10.0.pow(fieldDef.fractionLen)
					var fieldValue = (doubleValue * p).roundToLong()
					val ba = ByteArray(fieldDef.fieldSize)
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
					writer.write(ba)
				}

				// 字符串
				String::class.java -> {
					println("${field.name} 是字符串")
					val fieldSize = fieldDef.fieldSize ?: throw StreamingException(
							"${field.name} - 字符串必须指定长度！")
//					val fieldValue = field.get(managedObject) as String
					val fieldValue = fieldDef.field.getter.call(managedObject) as String
					val charset = fieldDef.charset ?: Charsets.UTF_8
					val ba = fieldValue.toByteArray(charset)
					if (ba.size < fieldSize) {
						writer.write(ba)
						writer.skip(fieldSize - ba.size)
					} else {
						writer.write(ba, 0, fieldSize)
					}
				}
			}
		}
	}

	fun loadFromByteStream(bs: ByteArrayInputStream) {
		loadFrom(object : AbstractReader() {

			override fun seek(n: Int) {
				// 用reset和skip实现seek，前提是没人调用过mark。
				bs.reset()
				bs.skip(n.toLong())
			}

			override fun read(bytes: ByteArray): Int =
					bs.read(bytes)

		})
	}

	private fun loadFrom(reader: AbstractReader) {
		fieldDefs.forEach { fieldDef ->
			// 处理指定偏移量
			if (fieldDef.hardOffset != null) {
				reader.seek(fieldDef.hardOffset)
			}

			val field = fieldDef.field.javaField!!

			when (field.type) {
				// 整数
				Int::class.java, Long::class.java,
				java.lang.Integer::class.java, java.lang.Long::class.java -> {
					println("${field.name} 是 ${field.type}")
					// 若没有指定fieldSize，从类型计算出来。
					val fieldSize = fieldDef.fieldSize
							?: when (field.type) {
								Int::class.java, java.lang.Integer::class.java -> 4
								Long::class.java, java.lang.Long::class.java -> 8
								else -> throw StreamingException("不可能运行到这里。")
							}
					val ba = ByteArray(fieldSize)
					reader.read(ba)
					if (!fieldDef.isBigEndian) {
						// 总是转换为大端，因为大端比小端容易处理些。
						ba.reverse()
					}
					val factor = (if (fieldDef.isBcd) 10 else 16).toLong()
					var fieldValue = 0L
					ba.forEach { b ->
						val l = b.toInt() and 0x0F
						val h = (b.toInt() ushr 4) and 0x0F
						fieldValue *= factor
						fieldValue += h
						fieldValue *= factor
						fieldValue += l
					}
					when (field.type) {
						Int::class.java, java.lang.Integer::class.java ->
							fieldDef.field.setter.call(managedObject, fieldValue.toInt())
						Long::class.java, java.lang.Long::class.java ->
							fieldDef.field.setter.call(managedObject, fieldValue)
						else -> throw StreamingException("不可能运行到这里。")
					}
				}

				// 定点小数
				Float::class.java, Double::class.java,
				java.lang.Float::class.java, java.lang.Double::class.java -> {
					if (fieldDef.fieldSize == null) {
						throw StreamingException("${field.name} - 非整数数值必须指定长度！")
					}
					if (fieldDef.fractionLen == null) {
						throw StreamingException("${field.name} - 必须指定小数位数！")
					}
					val ba = ByteArray(fieldDef.fieldSize)
					reader.read(ba)
					val p = 10.0.pow(fieldDef.fractionLen)
					if (!fieldDef.isBigEndian) {
						// 总是转换为大端，因为大端比小端容易处理些。
						ba.reverse()
					}
					val factor = (if (fieldDef.isBcd) 10 else 16).toLong()
					var fieldValue = 0L
					ba.forEach { b ->
						val l = b.toInt() and 0x0F
						val h = (b.toInt() ushr 4) and 0x0F
						fieldValue *= factor
						fieldValue += h
						fieldValue *= factor
						fieldValue += l
					}
					when (field.type) {
						Float::class.java, java.lang.Float::class.java ->
							fieldDef.field.setter.call(managedObject, (fieldValue / p).toFloat())
						Double::class.java, java.lang.Double::class.java ->
							fieldDef.field.setter.call(managedObject, fieldValue / p)
						else -> throw StreamingException("不可能运行到这里。")
					}
				}

				// 字符串
				String::class.java -> {
					println("${field.name} 是字符串")
					val fieldSize = fieldDef.fieldSize ?: throw StreamingException(
							"${field.name} - 字符串必须指定长度！")
					val charset = fieldDef.charset ?: Charsets.UTF_8
					val ba = ByteArray(fieldSize)
					reader.read(ba)
					val ba2 = ba.takeWhile {
						it != 0.toByte()
					}.toByteArray()
					println("${ba.size}, ${ba2.size}")
					val str = ba2.toString(charset)
					fieldDef.field.setter.call(managedObject, str)
				}
			}
		}
	}

	class StreamingException(msg: String) : IOException(msg)

	private abstract class AbstractReader {
		abstract fun seek(n: Int)
		abstract fun read(bytes: ByteArray): Int
	}

	private abstract class AbstractWriter {
		abstract val supportReadonly: Boolean
		abstract fun seek(n: Int)
		abstract fun skip(n: Int)
		abstract fun write(bytes: ByteArray)
		abstract fun write(bytes: ByteArray, start: Int, len: Int)
	}

}
