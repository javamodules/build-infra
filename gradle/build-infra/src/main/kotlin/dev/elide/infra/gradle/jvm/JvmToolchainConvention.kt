package dev.elide.infra.gradle.jvm

import dev.elide.infra.gradle.BuildConstants
import dev.elide.infra.gradle.api.ElideBuild
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmImplementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Applies JVM toolchain settings.
internal class JvmToolchainConvention : Plugin<Project> {
  override fun apply(target: Project) {
    target.extensions.getByType(ElideBuild.ElideBuildDsl::class.java).jvm.let {
      // apply toolchain settings
      val java = target.extensions.getByType(JavaPluginExtension::class.java)
      val toolchain = it.toolchain.get()
      val toolchainOverride = target.findProperty(BuildConstants.Properties.JVM_TOOLCHAIN)?.let { level ->
        JavaVersion.toVersion(level)
      }

      val jvmTarget = it.target.get()
      val targetOverride = (target.findProperty(BuildConstants.Properties.JVM_TARGET) as? String)?.let { level ->
        JvmTarget.fromTarget(level)
      }

      // allow override by property
      val resolvedToolchainVersion = toolchainOverride ?: toolchain
      val resolvedBytecodeTarget = targetOverride ?: jvmTarget

      java.toolchain {
        vendor.set(it.vendor)
        languageVersion.set(JavaLanguageVersion.of(resolvedToolchainVersion.majorVersion))
        if (it.implementation.get() != JvmImplementation.VENDOR_SPECIFIC) {
          implementation.set(it.implementation)
        }
      }

      // basic configuration for java compiler
      target.tasks.withType(JavaCompile::class.java).configureEach {
        targetCompatibility = resolvedBytecodeTarget.target
        sourceCompatibility = resolvedBytecodeTarget.target
        modularity.inferModulePath.set(true)
      }
    }
  }
}
