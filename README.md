
# Pokémon App MVVM

Aplicación móvil para explorar Pokémon, sus detalles y estadísticas. Desarrollada para aprender y experimentar con nuevas tecnologías y buenas prácticas de desarrollo.

---

## 📜 Tabla de Contenidos

1. [Tecnologías Utilizadas](#-tecnologías-utilizadas)
2. [Arquitectura](#-arquitectura)
3. [Pantallas](#-pantallas)
4. [Funcionalidades Principales](#-funcionalidades-principales)
5. [Instalación y Configuración](#-instalación-y-configuración)
6. [Pruebas](#-pruebas)
7. [Contribuciones](#-contribuciones)
8. [Capturas de Pantalla](#-capturas-de-pantalla)

---

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **Frameworks y Librerías**:
  - [Retrofit](https://square.github.io/retrofit/) - Consumo de APIs REST.
  - [Room](https://developer.android.com/training/data-storage/room) - Persistencia de datos locales.
  - [Hilt](https://dagger.dev/hilt/) - Inyección de dependencias.
  - [Mockito](https://site.mockito.org/) y [MockK](https://mockk.io/) - Para pruebas unitarias.
  - [Gson](https://github.com/google/gson) - Manejo de JSON.
  - [WorkManager](https://developer.android.com/reference/androidx/work/WorkManager) - Para la ejecución de tareas en segundo plano.
  - [Lottie](https://airbnb.io/lottie/) - Para animaciones ligeras basadas en JSON.
  - [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Manejo de operaciones asincrónicas de forma sencilla.
  - [Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle) - Para manejar los ciclos de vida de los componentes.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Manejo del estado de la UI y ciclo de vida.
- **Diseño**:
  - Material Design para interfaz.
  - Diseño de pantallas con XML.
- **API**: [PokeAPI](https://pokeapi.co/) - Información de Pokémon.

---

## 🏛️ Arquitectura

La aplicación sigue una arquitectura **MVVM (Model-View-ViewModel)**:

1. **Data Layer**:
   - **Repository**: Fuente de datos que coordina la API remota y el almacenamiento local con Room.
   - **Data Source**: Maneja los servicios de red y las operaciones en base de datos.
2. **Domain Layer**:
   - Encapsula la lógica de negocio.
3. **UI Layer**:
   - **ViewModel**: Maneja el estado de la UI y permite separar la lógica de negocio del ciclo de vida de las actividades/fragments.
   - **XML Layouts**: Las pantallas y vistas están diseñadas con XML, siguiendo las pautas de Material Design.

Además, se utiliza **Lottie** para animaciones ligeras y atractivas, como el logo de carga y transiciones. **Coroutines** se emplea para manejar tareas asincrónicas de forma sencilla y eficiente, especialmente en la carga de datos en segundo plano.

---

## 📱 Pantallas

1. **Lista de Pokémon**  
   - Muestra una lista paginada de Pokémon con su imagen y nombre.
2. **Detalle de Pokémon**  
   - Información detallada: altura, peso, tipos, habilidades, y estadísticas.
3. **Carga en Segundo Plano**  
   - La aplicación permite la carga de Pokémon en segundo plano, con la opción de detener y reanudar la carga.
4. **Splash Screen**  
   - Pantalla inicial que muestra el logotipo de la app mientras se cargan los datos.

---

## ✨ Funcionalidades Principales

- **Explorar Pokémon**: Lista todos los Pokémon disponibles con su información básica.
- **Busqueda**: Busqueda segun nombre.
- **Ver Detalles**: Consulta información detallada de cada Pokémon.
- **Carga en Segundo Plano**: La aplicación permite que los datos de los Pokémon se carguen en segundo plano para no bloquear la interfaz de usuario. Además, ofrece la posibilidad de detener o reanudar la carga en cualquier momento.
- **Splash Screen**: Al iniciar la aplicación, se muestra una pantalla de inicio con el logotipo mientras se cargan los datos.
- **Diseño Material**: La interfaz sigue las pautas de Material Design, ofreciendo una experiencia fluida y coherente.

---

## 🖼️ Capturas de Pantalla
![sreenshot 1](https://github.com/user-attachments/assets/bbe637bb-09fc-44eb-a256-fffccfecc1bf)
---

## 🚀 Instalación y Configuración

1. **Versión de Android Studio**:
   - Realizada en Android Studio Koala Feature Drop | 2024.1.2
   - Emulador o dispositivo físico con Android 7.0+.

2. **Clonar el repositorio**:
   ```bash
   git clone [ https://github.com/usuario/pokemon-app.git](https://github.com/MauGf/MauGfPokeAppMvvM.git)
   cd pokemon-app
