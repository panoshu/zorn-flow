plugins {
  id("java-dependency")
}

dependencies {
  api(project(":zorn-flow:domain:contract"))
  implementation(libs.ulid.creator)
}
