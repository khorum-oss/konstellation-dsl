plugins {
    id("com.google.devtools.ksp")
}

val metaDslVersion: String by rootProject.extra

repositories {
    maven {
        url = uri("https://reliquary.open.nyc3.cdn.digitaloceanspaces.com")
    }
}

dependencies {
    ksp(project(":dsl"))
    implementation("org.khorum.oss.konstellation:konstellation-meta-dsl:${metaDslVersion}")
    implementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(project(":core-test"))
}

kotlin {
    sourceSets {
        main {
            kotlin {
                // add KSP’s output dir for main
                srcDir("${layout.buildDirectory}/generated/ksp/main/kotlin")
            }
        }
    }
}


ksp {
    arg("projectRootClasspath", "io.violabs.konstellation.generateTest")
    arg("dslBuilderClasspath", "io.violabs.konstellation.generateTest")
    arg("dslMarkerClass", "io.violabs.konstellation.generateTest.TestDslMarker")
}