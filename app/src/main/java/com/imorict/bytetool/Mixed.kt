package com.imorict.bytetool

import com.imorict.bytetools.BinField
import com.imorict.bytetools.Endian

data class MixedEndianPacket(
    @BinField(order = 1, endian = Endian.BIG)
    val bigInt: Int,

    @BinField(order = 2, endian = Endian.LITTLE)
    val littleInt: Int,

    @BinField(order = 3, size = 8, endian = Endian.LITTLE)
    val name: String
)