import java.io.FileNotFoundException
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
}

android {
    namespace = "com.bartosboth.rollen_android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bartosboth.rollen_android"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
        buildConfig = true
    }
    hilt {
        enableAggregatingTask = false
    }
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("bartosboth.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
} else {
    throw FileNotFoundException("Could not find bartosboth.properties file!")
}

android.buildTypes.forEach {
    it.buildConfigField(
        "String",
        "BASE_URL",
        "\"${localProperties.getProperty("BASE_URL")}\""
    )
}


dependencies {

    //Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    //Coroutines
    implementation(libs.kotlinx.coroutines)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter)

    //Coil
    implementation(libs.coil.compose)

    //RoomDB
    implementation(libs.room)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    //Serialization
    implementation(libs.kotlinx.serialization.json)

    //Media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)
    implementation (libs.androidx.media3.session)
    implementation (libs.androidx.media3.datasource)


    //System accompanist
    implementation (libs.accompanist.systemuicontroller)
    implementation (libs.accompanist.permissions)

    //Glide
    implementation(libs.glide)

    //Navigation
    implementation (libs.androidx.navigation.compose)

    //Lifecycle viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    //Security
    implementation(libs.androidx.security.crypto)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}