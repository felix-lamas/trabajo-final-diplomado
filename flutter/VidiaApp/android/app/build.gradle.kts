import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dev.flutter.flutter-gradle-plugin")
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("key.properties")
if (keystorePropertiesFile.exists()) {
    FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
}

fun signingValue(name: String, environmentName: String): String? =
    System.getenv(environmentName)?.takeIf { it.isNotBlank() }
        ?: keystoreProperties.getProperty(name)?.takeIf {
            it.isNotBlank() && it != "CHANGE_ME"
        }

val releaseSigningValues = mapOf(
    "storeFile" to signingValue("storeFile", "VIDIA_RELEASE_STORE_FILE"),
    "storePassword" to signingValue("storePassword", "VIDIA_RELEASE_STORE_PASSWORD"),
    "keyAlias" to signingValue("keyAlias", "VIDIA_RELEASE_KEY_ALIAS"),
    "keyPassword" to signingValue("keyPassword", "VIDIA_RELEASE_KEY_PASSWORD"),
)

// Validate only release tasks so debug builds remain independent of release secrets.
gradle.taskGraph.whenReady {
    if (allTasks.any { it.project == project && it.name.contains("Release") }) {
        check(keystorePropertiesFile.exists()) {
            "Falta android/key.properties: configura la firma de produccion antes de construir release."
        }
        val missing = releaseSigningValues.filterValues { it == null }.keys
        check(missing.isEmpty()) {
            "Firma release incompleta. Configura: ${missing.joinToString()}. Las contrasenas pueden proporcionarse mediante variables VIDIA_RELEASE_*."
        }
        check(file(releaseSigningValues.getValue("storeFile")!!).isFile) {
            "No se encontro el keystore de produccion configurado para release."
        }
    }
}

android {
    namespace = "bo.edu.uajms.vidia"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "bo.edu.uajms.vidia"
        minSdk = 24
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    signingConfigs {
        create("release") {
            keyAlias = releaseSigningValues["keyAlias"]
            keyPassword = releaseSigningValues["keyPassword"]
            storeFile = releaseSigningValues["storeFile"]?.let { file(it) }
            storePassword = releaseSigningValues["storePassword"]
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

kotlin {
    compilerOptions { jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17 }
}

flutter { source = "../.." }
