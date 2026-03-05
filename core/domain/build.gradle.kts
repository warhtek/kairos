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
}
