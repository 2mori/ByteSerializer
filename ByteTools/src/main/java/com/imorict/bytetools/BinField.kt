package com.imorict.bytetools

enum class Endian { BIG, LITTLE }

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class BinField(
    val order: Int,
    val size: Int = -1,
    val endian: Endian = Endian.BIG
)