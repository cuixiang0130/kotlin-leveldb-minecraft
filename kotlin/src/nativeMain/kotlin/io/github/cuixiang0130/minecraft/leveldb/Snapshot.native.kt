package io.github.cuixiang0130.minecraft.leveldb

import cnames.structs.leveldb_snapshot_t
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
public actual class Snapshot internal constructor(private val snapshot: CPointer<leveldb_snapshot_t>, private val db: DB){

    private var isClosed = false

    public actual fun close(){
        if (isClosed) return
        db.releaseSnapshot(this)
        isClosed = true
    }

    internal fun snapshot(): CPointer<leveldb_snapshot_t> {
        check(!isClosed){
            "WriteBatch is closed!"
        }
        return snapshot
    }
}

