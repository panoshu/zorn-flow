import org.gradle.accessors.dm.LibrariesForLibs

private val Project.libs: LibrariesForLibs
  get() = extensions.getByType()

plugins {
  id("java-common")
  id("custom-naming")
}

dependencies {
  compileOnly(libs.lombok)
  annotationProcessor(libs.lombok)

  testCompileOnly(libs.lombok)
  testAnnotationProcessor(libs.lombok)

  testImplementation(platform(libs.junit.bom))
  testImplementation(platform(libs.mockito.bom))

  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.junit.jupiter)
  testImplementation(libs.assertj.core)
}
