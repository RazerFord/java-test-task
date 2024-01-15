plugins {
    java
}

dependencies {
    implementation(project(":common"))
}

sourceSets {
    main {
        java {
            srcDir("src/main")
        }
    }
}
