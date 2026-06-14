import com.github.gradle.node.npm.task.NpmTask
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    // Downloads a local Node.js + npm and runs the frontend build.
    // Candidates do NOT need Node.js installed on their machine.
    id("com.github.node-gradle.node") version "7.1.0"
}

group = "com.orderdesk"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("com.h2database:h2")
    // Spring Boot 4 moved the H2 web console into its own optional module.
    // Without this, spring.h2.console.enabled has no effect (/h2-console 404s).
    runtimeOnly("org.springframework.boot:spring-boot-h2console")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

node {
    version = "24.11.0"
    download = true
    nodeProjectDir = file("frontend")
}

val npmBuild by tasks.registering(NpmTask::class) {
    dependsOn(tasks.npmInstall)
    args = listOf("run", "build")
    inputs.dir("frontend/src")
    inputs.files(
        "frontend/package.json",
        "frontend/package-lock.json",
        "frontend/vite.config.js",
        "frontend/index.html",
    )
    outputs.dir(layout.projectDirectory.dir("frontend/dist"))
}

tasks.processResources {
    // Pass -PskipFrontend for faster backend-only restarts once the
    // frontend has been built at least once.
    if (!project.hasProperty("skipFrontend")) {
        dependsOn(npmBuild)
    }
    from("frontend/dist") {
        into("static")
    }
}
