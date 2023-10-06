package io.github.cuixiang0130.minecraft.leveldb

import cnames.structs.leveldb_iterator_t
import kotlinx.cinterop.*
import platform.posix.size_t
import platform.posix.size_tVar

@OptIn(ExperimentalStdlibApi::class, ExperimentalForeignApi::class)
public actual class DBIterator internal constructor(private val iterator: CPointer<leveldb_iterator_t>) :
    AutoCloseable {

    private var isClosed = false

    public actual fun valid(): Boolean {
        checkState()
        return leveldb_iter_valid(iterator).toInt() != 0
    }

    public actual fun seekToFirst() {
        checkState()
        leveldb_iter_seek_to_first(iterator)
    }
    
    public actual fun seekToLast() {
        checkState()
        leveldb_iter_seek_to_last(iterator)
    }

    public actual fun seek(key: ByteArray) {
        checkState()
        key.usePinned {
            leveldb_iter_seek(iterator, it.addressOf(0), key.size.convert<size_t>())
        }
    }

    public actual fun next() {
        checkState()
        leveldb_iter_next(iterator)
    }

    public actual fun prev() {
        checkState()
        leveldb_iter_prev(iterator)
    }

    public actual fun key(): ByteArray {
        if (!valid()) throw NoSuchElementException()
        memScoped {
            val length = alloc<size_tVar>()
            val key = leveldb_iter_key(iterator, length.ptr)!!
            return key.toByteArray(length)
        }
    }

    public actual fun value(): ByteArray {
        if (!valid()) throw NoSuchElementException()
        memScoped {
            val length = alloc<size_tVar>()
            val value = leveldb_iter_value(iterator, length.ptr)!!
            return value.toByteArray(length)
        }
    }

    public actual override fun close() {
        if (isClosed) return
        leveldb_iter_destroy(iterator)
        isClosed = true
    }

    private fun checkState() {
        check(!isClosed) {
            "DBIterator is closed!"
        }
    }

}