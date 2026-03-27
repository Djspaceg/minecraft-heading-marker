pluginManagement {
  repositories {
    maven {
      name = "Fabric"
      url = uri("https://maven.fabricmc.net/")
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  // Loom is incompatible with FAIL_ON_PROJECT_REPOS and PREFER_SETTINGS
  // because it injects internal repositories into the project.
  repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
  repositories {
    mavenCentral()
    maven {
      name = "Fabric"
      url = uri("https://maven.fabricmc.net/")
    }
    maven {
      name = "Minecraft"
      url = uri("https://libraries.minecraft.net/")
    }
  }
}

rootProject.name = "Minecraft Heading Marker"

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }
