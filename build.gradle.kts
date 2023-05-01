import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

buildscript {
    dependencies {
        classpath(libs.gradle)
        classpath(libs.google.services.gradle)
        classpath(libs.firebase.crashlytics.gradle)
        classpath(libs.perf.plugin.gradle)
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.detekt)
}

val detektReportMergeSarif by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif"))
}

tasks.withType<Detekt>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        @Suppress("UseTomlInstead")
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
    }

    detekt {
        parallel = true
        ignoreFailures = true
        buildUponDefaultConfig = true

        config.setFrom(files("${rootProject.projectDir}/detekt.yml"))
    }

    tasks.withType<Detekt> {
        basePath = rootProject.projectDir.absolutePath
        jvmTarget = "1.8"

        reports {
            xml.required.set(true)
            html.required.set(true)
            sarif.required.set(true)
        }

        finalizedBy(detektReportMergeSarif)
    }

    // Merge detekt report into sarif file for CodeQL scanning
    detektReportMergeSarif.configure {
        input.from(tasks.withType<Detekt>().map { it.sarifReportFile })
    }

    tasks.withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget = "1.8"
    }
}
