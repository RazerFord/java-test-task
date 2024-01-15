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
