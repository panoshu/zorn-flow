plugins {
  id("spring-boot-module")
}

dependencies {
  api(project(":engine:domain:definition"))
  api(project(":engine:domain:acceptance"))
  implementation("org.springframework:spring-expression")
}
