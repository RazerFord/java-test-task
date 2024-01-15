plugins {
    java
}

dependencies {
    implementation(project(":common"))
    implementation(project(":servers"))
}

sourceSets {
    main {
        java {
            srcDir("src/main")
        }
    }
}
