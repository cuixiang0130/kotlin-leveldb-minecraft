package io.github.cuixiang0130.minecraft.leveldb

import kotlinx.cinterop.*
import platform.posix.memcpy
import platform.posix.size_tVar

@ExperimentalForeignApi
internal inline fun CPointer<ByteVar>.toByteArray(length: size_tVar): ByteArray {
    val len = length.value
    val byteArray = ByteArray(len.toInt())
    byteArray.usePinned {
        memcpy(it.addressOf(0), this, len)
    }
    return byteArray
}

@ExperimentalForeignApi
internal fun handleException(err: CPointerVar<ByteVar>) {
    err.value?.let {
        throw DBException(it.toKString())
    }
}

@OptIn(ExperimentalForeignApi::class)
internal inline fun <R> checkErr(block: MemScope.(err: CPointerVar<ByteVar>) -> R): R {
    memScoped {
        val err = alloc<CPointerVar<ByteVar>>()
        val result = block(err)
        handleException(err)
        return result
    }
}

@OptIn(ExperimentalForeignApi::class)
internal inline fun Boolean.toUByte() = this.toByte().toUByte()