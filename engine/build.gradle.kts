import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Files
import java.nio.file.Path

plugins {
  kotlin("jvm") version "1.3.61"
  kotlin("plugin.spring") version "1.3.61"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
  mavenCentral()
}

dependencies {
  implementation(platform("org.springframework.boot:spring-boot-starter-parent:2.2.5.RELEASE"))

  implementation(project(":library"))

  implementation("io.github.classgraph:classgraph:4.8.65")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.opentest4j:opentest4j:1.2.0")
  implementation("org.junit.platform:junit-platform-engine")

  implementation("org.slf4j:slf4j-api")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}

fun Path.mkdir(): Path =
    if (!Files.exists(this)) Files.createDirectories(this)
    else this

fun Path.write(text: String): Path =
    Files.writeString(this, text, Charsets.UTF_8)

tasks {
  create("generateResource") {
    doLast { 
      project.sourceSets["main"].output.resourcesDir
          ?.toPath()
          ?.resolve("META-INF/services")
          ?.mkdir()
          ?.resolve("org.junit.platform.engine.TestEngine")
          ?.write("com.example.KotlinTestEngine")
    }
  }

  "jar"{
    dependsOn("generateResource")
  }
}
