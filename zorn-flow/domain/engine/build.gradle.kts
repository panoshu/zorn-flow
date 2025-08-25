plugins {
  id("java-dependency")
}

dependencies {
  api(project(":zorn-flow:domain:common"))
  implementation(libs.ulid.creator)
}
