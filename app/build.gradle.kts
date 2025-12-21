plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ggscc.app"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.ggscc.app"
        minSdk = 28
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
    }

    flavorDimensions += "privilege"

    productFlavors {
        create("privileged") {
            dimension = "privilege"
            // Default build: privileged app without platform signature requirement
        }
        create("systemUid") {
            dimension = "privilege"
            // Optional build that uses android.uid.system; requires platform signing keys
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../keys/platform.p12")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
        create("release") {
            storeFile = file("../keys/platform.p12")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            isDebuggable = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    useLibrary("android.car")

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.media:media:1.6.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
}
