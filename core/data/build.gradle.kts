plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
}

android {
    namespace = "mobi.kairos.android.data"
    compileSdk = 36
    defaultConfig { minSdk = 23 }
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
    implementation(project(":core:assets"))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    testImplementation(libs.junit)
    testImplementation("androidx.test:core:1.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation(libs.robolectric)
    testImplementation(libs.koin.test)
}
