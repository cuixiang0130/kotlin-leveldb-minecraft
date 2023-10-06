package io.github.cuixiang0130.minecraft.leveldb

public expect class WriteBatch(){

    public fun put(key: ByteArray, value: ByteArray)

    public fun delete(key: ByteArray)

    public fun clear()

    public fun append(source: WriteBatch)

    public fun close()

}

@Suppress("NOTHING_TO_INLINE")
public inline operator fun WriteBatch.set(key: ByteArray, value: ByteArray?) {
    if (value == null) delete(key) else put(key, value)
}
