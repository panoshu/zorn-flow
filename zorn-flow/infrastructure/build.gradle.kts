import org.gradle.api.internal.tasks.testing.report.HtmlTestReport.generator
import org.gradle.declarative.dsl.schema.FqName.Empty.packageName
import org.gradle.internal.impldep.org.apache.ivy.util.Message.deprecated
import org.jooq.meta.Databases.database
import org.jooq.tools.jdbc.JDBCUtils.driver
import org.springframework.boot.buildpack.platform.docker.configuration.DockerRegistryAuthentication.configuration

plugins {
  id("spring-boot-module")
  id("org.jooq.jooq-codegen-gradle") version "3.20.6"
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

  runtimeOnly("org.postgresql:postgresql")
  jooqCodegen("org.postgresql:postgresql:42.7.7")

  implementation(libs.jooq)
  implementation(libs.jooq.meta)
  implementation(libs.mapstruct)
  annotationProcessor(libs.mapstruct.processor)
}

jooq {
  configuration {
    jdbc {
      url = "jdbc:postgresql://127.0.0.1:15432/zornflow"
      user = "engine_user"
      password = "engine_user"
      driver = "org.postgresql.Driver"
    }
    generator {
      name = "org.jooq.codegen.JavaGenerator"
      database {
        name = "org.jooq.meta.postgres.PostgresDatabase"
        inputSchema = "engine"
      }
      generate {
        relations = true
        deprecated = false
        records = true
        immutablePojos = true
        fluentSetters = true
      }
      target {
        packageName = "com.zornflow.infrastructure.config.source.database.jooq"
        directory = "src/main/java"
      }
    }
  }
}
