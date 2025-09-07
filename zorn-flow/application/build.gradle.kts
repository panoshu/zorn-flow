plugins {
  id("spring-boot-module")
}

dependencies {
  implementation(project(":zorn-flow:domain:engine"))
  implementation(project(":zorn-flow:infrastructure"))
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework:spring-tx")
  implementation("org.springframework:spring-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
}
