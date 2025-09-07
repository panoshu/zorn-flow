plugins {
  id("java-dependency")
}

dependencies {
  api(project(":zorn-flow:domain:engine"))
  implementation(libs.ulid.creator)
}
