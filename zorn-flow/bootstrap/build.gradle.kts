plugins {
  id("spring-boot-starter")
}

dependencies {
  api(project(":zorn-flow:interface"))
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-jooq")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
