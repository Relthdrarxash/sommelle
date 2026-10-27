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

// Socle pur Kotlin (sans dépendance Android), testable et relisible indépendamment de l'interface.
include(":core:model")
include(":core:protocol")

// Modules Android à venir (voir ARCHITECTURE.md, section 5) :
// :app, :core:data, :core:designsystem, :core:ui,
// :feature:onboarding, :feature:diary, :feature:window, :feature:reminders, ...
