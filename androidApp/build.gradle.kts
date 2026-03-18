plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
}

android {
    namespace = "mobi.kairos.android"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
        targetSdk = 36
    }
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
    implementation(project(":core:data"))
    implementation(project(":ui:home"))
    kover(project(":core:data"))
    kover(project(":ui:home"))
    implementation(project(":ui:books"))

    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.kotlinx.serialization.json)
    ksp(libs.room.compiler)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.compose.ui.test)
}
