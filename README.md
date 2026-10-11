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

Las rutas están bajo `/api/auth`: `GET /csrf`, `POST /login`, `POST /refresh`, `POST /logout` y `GET /me`. El registro está en `POST /api/users`.

En local, `app.cookie.secure` es `false` para permitir HTTP. En HTTPS configura `COOKIE_SECURE=true`. El secreto JWT de `application.properties` es solo para desarrollo local; en otros entornos define `JWT_SECRET` como un secreto Base64 de al menos 32 bytes.

H2 está en memoria y borra los datos al detener la aplicación. El refresh token es stateless: el logout limpia las cookies del cliente, pero un token ya copiado sigue siendo válido hasta que expire.
