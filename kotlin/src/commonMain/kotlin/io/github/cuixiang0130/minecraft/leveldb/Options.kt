package io.github.cuixiang0130.minecraft.leveldb

public data class Options(
    val createIfMissing: Boolean = true,
    val errorIfExists: Boolean = false,
    val paranoidChecks: Boolean = false,
    val writeBufferSize: Long = 4 * 1024 * 1024,
    val maxOpenFiles: Int = 1000,
    val cacheSize: Long = 8 * 1024 * 1024,
    val blockSize: Long = 4 * 1024,
    val blockRestartInterval: Int = 16,
    val maxFileSize: Long = 2 * 1024 * 1024,
    val compressionType: CompressionType = CompressionType.NONE,
    val bloomFilterBitPerKey:Int? = 10
)
