// Proyecto Gradle independiente del de la app: se ejecuta con ../gradlew -p backend run
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "tratohecho-servidor"
