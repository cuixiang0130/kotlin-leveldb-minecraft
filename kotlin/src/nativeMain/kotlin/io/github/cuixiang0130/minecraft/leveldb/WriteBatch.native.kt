package io.github.cuixiang0130.minecraft.leveldb

import kotlinx.cinterop.*
import platform.posix.size_t

@OptIn(ExperimentalStdlibApi::class, ExperimentalForeignApi::class)
public actual class WriteBatch public actual constructor() : AutoCloseable {

    private val writeBatch = leveldb_writebatch_create()!!

    private var isClosed = false

    public actual fun put(key: ByteArray, value: ByteArray) {
        checkState()
        val keyPinned = key.pin()
        val valuePinned = value.pin()
        leveldb_writebatch_put(
            writeBatch,
            keyPinned.addressOf(0),
            key.size.convert<size_t>(),
            valuePinned.addressOf(0),
            value.size.convert<size_t>()
        )
        keyPinned.unpin()
        valuePinned.unpin()
    }

    public actual fun delete(key: ByteArray) {
        checkState()
        key.usePinned {
            leveldb_writebatch_delete(
                writeBatch,
                it.addressOf(0),
                key.size.convert<size_t>()
            )
        }
    }

    public actual fun clear() {
        checkState()
        leveldb_writebatch_clear(writeBatch)
    }

    public actual fun append(source: WriteBatch) {
        checkState()
        leveldb_writebatch_append(writeBatch, source.writeBatch)
    }

    public actual override fun close() {
        if (isClosed) return
        leveldb_writebatch_destroy(writeBatch)
        isClosed = true
    }

    private fun checkState() {
        check(!isClosed){
            "WriteBatch is closed!"
        }
    }

    internal fun writeBatch(): CPointer<leveldb_writebatch_t> {
        checkState()
        return writeBatch
    }
}
