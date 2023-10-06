package io.github.cuixiang0130.minecraft.leveldb

public expect class DBIterator {

    public fun valid(): Boolean

    public fun seekToFirst()

    public fun seekToLast()

    public fun seek(key: ByteArray)

    public fun next()

    public fun prev()

    public fun key(): ByteArray

    public fun value(): ByteArray

    public fun close()

}