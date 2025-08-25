plugins {
  id("spring-boot-module")
}

dependencies {
  api(project(":zorn-flow:api"))
  api(project(":zorn-flow:application"))
}
