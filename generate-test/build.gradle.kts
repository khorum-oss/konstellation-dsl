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
    arg("projectRootClasspath", "org.khorum.oss.konstellation.generateTest")
    arg("dslBuilderClasspath", "org.khorum.oss.konstellation.generateTest")
    arg("dslMarkerClass", "org.khorum.oss.konstellation.generateTest.TestDslMarker")
}