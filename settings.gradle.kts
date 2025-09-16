pluginManagement {
    repositories {
        flatDir {
            dirs("libs")
        }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()

        maven("https://jitpack.io")
        maven("https://mvn.0110.be/releases")
    }
}

rootProject.name = "Guitar Tuner"
include(":app")
