import org.gradle.accessors.dm.LibrariesForLibs

private val Project.libs: LibrariesForLibs
  get() = extensions.getByType()

plugins{
  id("java-common")
  id("custom-naming")
  id("org.springframework.boot")
  id("io.spring.dependency-management")
}

dependencies{
  implementation(platform(libs.spring.boot.dependency))
  implementation(platform(libs.spring.cloud.dependency))

  implementation(libs.spring.boot.starter)
  annotationProcessor(libs.spring.boot.configuration.processor)

  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.spring.boot.starter.test)
}
