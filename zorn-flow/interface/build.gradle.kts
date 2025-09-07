plugins {
  id("spring-boot-module")
}

dependencies {
  implementation(project(":zorn-flow:api"))
  implementation(project(":zorn-flow:application"))
}
