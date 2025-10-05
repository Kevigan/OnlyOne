import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) { file.inputStream().use { load(it) } }
}
val webClientId = localProperties.getProperty("WEB_CLIENT_ID") ?: ""
val privacyUrl = localProperties.getProperty("PRIVACY_URL") ?: "https://onlyone-a3285.web.app" // fallback

android {
    namespace = "com.onlyone.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.onlyone.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")
        buildConfigField("String", "PRIVACY_URL", "\"$privacyUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    // ✅ Release signing config reads from gradle.properties (see below)
    signingConfigs {
        create("release") {
            val storePath = (project.findProperty("RELEASE_STORE_FILE") as String?)
                ?: error("RELEASE_STORE_FILE missing in gradle.properties")

            println("→ Using release keystore at: $storePath")

            storeFile = file(storePath) // handles absolute paths fine
            storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String?
            keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String?
            keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String?
        }
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
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
    kotlinOptions { jvmTarget = "1.8" }

    buildFeatures {
        compose = true
        buildConfig = true // needed for BuildConfig.PRIVACY_URL / WEB_CLIENT_ID
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    // Google AdMob + UMP
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")

    implementation("com.google.code.gson:gson:2.10.1")

    // Work + Hilt
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    kapt("androidx.hilt:hilt-compiler:1.1.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Firebase BoM (keeps versions in sync)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-functions-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Navigation / Compose / Core
    val nav_version = "2.7.5"
    val compose_version = "1.6.0-alpha08"
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.material:material:$compose_version")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Compose BOM (you’re mixing explicit versions + BOM; leaving as-is to avoid breaks)
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("com.google.android.material:material:1.10.0")

    // Room
    val room = "2.6.0"
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    kapt("androidx.room:room-compiler:$room")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
