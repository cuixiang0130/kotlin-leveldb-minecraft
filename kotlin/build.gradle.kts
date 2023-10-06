import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    `maven-publish`
    signing
}

val ci = System.getenv("CI") != null
val releaseMode = System.getenv("RELEASE_MODE") != null

val allJvmTargets = listOf("windows-x64", "linux-x64")
val hostTarget = run {
    val osName = System.getProperty("os.name")
    val hostOs = when {
        osName.startsWith("Win") -> "windows"
        osName == "Linux" -> "linux"
        //osName == "Mac OS X" -> "macos"
        else -> throw GradleException("Unsupported OS $osName")
    }

    val hostArch = when (val osArch = System.getProperty("os.arch")) {
        "x86_64", "amd64" -> "x64"
        //"aarch64" -> "arm64"
        else -> throw GradleException("Unsupported Arch $osArch")
    }
    "$hostOs-$hostArch"
}
val jvmTargets = if (releaseMode) allJvmTargets else listOf(hostTarget)

fun getJvmLibName(target:String) = "leveldb-minecraft-jvm-$target"


kotlin {

    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    androidTarget {
        publishLibraryVariants("release")
    }

    if (ci || releaseMode){
        mingwX64()
    }else{
        when(hostTarget){
            "windows-x64" -> mingwX64()
        }
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.io.core)
            }
        }
        val jniMain by creating {
            dependsOn(commonMain)
        }
        val jniTest by creating {
            dependsOn(commonTest)
        }
        val jvmMain by getting {
            dependsOn(jniMain)
            dependencies {
                implementation(files("build/libs/${getJvmLibName(hostTarget)}-$version.jar"))
            }
        }
        val jvmTest by getting {
            dependsOn(jniTest)
        }
        val androidMain by getting {
            dependsOn(jniMain)
        }
        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val nativeTest by creating {
            dependsOn(commonTest)
        }

        targets.withType<KotlinNativeTarget> {
            val main by compilations.getting {
                defaultSourceSet.dependsOn(nativeMain)
                cinterops {
                    create("leveldb")
                }
            }

            val test by compilations.getting
            test.defaultSourceSet.dependsOn(nativeTest)
        }

    }
}

android {
    namespace = "io.github.cuixiang0130.minecraft.leveldb"
    compileSdk = libs.versions.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

afterEvaluate {
    val compileKotlinJvm = tasks.getByName("compileKotlinJvm")
    jvmTargets.forEach { target ->
        val libName = getJvmLibName(target)
        val jvmLibJar = tasks.register<Jar>("jvmLibJar-$target") {
            from(file("runtime/jvm/$target"))
            archiveBaseName = libName
        }
        compileKotlinJvm.dependsOn(jvmLibJar)
        publishing.publications {
            create<MavenPublication>(libName) {
                artifactId = libName
                artifact(jvmLibJar)
            }
        }
    }

    tasks.getByName("testDebugUnitTest").enabled = false
    tasks.getByName("testReleaseUnitTest").enabled = false
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing.publications {
    withType<MavenPublication> {
        if(artifactId.endsWith("jvm"))
            artifact(emptyJavadocJar)
        pom {
            name.set(project.name)
            description.set(project.description)
            url.set("https://github.com/cuixiang0130/kotlin-leveldb-minecraft")
            developers {
                developer {
                    id.set("cuixiang0130")
                    name.set("cuixiang0130")
                    email.set("cuixiang0130@gmail.com")
                }
            }
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/cuixiang0130/kotlin-leveldb-minecraft.git")
                developerConnection.set("scm:git:https://github.com/cuixiang0130/kotlin-leveldb-minecraft.git")
                url.set("https://github.com/cuixiang0130/kotlin-leveldb-minecraft")
            }
        }
    }
}

if (releaseMode) {
    publishing.repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")!!
                password = System.getenv("MAVEN_PASSWORD")!!
            }
        }
    }
    signing {
        useInMemoryPgpKeys(System.getenv("GPG_KEY")!!,System.getenv("GPG_PASSWORD")!!)
        sign(publishing.publications)
    }
}

