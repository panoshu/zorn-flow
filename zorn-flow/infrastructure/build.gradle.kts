plugins {
  id("spring-boot-module")
}

dependencies {
  implementation(project(":zorn-flow:domain:engine"))

  implementation("org.springframework:spring-context")
  implementation("org.springframework:spring-expression")
  implementation("org.springframework.boot:spring-boot")
  implementation("org.springframework:spring-tx")
  implementation("org.springframework:spring-jdbc")
  implementation("org.springframework.boot:spring-boot-autoconfigure")

  implementation("ch.qos.logback:logback-classic")
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

  implementation("org.postgresql:postgresql")

  implementation(libs.jooq)
  implementation(libs.mapstruct)
  annotationProcessor(libs.mapstruct.processor)
}
