import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

plugins {
    id("java-library")
    id("maven-publish")
}

group = "de.varoplugin"
version = "4.12.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
    withJavadocJar()
}

tasks.compileJava { options.encoding = "UTF-8" }
tasks.javadoc { options.encoding = "UTF-8" }

sourceSets {
    main {
        java {
            srcDir("src")
        }
        resources {
            srcDir("resources")
        }
    }

    test {
        java {
            srcDir("test")
        }
    }
}

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.varoplugin.de/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://m2.dv8tion.net/releases")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

val internal: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
    isTransitive = false
}

val runtimeDownload: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

fun DependencyHandler.modularInternal(dependencyNotation: Any, localFileName: String) : Dependency? {
    val file = file("${rootDir}/libs/${localFileName}.jar")
    return if (file.exists())
        this.add("internal", files(file))
    else
        this.add("internal", dependencyNotation)
}

dependencies {
    modularInternal("de.varoplugin:CFW:0.6.18", "CFW")

    implementation("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:6.1.3-SNAPSHOT")
    compileOnly("com.github.labymod:legacy-labymod-server-api:1.0")
    compileOnly("me.clip:placeholderapi:2.10.10")

    runtimeDownload("com.google.code.gson:gson:2.10.1")
    runtimeDownload("net.dv8tion:JDA:4.4.0_352") {
        exclude(module = "opus-java")
    }
    runtimeDownload("com.github.pengrad:java-telegram-bot-api:5.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

val createDependenciesFile = tasks.register("createDependenciesFile") {
    mustRunAfter(tasks.getByName("compileJava"))
    doLast {
        val dependenciesFile = file("${buildDir}/dependencies.txt")
        val writer = dependenciesFile.bufferedWriter(charset = StandardCharsets.UTF_8)
        runtimeDownload.resolvedConfiguration.firstLevelModuleDependencies.forEach {resolvedDependency ->
            resolvedDependency.allModuleArtifacts.forEach {
                writer.write("${resolvedDependency.moduleName}:${it.moduleVersion}:${it.file.sha512().base64()}")
                writer.newLine()
            }
        }
        writer.close()
    }
}

tasks.jar {
    if (project.hasProperty("destinationDir"))
        destinationDirectory.set(file(project.property("destinationDir").toString()))
    if (project.hasProperty("fileName"))
        archiveFileName.set(project.property("fileName").toString())

    dependsOn(createDependenciesFile)

    manifest {
        attributes(Pair("Manifest-Version", "1.0"), Pair("Class-Path", "."), Pair("Main-Class", "de.varoplugin.varo.RunnableLauncher"))
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(internal.map { if (it.isDirectory) it else zipTree(it) })

    from(file("${buildDir}/dependencies.txt"))
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
        outputs.upToDateWhen { false }
    }
}

tasks.processResources {
    outputs.upToDateWhen { false }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(sourceSets.main.get().resources.srcDirs) {
        include("**/plugin.yml")
        expand("name" to project.name, "version" to project.version)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("VaroPlugin")
                url.set("https://varoplugin.de")
            }
        }
    }

    repositories {
        maven {
            setUrl("https://repo.varoplugin.de/repository/maven-releases/")
            credentials {
                username = project.findProperty("repouser") as? String
                password = project.findProperty("repopassword") as? String
            }
        }
    }
}

val mdSha512: MessageDigest = MessageDigest.getInstance("SHA-512")
fun File.sha512() : ByteArray = mdSha512.digest(this.readBytes())
fun ByteArray.base64() : String = Base64.getEncoder().encodeToString(this)