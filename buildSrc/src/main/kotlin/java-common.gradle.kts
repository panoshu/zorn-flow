import org.gradle.accessors.dm.LibrariesForLibs

private val Project.libs: LibrariesForLibs
  get() = extensions.getByType()

plugins {
  `java-library`
  idea
}

group = "com.panoshu"
version = "1.0.0"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(libs.versions.javaLanguageVersion.get())
  }
}

repositories {
  mavenLocal()
  maven("https://maven.aliyun.com/repository/public")
  maven("https://maven.aliyun.com/repository/central")
  maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
  mavenCentral()
}

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  jvmArgs("-XX:+EnableDynamicAgentLoading", "-Dfile.encoding=UTF-8")
}

tasks.withType<JavaExec>().configureEach {
  jvmArgs("-Dfile.encoding=UTF-8")
}
