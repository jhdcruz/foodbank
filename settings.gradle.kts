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

plugins {
    `gradle-enterprise`
}

gradleEnterprise {
    if (System.getenv("CI") != null) {
        buildScan {
            publishAlways()
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "foodbank"
include(":app")
