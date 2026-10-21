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

---

## 7. Enlaces Directos a la Aplicación (Android App Links y Deep Links)

El proyecto cuenta con un sistema completo de enlaces compartibles verificables:

- **Rutas Canónicas Soportadas:**
  - Ficha directa de propiedad: `https://yvycapital.com.py/propiedad/{id}` (y esquema nativo `yvycapital://propiedad/{id}`)
  - Cartera pública de asesor: `https://yvycapital.com.py/asesor/{agentId}`
  - Búsqueda filtrada: `https://yvycapital.com.py/explorar?departamento=Asunción&operacion=SALE&tipo=APARTMENT`
- **Archivos de Verificación Web (`/web-deeplink/`):**
  - `/web-deeplink/.well-known/assetlinks.json`: Asociación Digital Asset Links configurada con `com.aistudio.yvycapital.pykxmp` y la huella `SHA-256` del certificado de firma. Al publicar el dominio `yvycapital.com.py`, sube este archivo a `https://yvycapital.com.py/.well-known/assetlinks.json`.
  - `/web-deeplink/index.html`: Página pública de destino para **Caso B** (cuando el destinatario aún no tiene la aplicación instalada), con redirección inteligente `intent://` hacia la app o hacia Google Play Store.
- **Comportamiento de los 3 Escenarios (Casos A, B y C):**
  - **Caso A (App instalada):** Al tocar un enlace (o con la app ya abierta en segundo plano gracias a `launchMode="singleTop"` y `onNewIntent`), Yvy Capital valida el identificador, consulta Firestore si no está en caché y abre directamente la ficha completa de la propiedad (`PropertyDetailScreen`). Si el usuario aún no inició sesión, el enlace pendiente se conserva y se abre automáticamente tras autenticarse.
  - **Caso B (App no instalada):** El enlace HTTPS abre la página pública de destino (`web-deeplink/index.html`) con información esencial y descarga desde Google Play, permitiendo además copiar el enlace para abrirlo tras la instalación.
  - **Caso C (Propiedad eliminada o no disponible):** Si el inmueble fue eliminado, pausado o el ID no existe, se muestra el diálogo `DeepLinkNotFoundDialog` con mensaje claro y botones para **Explorar Propiedades** o **Volver al Inicio**.

---

## 8. Planes de Membresía para Asesores y Generador de Videos Promocionales (Plan Senior)

1. **Planes de Membresía Validados en Interfaz y Backend (`firestore.rules` + `YvyRepository.kt`):**
   - **Plan Inicial — G. 99.000 mensuales (`PLAN_INICIAL`):**
     - Seguimiento en tiempo real de visualizaciones (`viewsCount`) de propiedades publicadas.
     - Máximo de 5 publicaciones activas (validado en cliente y en `canAdvisorPublishProperty` de `firestore.rules`).
     - Participación del asesor del 3,5 % por venta concretada.
     - Acceso autorizado a herramientas y bases de datos de clientes y propiedades de su rol.
   - **Plan Intermedio — G. 189.000 mensuales (`PLAN_INTERMEDIO`):**
     - Seguimiento de visualizaciones y máximo de 10 publicaciones activas.
     - Participación del asesor del 4 % por venta concretada.
     - Publicaciones destacadas (`featured = true`) y modalidad de agente inmobiliario externo a Yvy Capital (`externalAgentMode = true`).
   - **Plan Senior — G. 249.000 mensuales (`PLAN_SENIOR`):**
     - Seguimiento de visualizaciones y máximo de 10 publicaciones activas.
     - Participación del asesor del 4,5 % por venta concretada.
     - Publicaciones destacadas, modalidad de agente inmobiliario externo y consultas directas con asesores de Yvy Capital para colaborar en el cierre de operaciones.
     - **Exclusivo Plan Senior:** Acceso al **Generador de Videos Promocionales MP4** (`hasSeniorMembership()` en `firestore.rules`).
2. **Generador de Videos Promocionales (`PromoVideoGenerator.kt`):**
   - Renderiza en el dispositivo archivos `.mp4` reales (`MediaCodec` H.264 + `MediaMuxer`) a partir de 1 a 6 escenas/imágenes de la propiedad en formato vertical (`9:16` para Reels, TikTok y estados de WhatsApp) u horizontal (`16:9`), con titular promocional, precio en Guaraníes/Dólares, ubicación, características clave y música ambiental opcional, sin costo por cada video generado.
   - Incluye integración opcional con **Gemini Omni Flash (`gemini-omni-1.1-flash`)** vía `v1beta/interactions` si se configura `GEMINI_API_KEY` en el panel **Secrets** de AI Studio.
