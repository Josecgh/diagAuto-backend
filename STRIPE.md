# Premium con Stripe (pruebas)

Pago único de **99,99 EUR**. El plan FREE admite cuatro vehículos; añadir el quinto requiere PREMIUM. Se usa Checkout alojado en Stripe. El precio se define en el backend (9999 céntimos): no necesitas crear un `price_…` ni colocar claves en Angular.

## Configuración local

1. Desde `diagAuto-backend`, copia `stripe-local.properties.example` a `stripe-local.properties`.
2. En el Dashboard de Stripe, selecciona tu entorno de pruebas y copia su clave secreta `sk_test_…` en `stripe.secret-key` del archivo local. Este archivo está ignorado por Git. No compartas la clave en el chat.
3. Instala la [Stripe CLI](https://docs.stripe.com/stripe-cli), inicia sesión y escucha los eventos:

```sh
stripe login
stripe listen --events checkout.session.completed,checkout.session.async_payment_succeeded --forward-to localhost:8080/api/billing/webhook
```

4. Copia el secreto `whsec_…` que muestra `stripe listen` en `stripe.webhook-secret`. Mantén abierta esa terminal. Si cambia el secreto, actualiza el archivo y reinicia el backend.
5. Arranca el backend desde su carpeta y Angular en `http://localhost:4200`. Si usas otra dirección, actualiza `stripe.frontend-url` y la configuración CORS.

También se admiten variables de entorno: `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET` y `FRONTEND_URL`, sin necesidad del archivo local. La implementación actual acepta únicamente claves de pruebas.

## Probar el recorrido

Con una cuenta FREE, crea cuatro vehículos e intenta añadir el quinto. En Premium, pulsa **Pagar 99,99 € con Stripe**. Completa Checkout con la tarjeta de prueba `4242 4242 4242 4242`, una fecha futura y un CVC de tres cifras. Estos pagos no mueven dinero. [Guía oficial de pruebas](https://docs.stripe.com/testing).

Tras pagar, la aplicación vuelve a `/premium` y pide al backend confirmar la sesión. Solo se concede Premium si Stripe devuelve un pago completado de 9999 céntimos, en EUR, en modo de pruebas y asociado a esa cuenta. El webhook también concede Premium aunque cierres el navegador; su firma se verifica antes de consultar Stripe. Las entregas repetidas son idempotentes. [Confirmación y webhooks de Checkout](https://docs.stripe.com/checkout/fulfillment).

Cancelar, modificar la URL de retorno o crear una sesión de Checkout no activa Premium. Si la confirmación tarda, usa **Comprobar pago**. Después puedes añadir el quinto vehículo.

Las cuentas que ya activaron la demostración conservan su estado en la base de datos; para probar el pago, usa una cuenta FREE nueva. El endpoint de activación gratuita se ha eliminado.

## Verificación automatizada

Los tests simulan las respuestas de Stripe y verifican el límite de vehículos, la confirmación del pago, la propiedad de la sesión, las firmas y la repetición del webhook. No sustituyen la prueba completa con las claves de tu cuenta.
