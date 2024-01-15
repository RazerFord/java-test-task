plugins {
    java
}

dependencies {
    implementation(project(":common"))
}

java {
    sourceSets {
        main {
            java.setSrcDirs(listOf("src/main"))
        }
    }
}

val mainClassName = "ru.itmo.mit.Main"
project.setProperty("mainClassName", mainClassName)
