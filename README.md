# DiagAuto Backend

API REST para gestionar usuarios y sus vehículos, registrar averías y consultar fechas de inspección técnica. Incluye autenticación mediante sesión y un plan Premium con Stripe Checkout en modo de pruebas.

## Tecnologías

- Java 17 o superior.
- Spring Boot 3.3.5 y Maven.
- Spring Web, Spring Security, Spring Data JPA y Bean Validation.
- H2 con persistencia en archivo.
- BCrypt para las contraseñas y Lombok.
- SDK de Stripe para Java.
- JUnit, Mockito, MockMvc y JaCoCo.

## Ejecutar en local

Desde la carpeta `diagAuto-backend`:

```bash
mvn spring-boot:run
```

En PowerShell puedes usar `mvn.cmd`. También puedes ejecutar `src/main/java/com/auto/Main.java` desde el IDE.

| Servicio | Dirección |
| --- | --- |
| API | `http://localhost:8080/api` |
| Frontend permitido por CORS | `http://localhost:4200` |
| Consola H2 | `http://localhost:8080/h2-console` |

La base de datos se guarda en `./data/diagauto.mv.db` y conserva los datos entre reinicios. La ruta es relativa a la carpeta desde la que se arranca Java; ejecutar siempre desde la carpeta del backend evita crear bases de datos distintas por accidente.

## Acceder a la consola H2

1. Arranca el backend y déjalo en ejecución.
2. Abre `http://localhost:8080/h2-console` en el navegador.
3. Completa el formulario de conexión con estos valores:

| Campo | Valor |
| --- | --- |
| Saved Settings | `Generic H2 (Embedded)` |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:file:./data/diagauto;DB_CLOSE_ON_EXIT=FALSE` |
| User Name | `sa` |
| Password | Déjalo vacío |

4. Pulsa **Test Connection** para comprobar la conexión y después **Connect** para entrar.
5. Selecciona una tabla en el panel izquierdo o ejecuta consultas en el editor SQL:

```sql
SELECT * FROM USERS;
SELECT * FROM VEHICLES;
```

Estas credenciales son las de la base de datos. Para acceder a esta consola local no hace falta iniciar sesión con una cuenta de DiagAuto.

Si configuras la variable de entorno `DB_URL`, utiliza ese mismo valor en el campo **JDBC URL**. Si no aparecen tus tablas o datos, comprueba que la URL coincide con la del backend; la ruta `./data/diagauto` depende de la carpeta desde la que arrancaste Java.

## Autenticación

La versión actual usa una **sesión HTTP en el servidor**. Al iniciar sesión, Spring Security guarda la autenticación y el navegador conserva la cookie `JSESSIONID`. Las contraseñas se almacenan con BCrypt.

Para trabajar con las rutas privadas:

- Conserva y envía la cookie de sesión.
- En Angular, las peticiones autenticadas usan `withCredentials: true`.
- En Postman, conserva las cookies de `localhost` mediante su cookie jar.

### Flujo de uso

1. Registra una cuenta con `POST /api/users`.
2. Inicia sesión con `POST /api/auth/login` y conserva la cookie `JSESSIONID`.
3. Consulta tu perfil con `GET /api/auth/me`.
4. Gestiona tus vehículos enviando la cookie de sesión.
5. Cierra sesión con `POST /api/auth/logout`, que invalida la sesión del servidor.

### Registro

`POST /api/users` — responde `201 Created`. La contraseña debe tener al menos ocho caracteres. Las cuentas nuevas tienen el plan `FREE`.

```json
{
  "email": "persona@example.com",
  "password": "clave1234",
  "name": "Ana",
  "lastName": "Pérez"
}
```

### Inicio de sesión

`POST /api/auth/login` — devuelve el perfil del usuario y establece la sesión.

```json
{
  "email": "persona@example.com",
  "password": "clave1234"
}
```

## Endpoints

Todas las rutas de la tabla están bajo `/api`.

| Método | Ruta | Acceso | Función |
| --- | --- | --- | --- |
| POST | `/users` | Público | Registrar una cuenta |
| GET | `/users/{id}` | Sesión, cuenta propia | Consultar el usuario |
| POST | `/auth/login` | Público | Iniciar sesión |
| POST | `/auth/logout` | Público | Invalidar la sesión actual |
| GET | `/auth/me` | Sesión | Consultar el perfil autenticado |
| GET | `/vehicles` | Sesión | Listar los vehículos propios |
| POST | `/vehicles` | Sesión | Añadir un vehículo |
| GET | `/vehicles/{id}` | Sesión, vehículo propio | Consultar un vehículo |
| PUT | `/vehicles/{id}` | Sesión, vehículo propio | Actualizar un vehículo |
| DELETE | `/vehicles/{id}` | Sesión, vehículo propio | Borrar un vehículo |
| POST | `/billing/checkout` | Sesión | Crear una sesión de pago de Stripe |
| POST | `/billing/checkout/{id}/confirm` | Sesión, pago propio | Confirmar el pago y activar Premium |
| POST | `/billing/webhook` | Firma de Stripe | Recibir eventos de pago |

La API asigna cada vehículo al usuario autenticado: no se envía un `userId` al crearlo. Las consultas y modificaciones comprueban la propiedad del vehículo.

### Crear o actualizar un vehículo

Envía este formato a `POST /api/vehicles` o `PUT /api/vehicles/{id}`:

```json
{
  "vehicleType": "CAR",
  "brand": "Seat",
  "model": "Ibiza",
  "plate": "1234ABC",
  "kilometres": 42000,
  "hasFault": false,
  "technicalInspectionDate": "2027-05-01",
  "faultDescription": null
}
```

`vehicleType`, `brand`, `model`, `plate`, `kilometres` y `hasFault` son obligatorios. El kilometraje debe ser un entero no negativo. La fecha de inspección y la descripción de avería son opcionales. Las fechas usan el formato `AAAA-MM-DD`.

Los tipos de vehículo admitidos son `CAR`, `MOTORCYCLE`, `TRUCK`, `VAN`, `BICYCLE` y `BUS`.

La creación devuelve `201 Created` y el borrado `204 No Content`. Un vehículo inexistente o que no pertenece al usuario devuelve `404`. Los datos que incumplen la validación del DTO devuelven `400`.

## Planes y Stripe

- **FREE:** hasta cuatro vehículos. Intentar crear el quinto devuelve `409 Conflict`, con motivo `FREE_VEHICLE_LIMIT` en el backend.
- **PREMIUM:** permite superar ese límite mediante un pago único de **99,99 EUR**.

El límite se aplica en el backend, con una transacción y un bloqueo sobre el usuario durante la creación. El frontend también protege la ruta de creación y dirige a la página Premium cuando se alcanza el límite.

Stripe funciona únicamente en **modo de pruebas**. Crear Checkout o volver a la página de éxito no concede Premium por sí solo: el backend consulta Stripe y comprueba el pago, el importe, la moneda y la cuenta asociada. El webhook verifica la firma de Stripe y también puede activar el plan. Las entregas repetidas no vuelven a modificar una cuenta ya Premium.

### Configuración local

Copia `stripe-local.properties.example` a `stripe-local.properties` y completa las claves localmente. El archivo está ignorado por Git. También se admiten estas variables de entorno:

| Variable | Uso | Valor por defecto |
| --- | --- | --- |
| `DB_URL` | URL JDBC de H2 | `jdbc:h2:file:./data/diagauto;DB_CLOSE_ON_EXIT=FALSE` |
| `STRIPE_SECRET_KEY` | Clave secreta de pruebas `sk_test_…` | Vacío |
| `STRIPE_WEBHOOK_SECRET` | Secreto de firma del webhook `whsec_…` | Vacío |
| `FRONTEND_URL` | Dirección de retorno de Checkout | `http://localhost:4200` |

