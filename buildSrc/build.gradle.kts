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

dependencies {
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

  implementation(libs.spring.boot.gradle.plugin)
  implementation(libs.spring.dependency.management)

}
