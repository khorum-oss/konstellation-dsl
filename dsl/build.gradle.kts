import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.khorum.oss.plugins.open.secrets.getPropertyOrEnv

val dslVersion: String by rootProject.extra
val metaDslVersion: String by rootProject.extra

plugins {
    id("io.gitlab.arturbosch.detekt")
    `java-library`
    `maven-publish`
    signing

    id("org.khorum.oss.plugins.open.secrets")
    id("org.khorum.oss.plugins.open.publishing.maven-generated-artifacts")
    id("org.khorum.oss.plugins.open.publishing.digital-ocean-spaces")
}

group = "org.khorum.oss.konstellation"
version = dslVersion

val kotlinPoetVersion: String by project
val kspVersion: String by project
val googleAutoServiceVersion: String by project
val mockkVersion: String by project

dependencies {
    implementation("org.khorum.oss.konstellation:konstellation-meta-dsl:$metaDslVersion")
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
    implementation("com.squareup:kotlinpoet-ksp:$kotlinPoetVersion")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    implementation("com.google.auto.service:auto-service:$googleAutoServiceVersion")

    testImplementation(project(":core-test"))
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.jar {
    archiveBaseName.set("dsl")
}

kover {
    reports {
        filters {
            excludes {
                annotatedBy("org.khorum.oss.konstellation.dsl.common.ExcludeFromCoverage")
            }
        }
    }
}


detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
//    config.setFrom("$projectDir/config/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
//    baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
        txt.required.set(true) // similar to the console output, contains issue signature to manually edit baseline files
        sarif.required.set(true) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with GitHub Code Scanning
        md.required.set(true) // simple Markdown format
    }
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = JavaVersion.VERSION_21.majorVersion
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = JavaVersion.VERSION_21.majorVersion
}

digitalOceanSpacesPublishing {
    bucket = "open-reliquary"
    accessKey = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
    secretKey = project.getPropertyOrEnv("spaces.secret", "DO_SPACES_SECRET")
    publishedVersion = version.toString()
    signingRequired = true
}

signing {
    val signingKey = providers.environmentVariable("GPG_SIGNING_KEY").orNull
    val signingPassword = providers.environmentVariable("GPG_SIGNING_PASSWORD").orNull

    if (signingKey != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    } else {
        useGpgCmd()
    }
    sign(publishing.publications)
    afterEvaluate {
        tasks.named("uploadToDigitalOceanSpaces") {
            dependsOn(tasks.withType<Sign>())
        }
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    dependsOn(tasks.withType<Sign>())
}

mavenGeneratedArtifacts {
    publicationName = "digitalOceanSpaces"
    name = "Konstellation DSL Builder"
    description = """
            An annotation based DSL Builder for Kotlin.
        """
    websiteUrl = "https://github.com/violabs/konstellation/tree/main/dsl"

    licenses {
        license {
            name = "Apache License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        }
    }

    developers {
        developer {
            id = "violabs"
            name = "Violabs Team"
            email = "support@violabs.io"
            organization = "Violabs Software"
        }
    }

    scm {
        connection = "https://github.com/violabs/konstellation.git"
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17) // Specify your desired Java version here
    }
}