package io.github.cuixiang0130.minecraft.leveldb

import java.io.File

@Suppress("UnsafeDynamicallyLoadedCode")
internal actual fun loadLibrary() {

    val libName = getLibName()
    try {
        System.loadLibrary(libName)
    } catch (_: UnsatisfiedLinkError) {
        val libFullName = System.mapLibraryName(libName)
        val path = unpackLib(libFullName)
        System.load(path)
    }

}

private fun getLibName(): String {
    val osName = System.getProperty("os.name")
    val hostOs = when {
        osName.startsWith("Win") -> "windows"
        osName == "Linux" -> "linux"
        osName == "Mac OS X" -> "macos"
        "The Android Project" == System.getProperty("java.specification.vendor") -> throw Error("Use AndroidTarget dependency instead")
        else -> throw Error("Unsupported OS $osName")
    }

    val hostArch = when (val osArch = System.getProperty("os.arch")) {
        "x86_64", "amd64" -> "x64"
        "aarch64" -> "arm64"
        else -> throw Error("Unsupported Arch $osArch")
    }
    return "leveldb-minecraft-jvm-$hostOs-$hostArch"
}

private fun unpackLib(libFullName: String): String {
    val libFile = File("./$libFullName")
    val libStream = DB::class.java.getResourceAsStream("/$libFullName") ?: throw Error("Can't find library")
    libStream.use { input ->
        libFile.outputStream().use { output ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } >= 0) {
                output.write(buffer, 0, read)
            }
        }
    }
    return libFile.absolutePath
}

