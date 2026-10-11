# diagAuto backend

API REST con Spring Boot, JPA, H2 y autenticación JWT en cookies.

## Ejecutar

```bash
mvn spring-boot:run
```

## Flujo de autenticación

Los POST protegidos por CSRF requieren el valor de la cookie `XSRF-TOKEN` también en el encabezado `X-XSRF-TOKEN`.

1. Haz `GET http://localhost:8080/api/auth/csrf`. Guarda el valor devuelto y conserva la cookie `XSRF-TOKEN`.
2. Registra la cuenta con `POST /api/users` y envía `X-XSRF-TOKEN` con ese valor. JSON:

   ```json
   {"email":"persona@example.com","password":"clave1234","name":"Ana","lastName":"Pérez"}
   ```

3. Haz `POST /api/auth/login` con el mismo encabezado y JSON `{"email":"persona@example.com","password":"clave1234"}`. La respuesta entrega el perfil y establece las cookies `access_token` y `refresh_token`.
4. Usa `GET /api/auth/me` para obtener el perfil autenticado. El cliente debe conservar y enviar las cookies.
5. Usa `POST /api/auth/refresh` para renovar las cookies o `POST /api/auth/logout` para eliminarlas. Ambos POST requieren el encabezado CSRF.
6. Para crear un vehÃ­culo, envÃ­a `POST /api/vehicles` con el mismo encabezado `X-XSRF-TOKEN` y conserva las cookies de login (`access_token`) y CSRF (`XSRF-TOKEN`). En Postman, activa el cookie jar para `localhost`. Ejemplo de body:

   ```json
   {
     "vehicleType": "CAR",
     "brand": "Seat",
     "model": "Ibiza",
     "plate": "1234ABC",
     "kilometres": 42000,
     "hasFault": false,
     "technicalInspectionDate": "2026-05-01",
     "faultDescription": null
   }
   ```

   El usuario debe iniciar sesiÃ³n antes de crear vehÃ­culos. Las rutas `GET /api/vehicles` y `GET /api/vehicles/{id}`, `PUT /api/vehicles/{id}` y `DELETE /api/vehicles/{id}` tambiÃ©n requieren autenticaciÃ³n.

Las rutas están bajo `/api/auth`: `GET /csrf`, `POST /login`, `POST /refresh`, `POST /logout` y `GET /me`. El registro está en `POST /api/users`.

En local, `app.cookie.secure` es `false` para permitir HTTP. En HTTPS configura `COOKIE_SECURE=true`. El secreto JWT de `application.properties` es solo para desarrollo local; en otros entornos define `JWT_SECRET` como un secreto Base64 de al menos 32 bytes.

The local H2 database is stored at ./data/diagauto.mv.db and persists across app restarts. Set DB_URL to use another database.
