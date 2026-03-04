plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
}

android {
    namespace = "mobi.kairos.android"
    compileSdk = 36
    defaultConfig { minSdk = 23; targetSdk = 36 }
    buildFeatures { compose = true }
    buildTypes { debug { enableUnitTestCoverage = true } }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}
kover {
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
                annotatedBy(
                    "androidx.compose.ui.tooling.preview.Preview"
                )
                classes(
                    // Room generated
                    "*_Impl",
                    "*_Impl\$*",
                    "*\$DefaultImpls",
                    "*_AutoMigration_*",
                    // Room DAOs generated
                    "*Dao_Impl",
                    "*Dao_Impl\$*",
                    // Room Database generated
                    "*Database_Impl",
                )
            }
        }
    }
}
dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":ui:home"))
    kover(project(":core:data"))
    kover(project(":ui:home"))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test)
}
