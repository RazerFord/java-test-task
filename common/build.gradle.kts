plugins {
    id("com.google.protobuf") version "0.9.4"
    java
}

dependencies {
    implementation("com.google.protobuf:protoc:3.25.2")
    implementation("org.jetbrains:annotations:24.0.0")
}

sourceSets {
    main {
        proto {
            srcDir("proto")
        }
        java {
            srcDir("src/main")
        }
    }
}

tasks.processResources.configure {
    dependsOn(tasks["generateProto"])
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.2"
    }
}
