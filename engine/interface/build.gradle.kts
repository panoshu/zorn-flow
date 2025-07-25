plugins {
  id("spring-boot-module")
}

dependencies {
  api(project(":engine:api"))
  api(project(":engine:application"))
}
