import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "pl.kurczak.idea"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.61"
    id("org.jetbrains.intellij").version("0.4.14")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

intellij {
    version = "IC-193.5233.102"
    instrumentCode = false // See: https://github.com/JetBrains/gradle-intellij-plugin/issues/230
//    setPlugins("intellij-dvcs")
}
