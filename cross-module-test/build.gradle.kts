plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    ksp(project(":konstellation-dsl"))
    implementation(rootProject.libs.konstellation.meta.dsl)
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
    arg("projectRootClasspath", "org.khorum.oss.konstellation.crossModuleTest")
    arg("dslBuilderClasspath", "org.khorum.oss.konstellation.crossModuleTest")
    arg("dslMarkerClass", "org.khorum.oss.konstellation.crossModuleTest.CrossModuleDslMarker")
}
