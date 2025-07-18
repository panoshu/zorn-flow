import org.gradle.accessors.dm.LibrariesForLibs

private val Project.libs: LibrariesForLibs
  get() = extensions.getByType()

plugins{
  id("java-common")
  id("custom-naming")
  id("io.spring.dependency-management")
}

dependencies {
  implementation(platform(libs.spring.boot.dependency))
  implementation(platform(libs.spring.cloud.dependency))

  compileOnly(libs.lombok)
  annotationProcessor(libs.lombok)

  testCompileOnly(libs.lombok)
  testAnnotationProcessor(libs.lombok)
  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.spring.boot.starter.test)
}
