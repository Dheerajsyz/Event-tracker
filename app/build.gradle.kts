plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.dheeraj.snhu_dheeraj_kollapaneni"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dheeraj.snhu_dheeraj_kollapaneni"
        minSdk = 34
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation ("com.google.android.gms:play-services-auth:20.5.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")  // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}