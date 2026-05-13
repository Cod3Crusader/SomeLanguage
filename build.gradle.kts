plugins {
    kotlin("jvm") version "2.3.10"
    application
}

group = "com.archvin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
}

application {
    mainClass.set("com.archvin.MainKt")
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDirs("src/main/kotlin/packageName")
        }
    }
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}