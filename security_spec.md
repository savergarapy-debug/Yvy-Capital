# Especificación de Seguridad y Modelo de Amenazas — Yvy Capital (Firestore)

## 1. Invariantes de Datos (Data Invariants)

1. **Autenticación Obligatoria (`isSignedIn()`):** Ningún usuario no autenticado puede leer ni escribir en ninguna colección de Firestore. La autenticación se realiza mediante Google Sign-In con Jetpack Credential Manager.
2. **Aislamiento de PII (`users/{userId}`):** Los perfiles de usuario contienen información personal (`email`, `phone`, `displayName`). Solo el propietario (`request.auth.uid == userId`) o un administrador verificado (`isAdmin()`) puede leer (`get`, `list`), crear, actualizar o eliminar su propio perfil. Los usuarios no pueden autoasignarse el rol `admin`.
3. **Integridad del Catálogo (`properties/{propertyId}`):**
   - Lectura (`get`, `list`): Permitida para usuarios autenticados (`isSignedIn()`) sobre propiedades activas (`resource.data.status == 'active'`) o si el usuario es el asesor propietario (`resource.data.agentId == request.auth.uid`) o administrador (`isAdmin()`).
   - Creación (`create`): Solo usuarios autenticados donde `incoming().agentId == request.auth.uid`, con validación estricta de esquema (`isValidProperty(incoming())`), límites de texto, moneda (`PYG` o `USD`), operación (`SALE` o `RENT`) y sello temporal de servidor (`createdAt <= request.time`).
   - Actualización (`update`): Solo el propietario (`existing().agentId == request.auth.uid`) o `isAdmin()`, manteniendo inmutables `agentId` y `createdAt`, y validando `isValidProperty(incoming())`.
4. **Favoritos Sincronizados (`users/{userId}/favorites/{propertyId}`):**
   - Estricto aislamiento por subcolección bajo el `userId` del titular (`request.auth.uid == userId`).
   - Validación de `userId == request.auth.uid` y campos requeridos (`propertyId`, `title`, `price`, `currency`, `city`, `createdAt`).
5. **Consultas y Solicitudes de Visita (`inquiries/{inquiryId}`):**
   - Creación (`create`): Solo usuarios autenticados (`incoming().userId == request.auth.uid`) con estado inicial `'PENDING'`.
   - Lectura (`get`, `list`): Estrictamente restringida al cliente autor (`resource.data.userId == request.auth.uid`), al asesor destinatario (`resource.data.agentId == request.auth.uid`) o a `isAdmin()`.
   - Actualización (`update`): Bloqueo de estado terminal (`existing().status != 'CLOSED'`). El asesor o cliente puede actualizar el estado o notas dentro de las claves permitidas.
6. **Denuncias de Anuncios (`reports/{reportId}`):**
   - Creación (`create`): Solo usuarios autenticados (`incoming().reporterId == request.auth.uid`) con motivo válido y estado `'OPEN'`.
   - Lectura (`get`, `list`): Solo el denunciante (`resource.data.reporterId == request.auth.uid`) o `isAdmin()`.
7. **Perfiles y Suscripciones Yvy Capital Pro (`agent_profiles/{agentId}`):**
   - Lectura de perfil profesional para usuarios autenticados; escritura exclusiva del asesor titular (`request.auth.uid == agentId`) sin posibilidad de alterar campos de verificación administrativa ni concederse planes pagados sin validación.

## 2. The "Dirty Dozen" Payloads (Pruebas de Penetración y Rechazo)

1. **Lectura Anónima de Catálogo:** Usuario sin sesión intenta leer `properties`. -> `REJECT (PERMISSION_DENIED)`
2. **Robo de PII de Otro Usuario:** `alice` intenta hacer `get` sobre `users/bob`. -> `REJECT (PERMISSION_DENIED)`
3. **Escalada de Privilegios en Registro:** `alice` intenta crear `users/alice` con `role: "ADMIN"`. -> `REJECT (PERMISSION_DENIED)`
4. **Suplantación de Identidad en Propiedad:** `alice` intenta crear una propiedad con `agentId: "bob"`. -> `REJECT (PERMISSION_DENIED)`
5. **Shadow Update (Campo Fantasma):** `alice` intenta actualizar su propiedad añadiendo `isFeaturedVerified: true`. -> `REJECT (PERMISSION_DENIED)`
6. **Inyección de Moneda o Precio Inválido:** `alice` intenta crear una propiedad con `price: -500` o `currency: "EUR"`. -> `REJECT (PERMISSION_DENIED)`
7. **Manipulación de `createdAt` (Immortal Field):** `alice` intenta modificar `createdAt` durante un `update` de propiedad. -> `REJECT (PERMISSION_DENIED)`
8. **Acceso Cruzado a Favoritos:** `bob` intenta leer o borrar `users/alice/favorites/prop_1`. -> `REJECT (PERMISSION_DENIED)`
9. **Scraping de Consultas sin Filtro (Rules Are Not Filters):** `alice` ejecuta `db.collection("inquiries").get()` sin filtrar por `userId == alice`. -> `REJECT (PERMISSION_DENIED)`
10. **Lectura Cruzada de Consulta Privada:** `bob` intenta leer una consulta enviada por `alice` a otro asesor. -> `REJECT (PERMISSION_DENIED)`
11. **Reapertura de Consulta Cerrada (Terminal State Lock):** Intento de actualizar una consulta cuyo `existing().status == 'CLOSED'`. -> `REJECT (PERMISSION_DENIED)`
12. **Denial of Wallet (Payload Gigante):** Intento de crear una propiedad con `description` de más de 5000 caracteres o ID malformado. -> `REJECT (PERMISSION_DENIED)`
