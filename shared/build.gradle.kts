import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }
    }

    val iosTargets = listOf(iosArm64(), iosSimulatorArm64())

    iosTargets.forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true

            // BURAYI GÜNCELLE:
            linkerOpts += listOf(
//                // 1. Kütüphane klasörünün yolunu gösteriyoruz
//                "-L${project.file("src/nativeInterop/cinterop/lib")}",
//
//                // 2. Box2D ana motorunu zorla bağlıyoruz (libbox2d.a) -> ÇÖZÜM BU
//                "-lbox2d",
//
//                // 3. Eğer shared kütüphanesini de kullanıyorsan onu da ekle (libshared.a)
//                "-lshared",

                // 4. C++ desteği
                "-lc++"
            )
        }

        iosTarget.compilations.getByName("main") {
            val box2d by cinterops.creating {
                // Def dosyasının yolu
                defFile(project.file("src/nativeInterop/cinterop/box2d.def"))

                // Header dosyalarının klasörü
                includeDirs(project.file("src/nativeInterop/cinterop/include"))

             }
        }
    }

    sourceSets {
        commonMain.dependencies {
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.hsntncy.threebodysimulation.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    externalNativeBuild {
        cmake {
            path = file("src/androidMain/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}