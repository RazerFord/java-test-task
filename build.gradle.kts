plugins {
    application
}

group = "ru.itmo.mit"
version = "1.0-SNAPSHOT"

subprojects {
    apply {
        plugin("application")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("com.google.protobuf:protobuf-java:3.25.2")
    }
}
