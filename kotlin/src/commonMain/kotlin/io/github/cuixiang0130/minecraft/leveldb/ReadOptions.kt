package io.github.cuixiang0130.minecraft.leveldb

public data class ReadOptions(
    public val verifyChecksums: Boolean = false,
    public val fillCache: Boolean = true,
    public val snapshot: Snapshot? = null
)