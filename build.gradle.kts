plugins {
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenCentral()
    maven{
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation ("com.github.johnsonoxr:exnumber:1.0.3")
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
}

tasks {
    wrapper {
        gradleVersion = "8.5"
    }
}
