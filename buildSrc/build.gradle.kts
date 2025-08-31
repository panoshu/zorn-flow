plugins {
  `kotlin-dsl`
}

repositories {
  mavenLocal()
  maven("https://maven.aliyun.com/repository/public")
  maven("https://maven.aliyun.com/repository/gradle-plugin")
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

  implementation(libs.spring.boot.gradle.plugin)
  implementation(libs.spring.dependency.management)

}
