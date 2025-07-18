plugins {
  id("java-dependency")
}

dependencies {
  api(project(":engine:engine-domain"))
  api(project(":engine:engine-infrastructure"))
}
