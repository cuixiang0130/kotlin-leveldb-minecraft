# kotlin-leveldb-minecraft

## Using as dependency

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.cuixiang0130:leveldb-minecraft:0.1.0")
            }
        }
        val jvmMain by getting {
            dependencies {
		//for Windows x64
                implementation("io.github.cuixiang0130:leveldb-minecraft-jvm-windows-x64:0.1.0")
		//for Linux x64
                implementation("io.github.cuixiang0130:leveldb-minecraft-jvm-linux-x64:0.1.0")
            }
        }
    }
}
```

## Supported platforms

* Kotlin/JVM on Windows(x64)
* Kotlin/JVM on Linux(x64)
* Kotlin/JVM on Android(arm64, x64)
* Kotlin/Native on Windows(mingw_x64)
