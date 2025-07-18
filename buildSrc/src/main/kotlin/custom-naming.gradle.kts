import org.gradle.api.tasks.bundling.Jar
import org.springframework.boot.gradle.tasks.bundling.BootJar

project.afterEvaluate {
  var modulePath = project.path.removePrefix(":").replace(":", "-").removePrefix("common-")

  if (project.plugins.hasPlugin("org.springframework.boot")) {
    modulePath = modulePath.removeSuffix("-starter")

    tasks.withType<BootJar> {
      val targetJarFileName = "${modulePath}-${project.version}.jar" // 添加 -boot-
      archiveFileName.set(targetJarFileName)
    }
  } else {
    tasks.withType<Jar> {
      val targetJarFileName = "${modulePath}-${project.version}.jar"
      archiveFileName.set(targetJarFileName)
    }
  }
}
