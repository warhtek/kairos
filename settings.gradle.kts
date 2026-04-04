pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    
    // Forzar la versión 9.0.1 del plugin de Android
    plugins {
        id("com.android.application") version "9.0.1"
        id("com.android.library") version "9.0.1"
        id("org.jetbrains.kotlin.android") version "2.3.10"
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("versions.toml"))
        }
    }
}

include(":core:domain", ":core:data", ":core:assets")
include(":ui:common", ":ui:home")
include(":androidApp")
include(":ui:books")
include(":ui:translations")
include(":ui:search")
include(":ui:splash")
