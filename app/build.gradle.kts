import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "fr.sommelle"
    compileSdk = 35

    defaultConfig {
        applicationId = "fr.sommelle"
        minSdk = 26
        targetSdk = 35
        // En publication, la CI fournit le numéro et le nom de version (étiquette v1.2.3).
        versionCode = System.getenv("SOMMELLE_VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("SOMMELLE_VERSION_NAME")?.removePrefix("v") ?: "0.1.0"
    }

    // La clé de signature n'est jamais dans le dépôt : elle vient des secrets de la CI.
    val keystorePath = System.getenv("SOMMELLE_KEYSTORE_PATH")
    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("SOMMELLE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("SOMMELLE_KEY_ALIAS")
                keyPassword = System.getenv("SOMMELLE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:home"))
    implementation(project(":feature:diary"))
    implementation(project(":feature:week"))
    implementation(project(":feature:night"))
    implementation(project(":feature:reminders"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:export"))
    implementation(project(":feature:cognitive"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
