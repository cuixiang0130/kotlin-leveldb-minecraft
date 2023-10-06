package io.github.cuixiang0130.minecraft.leveldb

public actual class DBIterator internal constructor(private val pointer: Long) : AutoCloseable {

    private var isClosed = false

    public actual fun valid(): Boolean {
        checkState()
        return nativeValid(pointer)
    }

    public actual fun seekToFirst() {
        checkState()
        nativeSeekToFirst(pointer)
    }

    public actual fun seekToLast() {
        checkState()
        nativeSeekToLast(pointer)
    }

    public actual fun seek(key: ByteArray) {
        checkState()
        nativeSeek(pointer, key)
    }

    public actual operator fun next() {
        checkState()
        nativeNext(pointer)
    }

    public actual fun prev() {
        checkState()
        nativePrev(pointer)
    }

    public actual fun key(): ByteArray {
        if (!valid()) throw NoSuchElementException()
        return nativeKey(pointer)
    }

    public actual fun value(): ByteArray {
        if (!valid()) throw NoSuchElementException()
        return nativeValue(pointer)
    }

    public actual override fun close() {
        if (isClosed) return
        nativeRelease(pointer)
        isClosed = true
    }

    private fun checkState() {
        check(!isClosed) {
            "DBIterator is closed!"
        }
    }

    private external fun nativeValid(iteratorPtr: Long): Boolean
    private external fun nativeSeekToFirst(iteratorPtr: Long)
    private external fun nativeSeekToLast(iteratorPtr: Long)
    private external fun nativeSeek(iteratorPtr: Long, key: ByteArray)
    private external fun nativeNext(iteratorPtr: Long)
    private external fun nativePrev(iteratorPtr: Long)
    private external fun nativeKey(iteratorPtr: Long): ByteArray
    private external fun nativeValue(iteratorPtr: Long): ByteArray
    private external fun nativeRelease(iteratorPtr: Long)
}