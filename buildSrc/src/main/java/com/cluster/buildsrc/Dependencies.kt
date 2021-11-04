package com.cluster.buildsrc

object Versions {
    const val compileSdk = 31
    const val minSdk = 21
    const val targetSdk = 31

    const val accompanist = "0.20.0"
    const val activityKtx = "1.4.0"
    const val appcompat = "1.3.1"
    const val coil = "1.4.0"
    const val constraintLayout = "2.1.1"
    const val coreKtx = "1.7.0"
    const val coroutines = "1.5.2"
    const val compose = "1.0.4"
    const val desugarJdk = "1.1.5"
    const val espresso = "3.3.0"
    const val extJunit = "1.1.0"
    const val fragmentKtx = "1.3.2"
    const val glide = "4.12.0"
    const val googleServices = "4.3.3"
    const val junit = "4.12"
    const val koin = "3.1.2"
    const val kotlin = "1.5.31"
    const val lifecycle = "2.4.0"
    const val material = "1.4.0"
    const val navigation = "2.3.5"
    const val objectBox = "2.9.1"
    const val okhttp = "4.9.2"
    const val retrofit = "2.9.0"
    const val room = "2.3.0"
    const val serialization = "1.3.0"
    const val work = "2.7.0"
}

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.2.0-alpha03"

    object Accompanist {
        const val version = "0.20.0"
        const val insets = "com.google.accompanist:accompanist-insets:$version"
    }

    object Kotlin {
        private const val version = "1.5.31"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
    }

    object Coroutines {
        private const val version = "1.5.2"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }

    object JUnit {
        private const val version = "4.13"
        const val junit = "junit:junit:$version"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.7.0"
        const val navigation = "androidx.navigation:navigation-compose:2.4.0-alpha03"

        object Activity {
            const val activityCompose = "androidx.activity:activity-compose:1.3.0-beta02"
        }

        object Compose {
            const val snapshot = ""
            const val version = "1.0.4"

            const val animation = "androidx.compose.animation:animation:$version"
            const val foundation = "androidx.compose.foundation:foundation:$version"
            const val layout = "androidx.compose.foundation:foundation-layout:$version"
            const val iconsExtended = "androidx.compose.material:material-icons-extended:$version"
            const val material = "androidx.compose.material:material:$version"
            const val runtime = "androidx.compose.runtime:runtime:$version"
            const val tooling = "androidx.compose.ui:ui-tooling:$version"
            const val ui = "androidx.compose.ui:ui:$version"
            const val uiUtil = "androidx.compose.ui:ui-util:$version"
            const val uiTest = "androidx.compose.ui:ui-test-junit4:$version"
        }

        object ConstraintLayout {
            const val constraintLayoutCompose =
                "androidx.constraintlayout:constraintlayout-compose:1.0.0-alpha08"
        }

        object Test {
            private const val version = "1.3.0"
            const val core = "androidx.test:core:$version"
            const val rules = "androidx.test:rules:$version"

            object Ext {
                private const val version = "1.1.2"
                const val junit = "androidx.test.ext:junit-ktx:$version"
            }

            const val espressoCore = "androidx.test.espresso:espresso-core:3.3.0"
        }
    }
}
