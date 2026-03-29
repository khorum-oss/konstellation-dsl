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

dependencies {
    implementation(rootProject.libs.konstellation.meta.dsl)
    implementation(kotlin("stdlib"))
    implementation(rootProject.libs.kotlin.reflect)
    implementation(rootProject.libs.kotlinpoet)
    implementation(rootProject.libs.kotlinpoet.ksp)
    implementation(rootProject.libs.ksp.api)
    implementation(rootProject.libs.google.auto.service)

    testImplementation(project(":core-test"))
    testImplementation(rootProject.libs.mockk)
}

tasks.jar {
    archiveBaseName.set("konstellation-dsl")
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
    buildUponDefaultConfig = true
    allRules = false
    source.setFrom("src/main/kotlin")
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
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
    websiteUrl = "https://github.com/khorum-oss/konstellation-dsl/tree/main/dsl"

    licenses {
        license {
            name = "Apache License, Version 2.0"
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        }
    }

    developers {
        developer {
            id = "khorum-oss"
            name = "Khorum OSS Team"
            email = "khorum.oss@gmail.com"
            organization = "Khorum OSS"
        }
    }

    scm {
        connection = "https://github.com/khorum-oss/konstellation-dsl.git"
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
