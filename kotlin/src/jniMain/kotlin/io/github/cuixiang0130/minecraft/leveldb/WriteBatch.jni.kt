package io.github.cuixiang0130.minecraft.leveldb

public actual class WriteBatch public actual constructor() : AutoCloseable {

    private val pointer: Long = nativeNew()

    private var isClosed = false

    public actual fun put(key: ByteArray, value: ByteArray) {
        checkState()
        nativePut(pointer, key, value)
    }

    public actual fun delete(key: ByteArray) {
        checkState()
        nativeDelete(pointer, key)
    }

    public actual fun clear() {
        checkState()
        nativeClear(pointer)
    }

    public actual fun append(source: WriteBatch) {
        checkState()
        nativeAppend(pointer, source.pointer)
    }

    public actual override fun close() {
        if (isClosed) return
        nativeRelease(pointer)
        isClosed = true
    }

    private fun checkState() {
        check(!isClosed){
            "WriteBatch is closed!"
        }
    }

    internal fun pointer(): Long {
        checkState()
        return pointer
    }

    private companion object {
        init {
            DB
        }
        @JvmStatic
        private external fun nativeNew(): Long
        @JvmStatic
        private external fun nativeRelease(pointer: Long)
        @JvmStatic
        private external fun nativePut(pointer: Long, key: ByteArray?, value: ByteArray?)
        @JvmStatic
        private external fun nativeDelete(pointer: Long, key: ByteArray?)
        @JvmStatic
        private external fun nativeClear(pointer: Long)
        @JvmStatic
        private external fun nativeAppend(pointer: Long, sourceBatchPointer: Long)
    }
}
