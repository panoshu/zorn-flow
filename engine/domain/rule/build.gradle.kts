plugins {
  id("java-dependency")
}

dependencies {
  api(project(":engine:domain"))
  api(project(":engine:shared"))
}
