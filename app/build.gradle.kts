plugins {
    id("com.android.application")
}

android {
    namespace = "com.inwrist.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.inwrist.app"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.wear:wear:1.3.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
