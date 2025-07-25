plugins {
  id("java-dependency")
}

dependencies {
  api(project(":engine:type"))
  api(project(":engine:shared"))
}
