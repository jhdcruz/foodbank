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

            // finds `mapboxPassword` on gradle.properties
            credentials {
                username = "mapbox"
                password = providers.gradleProperty("mapboxPassword").orNull
            }
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
