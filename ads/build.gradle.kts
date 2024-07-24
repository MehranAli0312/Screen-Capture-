plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

android {
    namespace = "com.example.ads"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )


            resValue("string", "app_id", "ca-app-pub-3940256099942544~3347511713")

            resValue("string", "app_open", "ca-app-pub-3940256099942544/9257395921")

            resValue("string", "banner", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "inline_banner", "ca-app-pub-3940256099942544/2014213617")

            resValue("string", "interstitial", "ca-app-pub-3940256099942544/1033173712")

//            resValue("string", "rewarded", "ca-app-pub-3940256099942544/5224354917")
            resValue("string", "rewarded_interstitial", "ca-app-pub-3940256099942544/5354046379")

//            original
            resValue("string", "rewarded", "ca-app-pub-7055337155394452/3223556338")

            resValue("string", "native_advanced_video", "ca-app-pub-3940256099942544/2247696110")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "app_id", "ca-app-pub-7055337155394452~7843056688")

            resValue("string", "app_open", "ca-app-pub-7055337155394452/1791055161")

            resValue("string", "banner", "ca-app-pub-7055337155394452/5538728480")
            resValue("string", "inline_banner", "")

            resValue("string", "interstitial", "ca-app-pub-7055337155394452/4225646815")

            resValue("string", "rewarded", "ca-app-pub-7055337155394452/3223556338")
            resValue("string", "rewarded_interstitial", "")

            resValue("string", "native_advanced_video", "ca-app-pub-7055337155394452/7767088968")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // admob
    api("com.google.android.gms:play-services-ads:23.2.0")

    //shimmer
    api("com.facebook.shimmer:shimmer:0.5.0")

    //
    implementation(libs.ssp)
    implementation(libs.sdp)

    // lottie animation
    implementation("com.airbnb.android:lottie:6.4.0")


    // hilt DI
//    implementation("com.google.dagger:hilt-android:2.50")
//    kapt("com.google.dagger:hilt-compiler:2.50")

}