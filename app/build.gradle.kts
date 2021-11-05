import com.cluster.buildsrc.Versions

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("io.objectbox")
}

kapt {
    arguments {
        arg("correctErrorTypes", "true")
        arg("useBuildCache", "true")
    }
}

object Config {
    const val NOTIFICATION_TOKEN = "C175CFBE-E036-487B-9CC5-D8DFD2199989"
}

android {
    compileSdk = Versions.compileSdk
    useLibrary("org.apache.http.legacy")

    defaultConfig {
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
        ndkVersion = "21.3.6528147"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        versionCode = 37
        versionName = "1.4.16"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true",
                    "objectbox.modelPath" to "$projectDir/schemas/objectbox.json",
                )
            }
        }
    }

    lint {
        // Eliminates UnusedResources false positives for resources used in DataBinding layouts
        checkGeneratedSources = true
        // Running lint over the debug variant is enough
        checkReleaseBuilds = false
        // Or, if you prefer, you can continue to check for errors in release builds,
        // but continue the build even when errors are found:
        abortOnError = false
    }

    buildFeatures {
        dataBinding = true
        compose = true
    }

    packagingOptions {
        resources.excludes += "META-INF/**"
        resources.excludes += "kotlin/**"
        resources.excludes += "okhttp3/**"
        resources.excludes += "org/**"
        resources.excludes += "**.properties"
        resources.excludes += "**.bin"
        resources.excludes += "META-INF/AL2.0"
        resources.excludes += "META-INF/LGPL2.1"
        resources.excludes += "/*.jar"
    }
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storePassword = "android"
        }
    }
    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "API_HOST", "\"https://www.cluster.africa\"")
            buildConfigField("String", "NOTIFICATION_TOKEN", "\"${Config.NOTIFICATION_TOKEN}\"")
            manifestPlaceholders["usesCleartextTraffic"] = "false"
        }

        create("releaseStaging") {
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "API_HOST", "\"http://52.168.85.231\"")
            buildConfigField("String", "NOTIFICATION_TOKEN", "\"${Config.NOTIFICATION_TOKEN}\"")
            manifestPlaceholders["usesCleartextTraffic"] = "true"
            initWith(getByName("release"))
            matchingFallbacks += listOf("release", "debug")
            signingConfig = signingConfigs.getByName("debug")
            versionNameSuffix = "-staging"
        }

        getByName("debug") {
            isDebuggable = true
            buildConfigField("String", "API_HOST", "\"http://52.168.85.231\"")
            buildConfigField("String", "NOTIFICATION_TOKEN", "\"${Config.NOTIFICATION_TOKEN}\"")
            manifestPlaceholders["usesCleartextTraffic"] = "true"
            extra["enableCrashlytics"] = false
            extra["alwaysUpdateBuildId"] = false
            configure<com.google.firebase.perf.plugin.FirebasePerfExtension> {
                setInstrumentationEnabled(false)
            }
            versionNameSuffix = "-dev"
        }
    }

    flavorDimensions += listOf("flavor")

    productFlavors {
        create("creditclub") {
            dimension = "flavor"
            applicationId = "com.appzonegroup.app.creditclub.plus"
        }
        create("polaris") {
            dimension = "flavor"
            applicationId = "com.appzonegroup.app.creditclub.polaris"
        }
        create("access") {
            dimension = "flavor"
            applicationId = "com.creditclub.access"
        }
        create("gtbank") {
            dimension = "flavor"
            applicationId = "com.creditclub.gtbank"
        }
        create("purple") {
            dimension = "flavor"
            applicationId = "com.creditclub.purple"
        }
        create("grooming") {
            dimension = "flavor"
            applicationId = "com.creditclub.grooming"
        }
        create("heritage") {
            dimension = "flavor"
            applicationId = "africa.cluster.heritage"
        }
    }
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main", "src/main/java")
        }
        // debug and releaseStaging variants share the same source dir
        getByName("debug") {
            java.srcDir("src/debugRelease/java")
        }
        getByName("releaseStaging") {
            java.srcDir("src/debugRelease/java")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.compose
    }
//    dynamicFeatures += setOf(":nexgo")
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi", "armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":uicentral"))
    implementation(project(":poscore"))
    implementation(project(":pos"))
    implementation(project(":analytics"))

    "creditclubImplementation"(project(":telpo"))
    "polarisImplementation"(project(":nexgo"))
    "polarisImplementation"(project(":telpo"))
//    "polarisImplementation"(project(":sunmi"))
    "accessImplementation"(project(":telpo"))
    "gtbankImplementation"(project(":dspread"))
//    "purpleImplementation"(project(":dspread"))
    "purpleImplementation"(project(":telpo"))
    "groomingImplementation"(project(":telpo"))
    "groomingImplementation"(project(":nexgo"))
    "heritageImplementation"(project(":wizar"))

    implementation("androidx.activity:activity-ktx:${Versions.activityKtx}")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // Lifecycle components
    kapt("androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycle}")

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.appcompat:appcompat:${Versions.appcompat}")
    implementation("androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}")
    implementation("com.google.android.material:material:${Versions.material}")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("de.hdodenhof:circleimageview:2.1.0")
    implementation("com.loopj.android:android-async-http:1.4.11")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")

    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}")

    implementation("androidx.room:room-runtime:${Versions.room}")
    kapt("androidx.room:room-compiler:${Versions.room}")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:${Versions.room}")

    // Test helpers
    testImplementation("androidx.room:room-testing:${Versions.room}")

    implementation("com.github.thoughtbot:expandable-recycler-view:v1.4") {
        exclude(group = "com.thoughtbot", module = "expandablerecyclerview")
    }

    testImplementation("junit:junit:4.13.2")

    // Co-routine Tests
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}")

    implementation("me.relex:circleindicator:2.1.6")
    implementation("com.squareup.picasso:picasso:2.71828")

    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:${Versions.navigation}")
    implementation("androidx.navigation:navigation-ui-ktx:${Versions.navigation}")

    // Dynamic Feature Module Support
    implementation("androidx.navigation:navigation-dynamic-features-fragment:${Versions.navigation}")

    implementation(platform("com.google.firebase:firebase-bom:28.4.2"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-core")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:${Versions.navigation}")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${Versions.desugarJdk}")

    implementation("com.github.smart-fun:XmlToJson:1.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling:${Versions.compose}")
    debugImplementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")
}
