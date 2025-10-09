plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

}

val ARCORE_LIBPATH = "${buildDir}/arcore-native"
val natives by configurations.creating

android {
    namespace = "com.example.warhammer40kscanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.warhammer40kscanner"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        sourceSets["main"].jniLibs.srcDir("src/main/jniLibs")

        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        externalNativeBuild {
            cmake {
                // ...
                // Nota: considera no añadir "/jni" aquí si ARCORE_LIBPATH en CMake lo va a añadir
                // o si tu tarea de extracción ya lo incluye.
                // Lo importante es la consistencia.
                // Opción 1 (ARCORE_LIBPATH en CMake es la raíz de 'arcore-native'):
                // arguments.add("-DARCORE_LIBPATH_FROM_GRADLE=${ARCORE_LIBPATH}")
                // Opción 2 (ARCORE_LIBPATH en CMake es la carpeta 'jni' directamente):
                cppFlags.add("-std=c++17")
                arguments.add("-DARCORE_JNI_DIR_FROM_GRADLE=${ARCORE_LIBPATH}/jni") // Esta es la que usabas
                arguments.add("-DARCORE_INCLUDE_DIR_FROM_GRADLE=${project.rootDir}/arcore/include")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true

        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt") // O la ruta a tu CMakeLists.txt
                version = "3.22.1" // O la versión que estés usando
            }
        }
    }

    sourceSets["main"].assets.srcDir("src/main/assets")
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    //implementation("com.google.ar:core:1.45.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //Dependencias para retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.google.ar:core:1.45.0") // Add ARCore support
    add("natives", "com.google.ar:core:1.45.0") // Enabling use of ARCore APIs in the App
}

// Extract the shared libraries from aars in the natives configuration.
// This is done so that NDK builds can access these libraries.
// Method added as part of enabling use of ARCore APIs in the App
tasks.register("extractNativeLibraries") {
    // Always extract, this insures the native libs are updated if the version changes.
    outputs.upToDateWhen { false } // Esto sigue igual

    doFirst {
        // Acceder a la configuración 'natives' (asegúrate de que está definida)
        // y luego a sus archivos.
        val nativesConfiguration = configurations.getByName("natives") // O simplemente usa la propiedad 'natives' si la definiste con 'val natives by ...'

        nativesConfiguration.files.forEach { file -> // Usar forEach en Kotlin
            // La acción de copia (copy) es una función a nivel de proyecto en Kotlin DSL
            // Necesitas accederla a través del objeto `project` o asegurar que estás en el contexto correcto.
            // Si estás directamente en el script de build (no dentro de otro closure que cambie `this`),
            // `copy` debería estar disponible.
            copy {
                from(zipTree(file)) // zipTree es una función del proyecto
                into(ARCORE_LIBPATH)
                include("jni/**/*")
            }
        }
    }
}

tasks.configureEach {
    // El nombre del parámetro de la lambda puede ser 'this' por defecto si solo hay uno,
    // o puedes nombrarlo explícitamente como 'task'.
    // Usaremos 'this' para ser más idiomático en Kotlin si es el único parámetro.
    // Si prefieres 'task', puedes escribir: tasks.configureEach { task -> ... task.name ... }

    if ((this.name.contains("external", ignoreCase = true) || this.name.contains("CMake", ignoreCase = true)) &&
        !this.name.contains("Clean", ignoreCase = true)) {
        // 'dependsOn' toma el nombre de la tarea o una instancia de la tarea.
        // Si 'extractNativeLibraries' es una tarea ya registrada, puedes pasar su nombre.
        this.dependsOn("extractNativeLibraries")
        // O si tienes una referencia a la tarea (p.ej., val extractNativeLibrariesTask = tasks.register(...)),
        // podrías usar: this.dependsOn(extractNativeLibrariesTask)
    }
}
