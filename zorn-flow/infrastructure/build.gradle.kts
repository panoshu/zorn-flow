plugins {
  id("spring-boot-module")
}

dependencies {
  api(project(":zorn-flow:domain:engine"))

  implementation("org.springframework:spring-context")
  implementation("org.springframework:spring-expression")

  implementation("ch.qos.logback:logback-classic")
  implementation("jakarta.annotation:jakarta.annotation-api")
}
