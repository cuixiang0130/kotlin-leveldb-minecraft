package io.github.cuixiang0130.minecraft.leveldb

import cnames.structs.leveldb_cache_t
import cnames.structs.leveldb_filterpolicy_t
import cnames.structs.leveldb_options_t
import cnames.structs.leveldb_readoptions_t
import cnames.structs.leveldb_t
import cnames.structs.leveldb_writeoptions_t
import kotlinx.cinterop.*
import platform.posix.size_t
import platform.posix.size_tVar
import kotlin.AutoCloseable
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.ExperimentalStdlibApi
import kotlin.OptIn
import kotlin.String
import kotlin.Triple

@OptIn(ExperimentalStdlibApi::class, ExperimentalForeignApi::class)
public actual class DB private constructor(
    private val db: CPointer<leveldb_t>,
    private val cache: CPointer<leveldb_cache_t>,
    private val filterPolicy: CPointer<leveldb_filterpolicy_t>?
) : AutoCloseable {

    private var isClosed = false

    public actual fun put(key: ByteArray, value: ByteArray, sync: Boolean) {
        checkState()
        checkErr { err ->
            val options = newWriteOptions(sync)
            val keyPinned = key.pin()
            val valuePinned = value.pin()
            leveldb_put(
                db,
                options,
                keyPinned.addressOf(0),
                key.size.convert<size_t>(),
                valuePinned.addressOf(0),
                value.size.convert<size_t>(),
                err.ptr
            )
            keyPinned.unpin()
            valuePinned.unpin()
            leveldb_writeoptions_destroy(options)
        }
    }

    public actual fun delete(key: ByteArray, sync: Boolean) {
        checkState()
        checkErr { err ->
            val options = newWriteOptions(sync)
            key.usePinned {
                leveldb_delete(db, options, it.addressOf(0), key.size.convert<size_t>(), err.ptr)
            }
            leveldb_writeoptions_destroy(options)
        }
    }


    public actual fun write(writeBatch: WriteBatch, sync: Boolean) {
        checkState()
        val batch = writeBatch.writeBatch()
        checkErr { err ->
            val options = newWriteOptions(sync)
            leveldb_write(db, options, batch, err.ptr)
            leveldb_writeoptions_destroy(options)
        }
    }

    public actual operator fun get(key: ByteArray, options: ReadOptions): ByteArray? {
        checkState()
        checkErr { err ->
            val readOptions = newReadOptions(options)
            val length = alloc<size_tVar>()
            val value = key.usePinned {
                leveldb_get(db, readOptions, it.addressOf(0), key.size.convert<size_t>(), length.ptr, err.ptr)
            }
            leveldb_readoptions_destroy(readOptions)
            return value?.toByteArray(length)
        }
    }

    public actual fun iterator(options: ReadOptions): DBIterator = memScoped {
        checkState()
        val readOptions = newReadOptions(options)
        val iterator = leveldb_create_iterator(db, readOptions)!!
        leveldb_readoptions_destroy(readOptions)
        return DBIterator(iterator)
    }

    public actual fun getSnapshot(): Snapshot {
        checkState()
        val snapshot = leveldb_create_snapshot(db)!!
        return Snapshot(snapshot, this)
    }

    public actual fun releaseSnapshot(snapshot: Snapshot) {
        checkState()
        val snapshot_t = snapshot.snapshot()
        leveldb_release_snapshot(db, snapshot_t)
    }

    public actual fun getProperty(property: String): String? {
        checkState()
        val value = leveldb_property_value(db, property)
        return value?.toKString()
    }

    public actual fun compactRange(begin: ByteArray?, end: ByteArray?) {
        checkState()
        val beginPinned = begin?.pin()
        val endPinned = end?.pin()
        leveldb_compact_range(
            db,
            beginPinned?.addressOf(0),
            (begin?.size ?: 0).convert<size_t>(),
            endPinned?.addressOf(0),
            (end?.size ?: 0).convert<size_t>()
        )
        beginPinned?.unpin()
        endPinned?.unpin()
    }

    public actual override fun close() {
        if (isClosed) return
        leveldb_close(db)
        leveldb_cache_destroy(cache)
        filterPolicy?.let { leveldb_filterpolicy_destroy(it) }
        isClosed = true
    }

    private fun checkState() {
        check(!isClosed) {
            "DB is closed!"
        }
    }


    public actual companion object {

        public actual fun open(
            path: String,
            options: Options
        ): DB {
            checkErr { err ->
                val (dbOptions, cache, filterPolicy) = newOptions(options)
                val dbPointer = leveldb_open(dbOptions, path, err.ptr)
                leveldb_options_destroy(dbOptions)
                return DB(dbPointer!!, cache, filterPolicy)
            }
        }

        public actual fun repair(path: String, options: Options) {
            checkErr { err ->
                val (dbOptions, cache, filterPolicy) = newOptions(options)
                leveldb_repair_db(dbOptions, path, err.ptr)
                leveldb_cache_destroy(cache)
                filterPolicy?.let { leveldb_filterpolicy_destroy(it) }
                leveldb_options_destroy(dbOptions)
            }
        }

        private inline fun newOptions(options: Options): Triple<CPointer<leveldb_options_t>, CPointer<leveldb_cache_t>, CPointer<leveldb_filterpolicy_t>?> {
            val dbOptions = leveldb_options_create()!!
            leveldb_options_set_create_if_missing(dbOptions, options.createIfMissing.toUByte())
            leveldb_options_set_error_if_exists(dbOptions, options.errorIfExists.toUByte())
            leveldb_options_set_paranoid_checks(dbOptions, options.paranoidChecks.toUByte())
            leveldb_options_set_write_buffer_size(dbOptions, options.writeBufferSize.convert<size_t>())
            leveldb_options_set_max_open_files(dbOptions, options.maxOpenFiles)
            val cache = leveldb_cache_create_lru(options.cacheSize.convert<size_t>())!!
            leveldb_options_set_cache(dbOptions, cache)
            leveldb_options_set_block_size(dbOptions, options.blockSize.convert<size_t>())
            leveldb_options_set_block_restart_interval(dbOptions, options.blockRestartInterval)
            leveldb_options_set_max_file_size(dbOptions, options.maxFileSize.convert<size_t>())
            leveldb_options_set_compression(dbOptions, options.compressionType.ordinal)
            val filterPolicy = options.bloomFilterBitPerKey?.let {
                leveldb_filterpolicy_create_bloom(it).apply { leveldb_options_set_filter_policy(dbOptions, this) }
            }
            return Triple(dbOptions, cache, filterPolicy)
        }

        private inline fun newReadOptions(readOptions: ReadOptions): CPointer<leveldb_readoptions_t> {
            val snapshot = readOptions.snapshot?.snapshot()
            val options = leveldb_readoptions_create()!!
            leveldb_readoptions_set_verify_checksums(options, readOptions.verifyChecksums.toUByte())
            leveldb_readoptions_set_fill_cache(options, readOptions.fillCache.toUByte())
            leveldb_readoptions_set_snapshot(options, snapshot)
            return options
        }

        private inline fun newWriteOptions(sync: Boolean): CPointer<leveldb_writeoptions_t> {
            val options = leveldb_writeoptions_create()!!
            leveldb_writeoptions_set_sync(options, sync.toUByte())
            return options
        }

    }


}