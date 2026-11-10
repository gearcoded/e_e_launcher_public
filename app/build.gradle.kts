plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseKeystorePath =
    providers.gradleProperty("EE_KEYSTORE_FILE").orNull

val releaseKeystoreFile =
    releaseKeystorePath?.let(::file)

val releaseKeyAlias = "collegium_desideriorum"
val releaseStorePassword = providers.gradleProperty("EE_KEYSTORE_PASSWORD")
    .orElse(providers.environmentVariable("EE_KEYSTORE_PASSWORD"))
    .orNull
val releaseKeyPassword = providers.gradleProperty("EE_KEY_PASSWORD")
    .orElse(providers.environmentVariable("EE_KEY_PASSWORD"))
    .orNull
val releaseSigningAvailable =
    releaseKeystoreFile?.isFile == true &&
        releaseStorePassword != null &&
        releaseKeyPassword != null


android {
    namespace = "org.collegiumdesideriorum.e_e_launcher"
    compileSdk = 35

    val releaseSigningConfig = if (releaseSigningAvailable) {
        signingConfigs.create("release") {
            storeFile = releaseKeystoreFile
            storePassword = releaseStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
        }
    } else null

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "org.collegiumdesideriorum.e_e_launcher"
        minSdk = 34
        targetSdk = 35
        versionCode = 184
        versionName = "0.50.0"
    }

    buildTypes {
        debug {
            // Generate accented and bidirectional pseudo-locales for layout testing.
            isPseudoLocalesEnabled = true
            // Keep the installed development build compatible with the release
            // signature when local credentials are available. Otherwise use the
            // normal Android debug keystore; dependency/report tasks still work.
            if (releaseSigningConfig != null) signingConfig = releaseSigningConfig
        }
        release {
            // A clean public/F-Droid checkout can be configured without private
            // credentials. In that case the release APK remains unsigned and may
            // be signed later by the distribution pipeline.
            if (releaseSigningConfig != null) signingConfig = releaseSigningConfig
            isMinifyEnabled = false
            isDebuggable = false
          proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    androidResources {
        // Android 13+ exposes the available release locales in system app-language settings.
        generateLocaleConfig = true
    }

    packaging {
        jniLibs {
            // androidx.graphics.path is already shipped unstripped by its producer.
            // Declare that explicitly so AGP does not try (and fail) to strip it.
            keepDebugSymbols += "**/libandroidx.graphics.path.so"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Keep Kotlin bytecode compatible with the Java compilation target.
    // This does not require Gradle itself to run on JDK 17; JDK 21 can run Gradle
    // while both Java and Kotlin still emit JVM 17 bytecode.
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // Do not force a separate Java 17 toolchain here.
    // Android Studio's selected Gradle JDK will be used, which avoids
    // "Cannot find a Java installation matching languageVersion=17"
    // on machines that only have the bundled JBR/JDK installed.
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    implementation(platform("androidx.compose:compose-bom:2025.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
