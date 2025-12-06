package com.imorict.bytetools

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

object BinarySerializer {

    fun <T : Any> serialize(obj: T): ByteArray {
        val kClass = obj::class
        val props = kClass.declaredMemberProperties
            .mapNotNull { p ->
                val ann = p.annotations.filterIsInstance<BinField>().firstOrNull()
                if (ann != null) p to ann else null
            }
            .sortedBy { it.second.order }

        val out = ByteArrayOutputStream()

        for ((prop, ann) in props) {
            prop.isAccessible = true
            val value = prop.getter.call(obj)
            val endian = ann.endian

            fun newBuf(size: Int): ByteBuffer =
                ByteBuffer
                    .allocate(size)
                    .order(
                        when (endian) {
                            Endian.BIG -> ByteOrder.BIG_ENDIAN
                            Endian.LITTLE -> ByteOrder.LITTLE_ENDIAN
                        }
                    ) // 필드별 엔디언.[web:31]

            val bytes: ByteArray = when (value) {
                is Byte -> byteArrayOf(value)

                is Short -> newBuf(Short.SIZE_BYTES).apply { putShort(value) }.array()
                is Int -> newBuf(Int.SIZE_BYTES).apply { putInt(value) }.array()
                is Long -> newBuf(Long.SIZE_BYTES).apply { putLong(value) }.array()
                is Float -> newBuf(Float.SIZE_BYTES).apply { putFloat(value) }.array()
                is Double -> newBuf(Double.SIZE_BYTES).apply { putDouble(value) }.array()

                is Boolean -> byteArrayOf(if (value) 1 else 0)

                is String -> {
                    val fixed = ann.size
                    require(fixed > 0) { "String field needs fixed size in @BinField" }
                    val raw = value.toByteArray(Charsets.UTF_8)
                    require(raw.size <= fixed) { "String too long: ${raw.size} > $fixed" }
                    ByteArray(fixed).also { dst ->
                        System.arraycopy(raw, 0, dst, 0, raw.size)
                        // 나머지는 0 패딩.[web:51]
                    }
                }

                null -> byteArrayOf(0) // 필요 시 별도 규칙 정의.

                else -> error("Unsupported type: ${value?.javaClass}")
            }

            out.write(bytes)
        }

        return out.toByteArray()
    }

    fun <T : Any> deserialize(bytes: ByteArray, clazz: KClass<T>): T {
        val props = clazz.declaredMemberProperties
            .mapNotNull { p ->
                val ann = p.annotations.filterIsInstance<BinField>().firstOrNull()
                if (ann != null) p to ann else null
            }
            .sortedBy { it.second.order }

        val ctor = clazz.primaryConstructor
            ?: error("Class ${clazz.simpleName} must have primary constructor") // data class 전제.[web:84][web:98]

        val ctorParams = ctor.parameters.associateBy { it.name }

        var offset = 0
        val args = mutableMapOf<kotlin.reflect.KParameter, Any?>()

        fun fieldBuffer(len: Int, endian: Endian): ByteBuffer {
            val slice = bytes.copyOfRange(offset, offset + len)
            offset += len
            return ByteBuffer.wrap(slice).order(
                when (endian) {
                    Endian.BIG -> ByteOrder.BIG_ENDIAN
                    Endian.LITTLE -> ByteOrder.LITTLE_ENDIAN
                }
            ) // 필드별 엔디언 적용.[web:31]
        }

        for ((prop, ann) in props) {
            val name = prop.name
            val param = ctorParams[name]
                ?: error("No ctor param for property $name")

            val endian = ann.endian
            val value: Any = when (param.type.classifier) {
                Byte::class -> {
                    val b = bytes[offset]
                    offset += 1
                    b
                }
                Short::class -> fieldBuffer(Short.SIZE_BYTES, endian).short
                Int::class -> fieldBuffer(Int.SIZE_BYTES, endian).int
                Long::class -> fieldBuffer(Long.SIZE_BYTES, endian).long
                Float::class -> fieldBuffer(Float.SIZE_BYTES, endian).float
                Double::class -> fieldBuffer(Double.SIZE_BYTES, endian).double
                Boolean::class -> {
                    val b = bytes[offset]
                    offset += 1
                    b.toInt() != 0
                }
                String::class -> {
                    val fixed = ann.size
                    require(fixed > 0) { "String field $name needs fixed size in @BinField" }
                    val slice = bytes.copyOfRange(offset, offset + fixed)
                    offset += fixed
                    // 0 패딩 제거 후 UTF-8 문자열로.[web:51]
                    val trimmed = slice.takeWhile { it.toInt() != 0 }.toByteArray()
                    trimmed.toString(Charsets.UTF_8)
                }
                else -> error("Unsupported type for property $name: ${param.type}")
            }

            args[param] = value
        }

        return ctor.callBy(args) // primary constructor에 매핑해서 인스턴스 생성
    }
}
