import kotlinx.kover.api.KoverTaskExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION") plugins {
  application
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.kotest.multiplatform)
  alias(libs.plugins.kover)
  alias(libs.plugins.spring)
  alias(libs.plugins.dependency.management)
  id(libs.plugins.detekt.pluginId)
  id(libs.plugins.kotlin.jvm.pluginId)
}

buildscript {
  dependencies {
    classpath("org.apache.commons:commons-compress:1.22")
  }
}

val main by extra("alerts.MainKt")

application {
  mainClass by main
}


allprojects {
  setupDetekt()
}

repositories {
  mavenCentral()
  maven(url = "https://packages.confluent.io/maven/")
  // For Kotest Extensions Arrow Fx, remove if 1.1.3 is released
  maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
  maven(url = "https://jitpack.io")
}


java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().all {
  kotlinOptions.freeCompilerArgs += listOf(
    "-Xopt-in=kotlin.RequiresOptIn",
    "-Xopt-in=kotlin.OptIn",
    "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
    "-Xopt-in=kotlinx.coroutines.ObsoleteCoroutinesApi",
    "-Xopt-in=kotlinx.coroutines.FlowPreview"
  )
}

tasks {
  withType<KotlinCompile>().configureEach {
    kotlinOptions {
      jvmTarget = "${JavaVersion.VERSION_17}"
      freeCompilerArgs = freeCompilerArgs + listOf(
        "-Xcontext-receivers",
        "-opt-in=kotlinx.coroutines.FlowPreview"
      )
    }
  }
  
  test {
    useJUnitPlatform()
    extensions.configure(KoverTaskExtension::class) {
      includes.add("alerts.*")
    }
  }
}

dependencies {
  implementation(libs.bundles.spring)
  implementation(libs.bundles.arrow)
  implementation(libs.reactor.kafka)
  implementation(libs.reactor.kotlin.extensions)
  implementation(libs.logback.classic)
  implementation(libs.postgresql)
  implementation(libs.r2dbc.postgres)
  implementation(libs.flyway)
  implementation(libs.klogging)
  implementation(libs.avro4k)
  implementation(libs.kafka.schema.registry)
  implementation(libs.kafka.avro.serializer)
  implementation(libs.avro)
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.serialization.jsonpath)
  implementation(libs.kotlinx.coroutines.reactor)
  implementation(libs.micrometer.prometheus)
  implementation(libs.kotlinx.datetime)

  // mac users
  runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.89.Final:osx-aarch_64")

  annotationProcessor(libs.spring.configuration.processor)
  testImplementation(libs.bundles.kotest)
  testImplementation(libs.spring.test)
}
