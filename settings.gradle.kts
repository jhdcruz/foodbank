pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()

        maven {
            name = "mapbox"
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")

            authentication {
                create<BasicAuthentication>("basic")
            }

            // relies on the mapboxUsername and mapboxPassword
            // in gradle.properties at $HOME/.gradle
            credentials(PasswordCredentials::class)
        }
    }
}

rootProject.name = "Food Bank"
include(":app")
