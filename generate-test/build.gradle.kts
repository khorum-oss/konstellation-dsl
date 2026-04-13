plugins {
    id("com.google.devtools.ksp")
}

val metaDslVersion: String by rootProject.extra

dependencies {
    ksp(project(":konstellation-dsl"))
    implementation(rootProject.libs.konstellation.meta.dsl)
    implementation(rootProject.libs.kotlin.test.junit5)
    implementation(project(":cross-module-test"))
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
    arg("projectRootClasspath", "org.khorum.oss.konstellation.generatetest")
    arg("dslBuilderClasspath", "org.khorum.oss.konstellation.generatetest")
    arg("dslMarkerClass", "org.khorum.oss.konstellation.generatetest.TestDslMarker")
}

tasks.named("compileTestKotlin") {
    dependsOn("kspKotlin")
}
