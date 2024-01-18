plugins {
    java
}

dependencies {
    implementation(project(":common"))
    implementation(project(":client"))
    implementation(project(":servers"))
    implementation("org.jetbrains:annotations:24.0.0")
}

sourceSets {
    main {
        java {
            srcDir("src/main")
        }
    }
}
