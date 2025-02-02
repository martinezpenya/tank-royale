import org.jsonschema2pojo.AnnotationStyle
import org.jsonschema2pojo.SourceType
import org.jsonschema2pojo.gradle.JsonSchemaExtension
import java.util.Collections.singletonList

description = "Robocode Tank Royale schema for Java"

val title = "Robocode Tank Royale schema for Java"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

val artifactBaseName = "robocode-tankroyale-schema"
val archiveFileName = "${layout.buildDirectory.get()}/libs/$artifactBaseName-$version.jar"

val schemaPackage = "dev.robocode.tankroyale.schema"

plugins {
    java
    alias(libs.plugins.jsonschema2pojo)
}

dependencies {
    implementation(libs.gson)
    implementation(libs.java.websocket)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}

// https://github.com/joelittlejohn/jsonschema2pojo/tree/master/jsonschema2pojo-gradle-plugin
jsonSchema2Pojo {
    setSourceType(SourceType.YAMLSCHEMA.toString())
    setSource(singletonList(File("$projectDir/../schemas")))
    setAnnotationStyle(AnnotationStyle.GSON.toString())
    targetPackage = schemaPackage
}