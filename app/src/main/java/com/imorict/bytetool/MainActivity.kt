package com.imorict.bytetool

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.imorict.bytetools.BinarySerializer

class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalStdlibApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        ///


        val p = MixedEndianPacket(
            bigInt = 0x11223344.toInt(),
            littleInt = 0x55667788.toInt(),
            name = "hi"
        )

        val bytes = BinarySerializer.serialize(p)
        val restored = BinarySerializer.deserialize(bytes, MixedEndianPacket::class)

        println("------------------------------\n\n\nclass: $p\n\nbytes: [${bytes.toHexString(
            HexFormat {
                upperCase = true
                bytes {
                    bytesPerGroup = 1
                    groupSeparator = ":"
                }
            })}]\nrestored: $restored\n\n\n------------------------------")


    }
}