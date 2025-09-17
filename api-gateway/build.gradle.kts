plugins {
  id("spring-boot-module")
}

dependencies {
  implementation("org.springframework.cloud:spring-cloud-starter-gateway")
  implementation("org.springframework:spring-web")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
  implementation("org.springframework.cloud:spring-cloud-starter-vault-config")

  implementation("ch.qos.logback:logback-classic")
  implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")

  compileOnly("org.springframework.boot:spring-boot-autoconfigure")

  testImplementation("io.projectreactor:reactor-test")
}

// tasks.withType<JavaCompile> {
  // options.compilerArgs.add("-parameters")
// }
