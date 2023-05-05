plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

android {
    namespace = "com.ptech.foodbank"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.ptech.foodbank"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    packaging {
        resources {
            excludes += "META-INF/proguard/okhttp3.pro"
            excludes += "META-INF/proguard/coroutines.pro"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/DEPENDENCIES.txt"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
    dependenciesInfo {
        includeInApk = true
        includeInBundle = false
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.core)
    implementation(libs.appcompat)
    implementation(libs.material.ui)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.swiperefreshlayout)
    implementation(libs.coil)

    // bundles
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.mapbox)
    implementation(libs.bundles.firebase)
    implementation(libs.bundles.play.services)

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.test.extensions)
}
