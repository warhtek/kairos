import org.gradle.kotlin.dsl.androidTestImplementation

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "mobi.kairos.android.data"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
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
                packages(
                    "kotlinx.serialization.*",
                    "kotlinx.serialization.json.*",
                    "kotlinx.serialization.internal.*",
                    "kotlinx.serialization.encoding.*",
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

                    // Kotlin generated
                    "*\$DefaultImpls",
                    "*\$Companion",

                    // Kotlin Serialization generated
                    "*\$serializer",

                    // Opt-in annotations
                    "*\$*OptIn*",

                    // Test classes
                    "*Test*",
                )
                // exclude by annotation
                annotatedBy(
                    "Serializable",
                    "kotlinx.serialization.Serializable",
                    "kotlinx.serialization.InternalSerializationApi",
                    "kotlinx.serialization.ExperimentalSerializationApi",
                    "kotlin.OptIn",
                    "kotlin.RequiresOptIn",
                    "androidx.annotation.RequiresOptIn",
                    "kotlinx.coroutines.ExperimentalCoroutinesApi",
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
    implementation(libs.kotlinx.serialization.json)
    ksp(libs.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.koin.test)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.koin.test)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.junit.ktx)
}
