plugins {
  id("spring-boot-module")
}

dependencies {
  implementation(project(":zorn-flow:domain:engine"))
  implementation(project(":zorn-flow:infrastructure"))
}
