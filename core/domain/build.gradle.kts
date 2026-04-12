plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "mobi.kairos.android.domain"
    compileSdk = 36
    defaultConfig { minSdk = 23 }
}
dependencies {
    implementation(libs.koin.core)
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
