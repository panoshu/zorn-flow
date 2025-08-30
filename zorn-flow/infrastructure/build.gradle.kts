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
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("ch.qos.logback:logback-classic")
  implementation("com.fasterxml.jackson.core:jackson-databind")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
  implementation("jakarta.annotation:jakarta.annotation-api")

  implementation(libs.jooq)

  implementation(libs.mapstruct)
  annotationProcessor(libs.mapstruct.processor)
}
