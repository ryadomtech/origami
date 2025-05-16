import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvmToolchain(1_8)
    jvm()

    js(IR) {
        nodejs()
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        binaries.executable()
    }

    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
        }
    }

//    @OptIn(ExperimentalKotlinGradlePluginApi::class)
//    publishing {
//        repositories {
//            maven {
//                name = "GitHub"
//                url = uri("https://maven.pkg.github.com/ryadomtech/origami")
//                credentials {
//                    username = project.findProperty("gpr.user") as String?
//                        ?: System.getenv("GITHUB_ACTOR")
//
//                    password = project.findProperty("gpr.key") as String?
//                        ?: System.getenv("GITHUB_TOKEN")
//                }
//            }
//        }
//    }
}

android {
    namespace = "tech.ryadom.origami"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}