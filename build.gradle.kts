import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.fabric.loom)
  `maven-publish`
  alias(libs.plugins.kotlin.jvm)
}

version = providers.gradleProperty("mod_version").get()

group = providers.gradleProperty("maven_group").get()

base { archivesName.set(providers.gradleProperty("archives_base_name").get()) }

loom {
  splitEnvironmentSourceSets()

  mods {
    register("minecraft-heading-marker") {
      sourceSet(sourceSets.main.get())
      sourceSet(sourceSets.getByName("client"))
    }
  }
}

dependencies {
  implementation(libs.annotations)

  // JUnit for unit testing
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)

  minecraft(libs.minecraft)
  implementation(libs.fabric.loader)

  // Fabric API and Kotlin support
  implementation(libs.fabric.api)
  implementation(libs.fabric.kotlin)
}

tasks.test { useJUnitPlatform() }

tasks.processResources {
  inputs.property("version", version)

  filesMatching("fabric.mod.json") { expand(mapOf("version" to version)) }
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(25)
  options.encoding = "UTF-8"
  options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_25) } }

java {
  withSourcesJar()
  sourceCompatibility = JavaVersion.VERSION_25
  targetCompatibility = JavaVersion.VERSION_25
  toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
}

tasks.jar {
  from("LICENSE") { rename { "${it}_${base.archivesName.get()}" } }

  manifest {
    attributes(
        "Implementation-Title" to project.name,
        "Implementation-Version" to project.version,
        "Built-By" to System.getProperty("user.name"),
        "Built-JDK" to System.getProperty("java.version"),
        "Specification-Title" to "Heading Marker",
        "Specification-Version" to project.version,
    )
  }
}

// Helpful development tasks
tasks.register("listTasksToFile") {
    doLast {
        val file = file("task_list.txt")
        file.writeText(tasks.map { it.name }.joinToString("\n"))
    }
}

tasks.register("cleanBuildCache") {
  group = "build"
  description = "Clean Gradle and Loom caches"
  doLast {
    delete(file(".gradle/loom-cache"))
    delete(layout.buildDirectory)
    println("Cleaned build and Loom caches")
  }
}

tasks.register<Copy>("deployDevelopmentDatapack") {
  group = "other"
  description = "Deploys the datapack to the development server world"
  from("datapack_for_headingmarker/headingmarker_datapack")
  into(
      "C:/Users/braks/curseforge/minecraft/Instances/Minecraft/saves/Dev World/datapacks/headingmarker_datapack"
  )
  doLast { println("Copied datapack to dev world.") }
}

publishing {
  publications {
    register<MavenPublication>("mavenJava") {
      artifactId = base.archivesName.get()
      from(components["java"])
    }
  }
}
