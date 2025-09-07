plugins {
  id("spring-boot-module")
}

dependencies {
  implementation(project(":zorn-flow:api"))
  implementation(project(":zorn-flow:application"))
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
}
