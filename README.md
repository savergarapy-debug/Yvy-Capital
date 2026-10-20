# Yvy Capital — Plataforma Inmobiliaria para Paraguay (Android Nativo)

## 1. Arquitectura y Selección Tecnológica

- **Tecnología Principal:** **Kotlin + Jetpack Compose** (Android Nativo, Material Design 3, Coroutines & StateFlow, arquitectura MVVM + Repository).
- **Identificador de Aplicación (`applicationId`):** `com.aistudio.yvycapital.pykxmp` (configurado en `app/build.gradle.kts`, manteniendo el `namespace = "com.example"` para compatibilidad con recursos generados).
- **Backend y Base de Datos:** **Firebase Firestore Enterprise + Firebase Authentication** con **Google Sign-In mediante Jetpack Credential Manager (`GetSignInWithGoogleOption`)**.
- **Por qué se eligió Firebase sobre Supabase en este entorno:** El entorno de Google AI Studio cuenta con aprovisionamiento nativo directo de proyectos Firebase, generación automática de `google-services.json`, registro automático de certificados SHA-1/SHA-256 para Google Sign-In, emuladores locales para validación de reglas de seguridad y despliegue directo de reglas (`firestore.rules`).

---

## 2. Estructura del Proyecto

- `/app/src/main/java/com/example/MainActivity.kt` — Punto de entrada, barra de estado Edge-to-Edge, compuerta de autenticación reactiva (`AppNavigation`) y navegación adaptativa (`NavigationBar` en teléfonos / `NavigationRail` en tablets y plegables).
- `/app/src/main/java/com/example/data/model/YvyModels.kt` — Modelos de datos tipados con `@DocumentId` y `@ServerTimestamp` (`UserProfile`, `Property`, `FavoriteItem`, `PropertyInquiry`, `PropertyReport`, `PropertyFilter`).
- `/app/src/main/java/com/example/data/repository/YvyRepository.kt` — Capa de datos conectada al ID de base de datos aprovisionado (`R.string.firestore_database_id`), flujos en tiempo real (`snapshots()`), consultas alineadas con reglas de seguridad y carga inicial de propiedades de prueba en Paraguay identificadas con `[DEMO]`.
- `/app/src/main/java/com/example/data/repository/FirestoreErrorHandler.kt` — Manejador centralizado de errores estructurados en JSON (`handleFirestoreError`).
- `/app/src/main/java/com/example/ui/auth/AuthScreen.kt` — Pantalla de bienvenida e inicio de sesión con Google (`CredentialManager`).
- `/app/src/main/java/com/example/ui/screens/HomeScreen.kt` — Portada principal con selector instantáneo de moneda (`PYG` / `USD`), buscador rápido, categorías, carrusel de propiedades destacadas y acceso a Yvy Capital Pro.
- `/app/src/main/java/com/example/ui/screens/ExploreScreen.kt` — Buscador multicriterio por departamento de Paraguay, ciudad, operación, tipo de inmueble, dormitorios, cocheras, vista en lista/cuadrícula y paginación incremental.
- `/app/src/main/java/com/example/ui/screens/PropertyDetailScreen.kt` — Ficha completa de propiedad con galería de vistas, precios en Guaraníes y Dólares, envío de consultas y solicitudes de visita, contacto directo por WhatsApp, apertura de ubicación en Google Maps, botón de compartir y reporte de anuncios.
- `/app/src/main/java/com/example/ui/screens/FavoritesScreen.kt` — Propiedades favoritas sincronizadas en la nube y seguimiento en tiempo real de solicitudes de visita enviadas.
- `/app/src/main/java/com/example/ui/screens/ProfileAndProScreen.kt` — Gestión de perfil del cliente, preferencias de moneda, política de privacidad, eliminación de cuenta y espacio **Yvy Capital Pro** para asesores (publicación de propiedades, gestión de anuncios propios y seguimiento de leads/visitas).
- `/firebase-blueprint.json` — Esquema declarativo de entidades y colecciones de Firestore.
- `/firestore.rules` — Reglas de seguridad Zero-Trust verificadas con pruebas unitarias en JavaScript (`firestore.test.js`) y pruebas de integración Android Robolectric (`YvyRepositoryRuleTest.kt`).

---

## 3. Configuración del Backend y Variables de Entorno

1. **Firebase Configurado Automáticamente:**
   - El archivo `app/google-services.json` y `app/src/main/res/values/firebase_applet_config.xml` ya están aprovisionados y vinculados al proyecto.
   - Las reglas de seguridad (`firestore.rules`) ya fueron verificadas en emuladores y desplegadas en el backend.
2. **Variables de Entorno (`.env.example`):**
   - Nunca almacenes claves privadas ni credenciales administrativas dentro del código fuente.
   - Si en el futuro agregas claves de servicios externos, configúralas desde el panel **Secrets** de AI Studio y decláralas sin valores reales en `.env.example`.

---

## 4. Cómo Ejecutar y Probar la Aplicación

