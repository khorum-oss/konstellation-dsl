plugins {
    id("com.google.devtools.ksp")
}

val metaDslVersion: String by rootProject.extra

dependencies {
    ksp(project(":dsl"))
    implementation(rootProject.libs.konstellation.meta.dsl)
    implementation(rootProject.libs.kotlin.test.junit5)
    testImplementation(project(":core-test"))
}

kotlin {
    sourceSets {
        main {
            kotlin {
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

tasks.named("compileTestKotlin") {
    dependsOn("kspKotlin")
}
