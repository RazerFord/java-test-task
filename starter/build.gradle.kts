import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

dependencies {
    implementation(project(":common"))
    implementation(project(":client"))
    implementation(project(":servers"))
    implementation("org.jfree:jfreechart:1.5.4")
    implementation("org.jetbrains:annotations:24.0.0")
}

sourceSets {
    main {
        java {
            srcDir("src/main")
        }
    }
}

val mainClassName = "ru.itmo.mit.Main"
project.setProperty("mainClassName", mainClassName)

tasks.withType(Jar::class) {
    manifest {
        attributes["Main-Class"] = mainClassName
    }
}

tasks.withType(ShadowJar::class.java) {
    archiveBaseName.set("starter")
    archiveClassifier.set("")
    archiveVersion.set("")
}
