# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)

project(warhammerengine) # Puedes cambiar "VuforiaSample" por el nombre de tu proyecto si lo deseas

# --- CONFIGURACIÓN DE VUFORIA ---
# Directorio donde se encuentra este archivo CMakeLists.txt
# Asumimos que está en app/src/main/cpp/
set(CMAKE_CURRENT_PROJECT_DIR ${CMAKE_CURRENT_LIST_DIR})

# 1. Directorio de Inclusión para las cabeceras de Vuforia (.h files)
#    Si sigues usando los archivos .h de la instalación del SDK de Vuforia:
include_directories("C:/Program Files/Android/Android Studio/vuforia-sdk-android-11-2-4/build/include")
#    Si has copiado los includes a tu proyecto (ej. app/src/main/include/VuforiaEngine),
#    cambiarías la línea de arriba por algo como:
#    include_directories("${CMAKE_CURRENT_PROJECT_DIR}/../include") # Ajusta la ruta relativa

# 2. Localizar la librería precompilada de Vuforia (.so files)
#    Asumimos que están en app/src/main/jniLibs/${ANDROID_ABI}/libVuforiaEngine.so
add_library(VUFORIA_LIBRARY SHARED IMPORTED)
set_property(TARGET VUFORIA_LIBRARY PROPERTY IMPORTED_LOCATION
        "${CMAKE_CURRENT_PROJECT_DIR}/../jniLibs/${ANDROID_ABI}/libVuforiaEngine.so")
# --- FIN CONFIGURACIÓN DE VUFORIA ---
set(ARCORE_EFFECTIVE_JNI_DIR ${ARCORE_JNI_DIR_FROM_GRADLE})
message(STATUS "CMake: ARCORE_EFFECTIVE_JNI_DIR = ${ARCORE_EFFECTIVE_JNI_DIR}") #Para depurar
message(STATUS "CMake: ARCORE_INCLUDE_DIR_FROM_GRADLE = ${ARCORE_INCLUDE_DIR_FROM_GRADLE}") #Para depurar
add_library(ARCORE_LIBRARY SHARED IMPORTED)
set_target_properties(ARCORE_LIBRARY PROPERTIES
        IMPORTED_LOCATION "${ARCORE_EFFECTIVE_JNI_DIR}/${ANDROID_ABI}/libarcore_sdk_c.so"
        INTERFACE_INCLUDE_DIRECTORIES ${ARCORE_INCLUDE_DIR_FROM_GRADLE} # Usa la variable de include pasada
)
# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
find_library(ANDROID_LIBRARY android)
find_library(GLES3_LIBRARY GLESv3) # Asegúrate que GLESv3 se encuentre o proporciona una ruta
find_library(LOG_LIBRARY log)

# Configure building the app library
add_library(warhammerengine SHARED
        # Cross platform source
        CrossPlatform/AppController.cpp
        CrossPlatform/tiny_obj_loader.cpp

        # Android native sources
        GLESRenderer.cpp
        GLESUtils.cpp
        VuforiaWrapper.cpp
)

# Especifica los directorios de inclusión para TU librería 'warhammerengine'
target_include_directories(warhammerengine PUBLIC
        # Ruta a tus archivos CrossPlatform, relativa a este CMakeLists.txt
        # Si AppController.cpp está en app/src/main/cpp/CrossPlatform/
        "${CMAKE_CURRENT_PROJECT_DIR}/CrossPlatform"
        # Si CrossPlatform está un nivel arriba de cpp (ej. app/src/main/CrossPlatform)
        # "${CMAKE_CURRENT_PROJECT_DIR}/../CrossPlatform"

        # Incluye las cabeceras de Vuforia (ya gestionado por include_directories globalmente,
        # pero puede ser bueno ser explícito si solo warhammerengine las necesita)
        # Si usas el include_directories global, esta línea podría no ser estrictamente necesaria aquí
        # O podrías usar la variable del SDK si la definieras:
        # "C:/Program Files/Android/Android Studio/vuforia-sdk-android-11-2-4/build/include"

        # Incluye las cabeceras de GLES3 si find_library(GLES3_LIBRARY GLESv3)
        # no define automáticamente GLES3_INCLUDE_DIR o si necesitas ser explícito.
        # Por lo general, find_library para GLESv3 lo maneja.
        # ${GLES3_INCLUDE_DIR} # Descomenta si es necesario y si GLES3_INCLUDE_DIR está definido
)

# Specify libraries CMake should link to your target library
target_link_libraries(warhammerengine
        ${ANDROID_LIBRARY}
        ${LOG_LIBRARY}
        ${GLES3_LIBRARY}
        ARCORE_LIBRARY # Enabling use of ARCore APIs in the App
        VUFORIA_LIBRARY # Enlaza con la librería de Vuforia que definiste arriba
)