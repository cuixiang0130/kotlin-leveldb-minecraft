package io.github.cuixiang0130.minecraft.leveldb

import kotlinx.io.files.SystemPathSeparator
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.random.Random
import kotlin.test.*


class DBTest {

    private lateinit var db: DB
    private val key1: ByteArray = Random.nextBytes(10)
    private val key2: ByteArray = Random.nextBytes(14)
    private val value1: ByteArray = Random.nextBytes(1024)
    private val value2: ByteArray = Random.nextBytes(4096)

    @BeforeTest
    fun openDB() {
        val tmpDir = SystemTemporaryDirectory.toString()
        val path = tmpDir + SystemPathSeparator + "test_db"
        val options = Options(createIfMissing = true, compressionType = CompressionType.ZLIB)
        db = DB.open(path, options)
    }

    @AfterTest
    fun closeDB() {
        db.close()
    }

    @Test
    fun testDB() {
        db.put(key1, value1)
        assertContentEquals(value1, db[key1])
        db.put(key2, value2)
        assertContentEquals(value2, db[key2])
        db.put(key1, value2)
        assertContentEquals(value2, db[key1])
        db.delete(key1)
        assertNull(db[key1])
        db.compactRange(null, null)
        db[key1] = value1
        assertContentEquals(value1, db[key1])
        db[key1] = null
        assertNull(db[key1])
        println(db.getProperty("leveldb.stats"))
    }

    @Test
    fun testWriteBatch() {
        val batch = WriteBatch()
        batch.put(key1, value1)
        batch.put(key1, value2)
        batch.put(key2, value1)
        batch.delete(key2)
        batch[key1] = value2
        db.write(batch)
        batch.close()
        assertContentEquals(value2,db[key1])
    }

    @Test
    fun testIterator() {
        val prefix = "test_iteration:"
        val num = 10
        for (i in 0 until num) {
            db.put((prefix + i).encodeToByteArray(), byteArrayOf(i.toByte()))
        }
        val iterator = db.iterator()
        try {
            var forward = 0
            iterator.seekToFirst()
            while (iterator.valid()) {
                forward++
                iterator.next()
            }
            var reverse = 0
            iterator.seekToLast()
            while (iterator.valid()) {
                reverse++
                iterator.prev()
            }
            assertEquals(forward, reverse)
            var cnt = 0
            iterator.seek(prefix.encodeToByteArray())
            while (iterator.valid() && iterator.key().decodeToString().startsWith(prefix)) {
                cnt++
                iterator.next()
            }
            assertEquals(cnt, num)
        } finally {
            iterator.close()
        }
    }

}
