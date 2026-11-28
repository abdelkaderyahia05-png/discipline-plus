import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Clé d'envoi Google Play : les mots de passe et le chemin du keystore sont dans android/keystore.properties,
// fichier ignoré par git. Le keystore lui-même est rangé hors du dépôt. Sans ce fichier, seul le build debug marche.
val clesEnvoi = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "app.disciplineplus"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.disciplineplus"
        minSdk = 26
        targetSdk = 36 // exigence Google Play : Android 16 (API 36) pour les nouvelles applis depuis le 31/08/2026
        versionCode = 1 // à augmenter de 1 à chaque envoi sur le Play Store
        versionName = "1.0"
    }

    signingConfigs {
        if (clesEnvoi.getProperty("storeFile") != null) {
            create("envoi") {
                storeFile = file(clesEnvoi.getProperty("storeFile"))
                storePassword = clesEnvoi.getProperty("storePassword")
                keyAlias = clesEnvoi.getProperty("keyAlias")
                keyPassword = clesEnvoi.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("envoi")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

// Aucune dépendance : uniquement le SDK Android (WebView, service d'accessibilité).
