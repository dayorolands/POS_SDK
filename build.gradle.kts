buildscript {
    val buildVersions = com.cluster.buildsrc.Versions
    val versions = mapOf(
        "compileSdk" to buildVersions.compileSdk,
        "minSdk" to buildVersions.minSdk,
        "targetSdk" to buildVersions.targetSdk,
        "accompanist" to buildVersions.accompanist,
        "activityKtx" to buildVersions.activityKtx,
        "appcompat" to buildVersions.appcompat,
        "coil" to buildVersions.coil,
        "constraintLayout" to buildVersions.constraintLayout,
        "coreKtx" to buildVersions.coreKtx,
        "coroutines" to buildVersions.coroutines,
        "compose" to buildVersions.compose,
        "desugarJdk" to buildVersions.desugarJdk,
        "espresso" to buildVersions.espresso,
        "extJunit" to buildVersions.extJunit,
        "fragmentKtx" to buildVersions.fragmentKtx,
        "googleServices" to buildVersions.googleServices,
        "junit" to buildVersions.junit,
        "koin" to buildVersions.koin,
        "kotlin" to buildVersions.kotlin,
        "lifecycle" to buildVersions.lifecycle,
        "material" to buildVersions.material,
        "navigation" to buildVersions.navigation,
        "objectBox" to buildVersions.objectBox,
        "okhttp" to buildVersions.okhttp,
        "retrofit" to buildVersions.retrofit,
        "room" to buildVersions.room,
        "serialization" to buildVersions.serialization,
        "work" to buildVersions.work,
    )

    extra.apply {
        set("versions", versions)
    }

    repositories {
        mavenCentral()
        google()
        maven { url = java.net.URI.create("https://jitpack.io") }
        jcenter()
    }
    dependencies {
        classpath(com.cluster.buildsrc.Libs.androidGradlePlugin)
        classpath(com.cluster.buildsrc.Libs.Kotlin.gradlePlugin)
        classpath("org.jetbrains.kotlin:kotlin-serialization:${buildVersions.kotlin}")
        classpath("com.google.gms:google-services:${buildVersions.googleServices}")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.0")
        classpath("com.google.firebase:perf-plugin:1.4.0")
        classpath("io.objectbox:objectbox-gradle-plugin:${buildVersions.objectBox}")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven { url = java.net.URI.create("https://jitpack.io") }
        jcenter()
    }
}
task<Delete>("clean") {
    delete(rootProject.buildDir)
}
