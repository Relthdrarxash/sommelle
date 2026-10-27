pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "sommelle"

include(":app")

// Socle pur Kotlin (sans dépendance Android), testable et relisible indépendamment de l'interface.
include(":core:model")
include(":core:protocol")
include(":core:presentation")

// Socle Android.
include(":core:data")
include(":core:designsystem")

// Écrans.
include(":feature:onboarding")
include(":feature:home")
include(":feature:diary")
include(":feature:week")
include(":feature:night")

// Captures d'écran de la documentation, générées par la CI (docs/captures).
include(":screenshots")
