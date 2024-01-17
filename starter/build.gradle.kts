plugins {
    java
}

dependencies {
    implementation(project(":common"))
    implementation(project(":client"))
    implementation(project(":servers"))
}

sourceSets {
    main {
        java {
            srcDir("src/main")
        }
    }
}
