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
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.0-RC")
    implementation("io.coil-kt:coil:2.3.0")

    // mapbox
    implementation("com.mapbox.maps:android:10.12.2")
    implementation("com.mapbox.search:discover:1.0.0-rc.3")
    implementation("com.mapbox.search:mapbox-search-android-ui:1.0.0-rc.3")
    implementation("com.mapbox.search:mapbox-search-android:1.0.0-rc.3")

    // firebase
    implementation("com.google.firebase:firebase-crashlytics:18.3.6")
    implementation("com.google.firebase:firebase-analytics:21.2.2")
    implementation("com.google.firebase:firebase-firestore-ktx:24.5.0")
    implementation("com.google.firebase:firebase-perf-ktx:20.3.1")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
