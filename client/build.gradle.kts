plugins {
    java
}

dependencies {
    implementation(project(":common"))
    implementation("org.jetbrains:annotations:24.0.0")
}

java {
    sourceSets {
        main {
            java.setSrcDirs(listOf("src/main"))
            resources.setSrcDirs(listOf("src/resources"))
        }
        test {
            java.setSrcDirs(listOf("src/test"))
        }
    }
}
