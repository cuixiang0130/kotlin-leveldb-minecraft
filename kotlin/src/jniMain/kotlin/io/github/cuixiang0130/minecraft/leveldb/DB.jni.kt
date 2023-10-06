package io.github.cuixiang0130.minecraft.leveldb

public actual class DB private constructor(
    private val dbPointer: Long,
    private val optionsPointer: Long
) : AutoCloseable {

    private var isClosed = false

    public actual fun put(key: ByteArray, value: ByteArray, sync: Boolean) {
        checkState()
        nativePut(dbPointer, key, value, sync)
    }

    public actual fun delete(key: ByteArray, sync: Boolean) {
        checkState()
        nativeDelete(dbPointer, key, sync)
    }

    public actual fun write(writeBatch: WriteBatch, sync: Boolean) {
        checkState()
        nativeWrite(dbPointer, writeBatch.pointer(), sync)
    }

    public actual fun get(key: ByteArray, options: ReadOptions): ByteArray? {
        checkState()
        val snapshotPointer: Long = options.snapshot?.pointer() ?: 0
        return nativeGet(dbPointer, key, options.verifyChecksums, options.fillCache, snapshotPointer)
    }

    public actual fun iterator(options: ReadOptions): DBIterator {
        checkState()
        val snapshotPointer: Long = options.snapshot?.pointer() ?: 0
        return DBIterator(
            nativeNewIterator(
                dbPointer,
                options.verifyChecksums,
                options.fillCache,
                snapshotPointer
            )
        )
    }

    public actual fun getSnapshot(): Snapshot {
        checkState()
        return Snapshot(nativeGetSnapshot(dbPointer), this)
    }

    public actual fun releaseSnapshot(snapshot: Snapshot) {
        checkState()
        nativeReleaseSnapshot(dbPointer, snapshot.pointer())
    }


    public actual fun getProperty(property: String): String? {
        checkState()
        return nativeGetProperty(dbPointer, property)
    }

    public actual fun compactRange(begin: ByteArray?, end: ByteArray?) {
        checkState()
        nativeCompactRange(dbPointer, begin, end)
    }

    public actual override fun close() {
        if (isClosed) return
        nativeRelease(dbPointer)
        nativeReleaseOptions(optionsPointer)
        isClosed = true
    }

    private fun checkState() {
        check(!isClosed) {
            "DB is closed!"
        }
    }

    public actual companion object {

        init {
            loadLibrary()
        }

        public actual fun open(
            path: String,
            options: Options
        ): DB {
            val optionsPointer = newOptions(options)
            val dbPointer = nativeOpen(path, optionsPointer)
            return DB(dbPointer, optionsPointer)
        }

        public actual fun repair(path: String, options: Options) {
            val optionsPointer = newOptions(options)
            try {
                nativeRepair(path, optionsPointer)
            } finally {
                nativeReleaseOptions(optionsPointer)
            }
        }

        private fun newOptions(options: Options) = nativeNewOptions(
            options.createIfMissing,
            options.paranoidChecks,
            options.writeBufferSize,
            options.maxOpenFiles,
            options.cacheSize,
            options.blockSize,
            options.blockRestartInterval,
            options.maxFileSize,
            options.compressionType.ordinal,
            options.bloomFilterBitPerKey ?: -1
        )

        @JvmStatic
        private external fun nativeNewOptions(
            createIfMissing: Boolean,
            paranoidChecks: Boolean,
            writeBufferSize: Long,
            maxOpenFiles: Int,
            cacheSize: Long,
            blockSize: Long,
            blockRestartInterval: Int,
            maxFileSize: Long,
            compressionType: Int,
            bloomFilterBitPerKey: Int
        ): Long

        @JvmStatic
        private external fun nativeReleaseOptions(optionsPointer: Long)

        @JvmStatic
        private external fun nativeOpen(path: String, optionsPointer: Long): Long

        @JvmStatic
        private external fun nativeRelease(pointer: Long)

        @JvmStatic
        private external fun nativePut(pointer: Long, key: ByteArray, value: ByteArray, sync: Boolean)

        @JvmStatic
        private external fun nativeDelete(pointer: Long, key: ByteArray, sync: Boolean)

        @JvmStatic
        private external fun nativeWrite(pointer: Long, batchPtr: Long, sync: Boolean)

        @JvmStatic
        private external fun nativeGet(
            pointer: Long,
            key: ByteArray,
            verifyChecksums: Boolean,
            fillCache: Boolean,
            snapshotPointer: Long
        ): ByteArray?

        @JvmStatic
        private external fun nativeNewIterator(
            pointer: Long,
            verifyChecksums: Boolean,
            fillCache: Boolean,
            snapshotPointer: Long
        ): Long

        @JvmStatic
        private external fun nativeGetSnapshot(pointer: Long): Long

        @JvmStatic
        private external fun nativeReleaseSnapshot(pointer: Long, snapshotPointer: Long)

        @JvmStatic
        private external fun nativeGetProperty(pointer: Long, property: String): String

        @JvmStatic
        private external fun nativeCompactRange(pointer: Long, begin: ByteArray?, end: ByteArray?)

        @JvmStatic
        private external fun nativeRepair(path: String, optionsPointer: Long)

    }

}

internal expect fun loadLibrary()