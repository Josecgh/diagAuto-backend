# diagAuto backend

API REST con Spring Boot, JPA y H2 en memoria.

## Ejecutar

```bash
mvn spring-boot:run
```

## Endpoints

| Método | Ruta | Acción |
|---|---|---|
| GET | `/api/users` | Listar usuarios (sin contraseña) |
| GET | `/api/users/{id}` | Consultar usuario |
| POST | `/api/users` | Crear usuario |
| PUT | `/api/users/{id}` | Actualizar usuario |
| DELETE | `/api/users/{id}` | Eliminar usuario y sus vehículos |
| GET | `/api/vehicles` | Listar vehículos; admite `?userId={id}` |
| GET | `/api/vehicles/{id}` | Consultar vehículo |
| POST | `/api/vehicles` | Crear vehículo |
| PUT | `/api/vehicles/{id}` | Actualizar vehículo |
| DELETE | `/api/vehicles/{id}` | Eliminar vehículo |

Las peticiones POST/PUT reciben JSON. Para crear un usuario:

```json
{"email":"persona@example.com","password":"clave","accountStatus":"ACTIVE"}
```

Para crear un vehículo, incluya `userId` con el usuario propietario:

```json
{"vehicleType":"car","brand":"Toyota","model":"Corolla","plate":"1234ABC","kilometers":45000,"hasFault":false,"faultDescription":null,"userId":1}
```

H2 es temporal: los datos se borran al detener la aplicación. La contraseña de la entidad de usuario se guarda como texto por ahora; debe almacenarse con hash antes de usar el backend con datos reales.

## Acceso HTTP

`POST /api/users` queda abierto para permitir el registro. El resto de rutas requiere autenticación HTTP Basic. Al no existir todavía un sistema de login/JWT ni una conexión entre Spring Security y los usuarios de la aplicación, Spring Boot crea un usuario temporal `user` y muestra su contraseña en la consola al arrancar. No uses esta configuración temporal en producción.