El arranque desde `Main.java` localiza el archivo de configuración tanto desde la carpeta del proyecto como desde la ubicación de las clases compiladas. Reinicia el backend después de cambiar las claves.

La clave publicable no es necesaria en Angular para este flujo de Checkout alojado. La instalación de Stripe CLI, el listener, las tarjetas de pruebas y el recorrido completo están documentados en [STRIPE.md](STRIPE.md).

Si Checkout devuelve `503`, comprueba que el backend está cargando una clave de pruebas válida. Para recibir eventos localmente, mantén activo el listener de Stripe CLI y el backend en el puerto 8080.

## Tests y cobertura

Ejecuta los tests desde la carpeta del backend:

```bash
mvn test
```

Para generar el informe de cobertura de JaCoCo:

```bash
mvn verify
```

El informe se genera en `target/site/jacoco/index.html`. La suite cubre autenticación, seguridad, usuarios, CRUD de vehículos, propiedad de los recursos, límite del plan gratuito y confirmación de pagos. Los tests de pagos simulan Stripe y no realizan cargos.

## Organización del código

El código principal está en `src/main/java/com/auto`:

| Paquete | Responsabilidad |
| --- | --- |
| `auth` | Inicio y cierre de sesión, perfil autenticado |
| `secure` | Spring Security, CORS y BCrypt |
| `user` | Usuarios, DTO, servicio y repositorio |
| `vehicles` | Vehículos, DTO, servicio y repositorio |
| `billing` | Checkout, webhook y activación de Premium |
| `enums` | Tipos de vehículo y planes de cuenta |
| `mappers` | Conversión entre entidades y DTO |
| `contracts` | Interfaces de los servicios |

La configuración está en `src/main/resources/application.properties` y los tests en `src/test/java/com/auto`.
