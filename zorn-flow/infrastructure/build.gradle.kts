plugins {
  id("spring-boot-module")
}

dependencies {
  api(project(":zorn-flow:domain:engine"))

  implementation("org.springframework:spring-context")
  implementation("org.springframework:spring-expression")
  implementation("org.springframework.boot:spring-boot")

  implementation("ch.qos.logback:logback-classic")
  implementation("org.yaml:snakeyaml")
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  // implementation("jakarta.annotation:jakarta.annotation-api")
}
