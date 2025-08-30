plugins {
  `kotlin-dsl`
}

repositories {
  mavenLocal()
  mavenCentral()
  gradlePluginPortal()
  maven("https://maven.aliyun.com/repository/public")
  maven("https://maven.aliyun.com/repository/gradle-plugin")
}

val kotlinVersion = libs.versions.kotlin.get()

dependencies {
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

  implementation(kotlin("gradle-plugin", version = kotlinVersion))
  implementation(kotlin("scripting-common", version = kotlinVersion))
  implementation(kotlin("scripting-jvm", version = kotlinVersion))
  implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
  implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:$kotlinVersion")

  implementation(libs.spring.boot.gradle.plugin)
  implementation(libs.spring.dependency.management)
}
