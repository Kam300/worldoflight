plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.worldoflight"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.worldoflight"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")


        // Supabase - используем стабильную версию
        implementation(platform("io.github.jan-tennert.supabase:bom:2.6.0"))
        implementation("io.github.jan-tennert.supabase:postgrest-kt")
        implementation("io.github.jan-tennert.supabase:gotrue-kt")

        // Ktor
        implementation("io.ktor:ktor-client-android:2.3.12")
        implementation("io.ktor:ktor-client-core:2.3.12")

        // Kotlinx Serialization
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

        // Security
        implementation("androidx.security:security-crypto:1.1.0-alpha06")




    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.play.services.auth)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Для круглых изображений
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Для загрузки изображений
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("io.github.jan-tennert.supabase:storage-kt")
}
