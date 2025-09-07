object Versions {
  // 建议升级到最新稳定版
  const val kotlin = "2.2.0"
  // 对应版本的 Android Gradle Plugin 请查看官方兼容性说明
  const val androidGradlePlugin = "8.4.0"
  // 协程库也建议升级到最新稳定版
  const val coroutines = "1.8.0"
}

object Libs {
  const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
  const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
}
