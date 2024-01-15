plugins {
    id("com.google.protobuf") version "0.9.4"
}

dependencies {
    implementation("com.google.protobuf:protoc:3.25.2")
}

sourceSets {
    main {
        proto {
            srcDir("proto")
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
