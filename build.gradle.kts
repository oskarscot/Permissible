import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    java
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
}

val hytaleHome: String = "${System.getProperty("user.home")}/.var/app/com.hypixel.HytaleLauncher/data/Hytale"

val java_version: String by project
val patchline: String by project
val includes_pack: String by project
val load_user_mods: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(java_version))
    withSourcesJar()
    withJavadocJar()
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:-missing", "-quiet")
}

repositories {
    maven("https://repo.okaeri.cloud/releases")
    mavenCentral()
}

dependencies {
    implementation(files("$hytaleHome/install/$patchline/package/game/latest/Server/HytaleServer.jar"))

    val okaeriConfigs = "6.0.0-beta.27"
    implementation("eu.okaeri:okaeri-configs-yaml-snakeyaml:$okaeriConfigs")
    implementation("eu.okaeri:okaeri-configs-serdes-commons:$okaeriConfigs")
    implementation("eu.okaeri:okaeri-configs-validator-okaeri:$okaeriConfigs")
    implementation("scot.oskar:volt-core:0.0.4")
    implementation("org.postgresql:postgresql:42.7.8")
}

val serverRunDir = file("$projectDir/run")
if (!serverRunDir.exists()) {
    serverRunDir.mkdirs()
}

tasks.register("updatePluginManifest") {
    val manifestFile = file("src/main/resources/manifest.json")
    doLast {
        if (!manifestFile.exists()) {
            throw GradleException("Could not find manifest.json at ${manifestFile.path}!")
        }
        @Suppress("UNCHECKED_CAST")
        val manifestJson = groovy.json.JsonSlurper().parseText(manifestFile.readText()) as MutableMap<String, Any>
        manifestJson["Version"] = version
        manifestJson["IncludesAssetPack"] = includes_pack.toBoolean()
        manifestFile.writeText(groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(manifestJson)))
    }
}

tasks.named("processResources") {
    dependsOn("updatePluginManifest")
}

idea {
    project {
        settings {
            runConfigurations {
                create<Application>("HytaleServer") {
                    mainClass = "com.hypixel.hytale.Main"
                    moduleName = "${project.idea.module.name}.main"
                    var params = "--allow-op --assets=$hytaleHome/install/$patchline/package/game/latest/Assets.zip"
                    if (includes_pack.toBoolean()) {
                        params += " --mods=${sourceSets.main.get().java.srcDirs.first().parentFile.absolutePath}"
                    }
                    if (load_user_mods.toBoolean()) {
                        params += " --mods=$hytaleHome/UserData/Mods"
                    }
                    programParameters = params
                    workingDirectory = serverRunDir.absolutePath
                }
            }
        }
    }
}