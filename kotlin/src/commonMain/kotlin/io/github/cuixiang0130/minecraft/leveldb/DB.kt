package io.github.cuixiang0130.minecraft.leveldb

import kotlin.jvm.JvmOverloads

public expect class DB {

    @JvmOverloads
    public fun put(key: ByteArray, value: ByteArray, sync: Boolean = false)

    @JvmOverloads
    public fun delete(key: ByteArray, sync: Boolean = false)

    @JvmOverloads
    public fun write(writeBatch: WriteBatch, sync: Boolean = false)

    @JvmOverloads
    public fun get(key: ByteArray, options: ReadOptions = ReadOptions()): ByteArray?

    @JvmOverloads
    public fun iterator(options: ReadOptions = ReadOptions(fillCache = false)): DBIterator

    public fun getSnapshot(): Snapshot

    public fun releaseSnapshot(snapshot: Snapshot)

    public fun getProperty(property: String): String?

    public fun compactRange(begin: ByteArray? = null, end: ByteArray? = null)

    public fun close()

    public companion object {
        @JvmOverloads
        public fun open(path: String, options: Options = Options()): DB

        @JvmOverloads
        public fun repair(path: String, options: Options = Options())
    }

}

@Suppress("NOTHING_TO_INLINE")
public inline fun DB.set(key: ByteArray, value: ByteArray?, sync: Boolean = false) {
    if (value == null) delete(key, sync) else put(key, value, sync)
}

@Suppress("NOTHING_TO_INLINE")
public inline operator fun DB.set(key: ByteArray, value: ByteArray?) {
    if (value == null) delete(key) else put(key, value)
}

@Suppress("NOTHING_TO_INLINE")
public inline operator fun DB.get(key: ByteArray): ByteArray? = this.get(key)