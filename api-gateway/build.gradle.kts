plugins {
  id("spring-boot-module")
}

dependencies {
  implementation("org.springframework.cloud:spring-cloud-starter-gateway")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

  implementation("ch.qos.logback:logback-classic")

  compileOnly("org.springframework.boot:spring-boot-autoconfigure")
}
