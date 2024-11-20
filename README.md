# MauGfPokeAppMvvM
Se creara una app en Kotlin con arquitectura MVVM que consuma la PokeAPI y cumpla con los requisitos mencionados.

 Pokémon App
Una aplicación móvil para explorar Pokémon, sus detalles y estadísticas. Desarrollada para aprender y experimentar con nuevas tecnologías y buenas prácticas de desarrollo.

📜 Tabla de Contenidos
Tecnologías Utilizadas
Arquitectura
Pantallas
Funcionalidades Principales
Instalación y Configuración
Pruebas
Contribuciones
🛠️ Tecnologías Utilizadas
Lenguaje: Kotlin
Frameworks y Librerías:
Retrofit - Consumo de APIs REST.
Room - Persistencia de datos locales.
Hilt - Inyección de dependencias.
Mockito y MockK - Para pruebas unitarias.
Gson - Manejo de JSON.
WorkManager - Para la ejecución de tareas en segundo plano.
Diseño:
Material Design para interfaz.
Diseño de pantallas con XML.
API: PokeAPI - Información de Pokémon.
🏛️ Arquitectura
La aplicación sigue una arquitectura MVVM (Model-View-ViewModel), combinada con componentes de Android Jetpack:

Data Layer:
Repository: Fuente de datos que coordina la API remota y el almacenamiento local con Room.
Data Source: Maneja los servicios de red y las operaciones en base de datos.
Domain Layer:
Encapsula la lógica de negocio.
UI Layer:
ViewModel: Maneja el estado de la UI.
XML Layouts: Las pantallas y vistas están diseñadas con XML, siguiendo las pautas de Material Design.
📱 Pantallas
Lista de Pokémon
Muestra una lista paginada de Pokémon con su imagen y nombre.
Detalle de Pokémon
Información detallada: altura, peso, tipos, habilidades, y estadísticas.
Carga en Segundo Plano
La aplicación permite la carga de Pokémon en segundo plano, con la opción de detener y reanudar la carga.
Splash Screen
Pantalla inicial que muestra el logotipo de la app mientras se cargan los datos.
✨ Funcionalidades Principales
Explorar Pokémon: Lista todos los Pokémon disponibles con su información básica.
Ver Detalles: Consulta información detallada de cada Pokémon.
Carga en Segundo Plano: La aplicación permite que los datos de los Pokémon se carguen en segundo plano para no bloquear la interfaz de usuario. Además, ofrece la posibilidad de detener o reanudar la carga en cualquier momento.
Splash Screen: Al iniciar la aplicación, se muestra una pantalla de inicio con el logotipo mientras se cargan los datos.
Diseño Material: La interfaz sigue las pautas de Material Design, ofreciendo una experiencia fluida y coherente.