### En el Emulador Integrado de AI Studio o Teléfono Android
1. En el panel de vista previa de AI Studio (o instalando el APK en tu teléfono Android), abre **Yvy Capital**.
2. En la pantalla de bienvenida, pulsa **"Continuar con Google"** para autenticarte de forma segura.
3. Al ingresar por primera vez, si el catálogo está vacío, se inicializan automáticamente 6 propiedades demostrativas de Paraguay claramente identificadas con `[DEMO]` (Asunción - Villa Morra, Las Lomas, San Bernardino, Luque, Ciudad del Este y Encarnación).
4. Prueba el cambio de moneda **PYG (Gs.) / USD ($)** en la esquina superior derecha de Inicio, los filtros por departamento en **Explorar**, el guardado en **Favoritos**, el agendamiento de visitas en la ficha de propiedad y la publicación de inmuebles desde **Perfil -> Yvy Capital Pro**.

### Ejecutar Pruebas Automatizadas
- **Pruebas de Reglas en JavaScript (~1s):**
  ```bash
  FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
  ```
- **Pruebas de Integración Android + Robolectric (`YvyRepositoryRuleTest`):**
  ```bash
  gradle :app:testDebugUnitTest --tests '*Rule*'
  ```

---

## 5. Instrucciones para Compilar APK de Prueba y Android App Bundle (AAB) para Google Play

### Generar APK de Prueba (Instalable en Teléfonos Android)
Desde el menú de configuración/exportación de AI Studio puedes descargar el APK directamente, o bien compilarlo localmente en Android Studio / línea de comandos:
```bash
gradle :app:assembleDebug
```
El archivo instalable se genera en:
`app/build/outputs/apk/debug/app-debug.apk`

### Generar Android App Bundle (AAB) para Publicación en Google Play
1. Genera tu keystore de producción (`my-upload-key.jks`) con `keytool` y configura las variables de entorno `KEYSTORE_PATH`, `STORE_PASSWORD` y `KEY_PASSWORD` en tu entorno de compilación segura (nunca subas el keystore ni las contraseñas al repositorio).
2. Ejecuta:
   ```bash
   gradle :app:bundleRelease
   ```
3. El paquete AAB firmado para subir a Google Play Console se ubicará en:
   `app/build/outputs/bundle/release/app-release.aab`

---

## 6. Lista de Comprobación para Publicar en Google Play y Estado del Proyecto

### Lista de Comprobación Pre-Lanzamiento (Google Play Checklist)
- [x] `applicationId` único configurado (`com.aistudio.yvycapital.pykxmp` o dominio propio antes del primer envío a Play Console).
- [x] Ícono adaptativo personalizado (`ic_launcher_foreground.xml`, `ic_launcher_background.xml` y PNGs en todas las densidades `mipmap-*`).
- [x] Reglas de seguridad Firestore Zero-Trust verificadas y desplegadas (`firestore.rules`).
- [x] Opción de eliminación de cuenta y datos disponible dentro de la aplicación (**Perfil -> Solicitar eliminación de mi cuenta y datos**), requisito obligatorio de Google Play.
- [x] Cumplimiento de permisos de mínimo privilegio (`INTERNET` y `ACCESS_NETWORK_STATE`, sin permisos invasivos de almacenamiento).
- [ ] Reemplazar las propiedades iniciales marcadas con `[DEMO]` por el catálogo comercial definitivo antes de la apertura al público.
- [ ] Registrar el certificado SHA-1 de producción (Play App Signing) en Firebase Console al publicar en producción.

### Funcionalidades Completadas vs. Próximas Etapas
- **Completado en esta etapa:**
  - Aplicación Android nativa en Kotlin + Jetpack Compose con tema visual Yvy Capital (verde bosque, beige cálido, dorado tierra y soporte para modo oscuro).
  - Autenticación real con Google Sign-In mediante Jetpack Credential Manager y protección contra consultas sin sesión.
  - Catálogo en tiempo real con soporte bimoneda Guaraníes (PYG) y Dólares (USD).
  - Buscador y filtros combinados por departamento, operación, categoría, dormitorios y cocheras, con vista en lista y cuadrícula y paginación incremental.
  - Ficha detallada con galería, superficies, comodidades, apertura de mapa, compartir, contacto por WhatsApp, agendamiento de visitas y reporte de anuncios.
  - Sincronización de favoritos y estado de solicitudes en Firestore.
  - Módulo **Yvy Capital Pro** integrado para publicar inmuebles y gestionar consultas recibidas.
- **Limitaciones Conocidas y Siguientes Pasos:**
  - **Autenticación y Almacenamiento:** El entorno aprovisiona automáticamente Google Sign-In y Firestore Enterprise. Para habilitar métodos adicionales o Firebase Cloud Storage para subida de archivos binarios pesados desde galería, se debe activar Firebase Storage en Firebase Console.
  - **Monetización (Google Play Billing):** La estructura de planes (`FREE`, `PRO_MONTHLY`, `PRO_ANNUAL`) ya está modelada y protegida contra autoasignación en las reglas del servidor. Los cobros reales están desactivados en esta primera etapa según lo solicitado.
