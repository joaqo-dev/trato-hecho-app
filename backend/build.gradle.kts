import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.2.10"
    application
}

dependencies {
    // Servidor HTTP (Ktor con Netty)
    implementation("io.ktor:ktor-server-core:3.2.3")
    implementation("io.ktor:ktor-server-netty:3.2.3")
    implementation("ch.qos.logback:logback-classic:1.5.18")
    // JSON (el mismo formato que usa Retrofit en la app)
    implementation("com.google.code.gson:gson:2.13.1")
    // Base de datos: PostgreSQL en producción, SQLite en desarrollo
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

application {
    mainClass.set("cl.duoc.tratohecho.servidor.MainKt")
    // SQLite carga una librería nativa; así Java no muestra advertencias
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

// La base de datos queda en la carpeta backend/
tasks.named<JavaExec>("run") {
    workingDir = projectDir
}
