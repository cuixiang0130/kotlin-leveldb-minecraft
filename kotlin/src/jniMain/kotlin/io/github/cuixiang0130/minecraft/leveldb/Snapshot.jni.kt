package io.github.cuixiang0130.minecraft.leveldb

public actual class Snapshot internal constructor(private val pointer: Long, public val db: DB) {

    private var isClosed = false

    public actual fun close() {
        if (isClosed) return
        db.releaseSnapshot(this)
        isClosed = true
    }

    internal fun pointer(): Long {
        check(!isClosed) {
            "Snapshot is closed!"
        }
        return pointer
    }

}
