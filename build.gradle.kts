plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.dokka") version "1.9.20" apply false
    id("com.google.devtools.ksp") version "2.1.20-1.0.32" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
    id("org.sonarqube") version "7.0.0.6105"
    application
    id("org.khorum.oss.plugins.open.pipeline") version "1.0.3" apply false
    id("org.khorum.oss.plugins.open.secrets") version "1.0.3" apply false

    id("org.khorum.oss.plugins.open.publishing.maven-generated-artifacts") version "1.0.5" apply false
    id("org.khorum.oss.plugins.open.publishing.digital-ocean-spaces") version "1.0.5" apply false
}

group = "org.khorum.oss.konstellation"

sequenceOf(
    "coroutinesCoreVersion" to "1.10.0",
    "dslVersion" to file("VERSION").readText().trim(),
    "googleAutoServiceVersion" to "1.1.1",
    "junitJupiterVersion" to "5.13.0-M2",
    "kotlinPoetVersion" to "2.1.0",
    "kspVersion" to "2.1.20-1.0.32",
    "metaDslVersion" to "1.0.1",
    "mockkVersion" to "1.13.17",
    "serializationJsonVersion" to "1.8.1"
).forEach { (name, value) ->
    println("Setting $name to $value")
    extra[name] = value
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

sharedRepositories()

allprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.dokka")
        plugin("application")
        plugin("org.jetbrains.kotlinx.kover")
    }

    sharedRepositories()

    dependencies {
        implementation(kotlin("stdlib"))
        implementation("org.jetbrains.kotlin:kotlin-reflect")

        implementation("io.github.microutils:kotlin-logging:4.0.0-beta-2")

        testImplementation(kotlin("test")) // Kotlin’s own assert functions, optional but handy
        testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.0-M1")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }
}

fun Project.sharedRepositories() {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven { url = uri("https://www.jetbrains.com/intellij-repository/releases") }
        maven { url = uri("https://open-reliquary.nyc3.cdn.digitaloceanspaces.com") }
    }
}

tasks.register("koverMergedReport") {
    group = "verification"
    description = "Generates coverage report for the dsl module"

    dependsOn(project(":dsl").tasks.named("koverXmlReport"))
}

sonar {
    properties {
        property("sonar.projectKey", "khorum-oss_konstellation-dsl")
        property("sonar.organization", "khorum-oss")
        property("sonar.host.url", "https://sonarcloud.io")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${project(":dsl").layout.buildDirectory.get()}/reports/kover/report.xml"
        )
    }
}