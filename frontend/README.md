# Frontend

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 22.0.3.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Despliegue en Render y rutas SPA

El frontend se publica como **Static Site**. El archivo `../render.yaml`
versiona su configuración: raíz `frontend`, build `npm ci && npm run build`
y directorio publicado `dist/frontend/browser` (relativo a la raíz del servicio).

Angular Router resuelve las rutas en el navegador. Para abrir enlaces directos,
Render debe servir `index.html` cuando la ruta no corresponde a un archivo:

| Source | Destination | Action |
| --- | --- | --- |
| `/*` | `/index.html` | **Rewrite** |

La reescritura conserva la URL y sus parámetros, incluido `?token=...`.
Los archivos existentes (JavaScript, CSS, imágenes) se sirven normalmente.
No se requiere cambiar las rutas Angular ni las URLs del backend.

### Aplicar al sitio existente

Si el servicio está administrado por un Blueprint, sincronizar el
`render.yaml` de la raíz del repositorio con el **servicio existente**
`trabajo-final-diplomado-web`. Revisar el cambio antes de aplicarlo.
El Blueprint solo declara el frontend.

Si el sitio se creó manualmente, un push o redeploy **no importa automáticamente**
este YAML. En Render Dashboard, abrir el Static Site existente y agregar la
regla anterior en **Redirects/Rewrites**. Colocarla después de cualquier regla
más específica. Usar **Rewrite**, para conservar la ruta y el token en el
navegador. No crear un segundo sitio.

La regla de Static Site no se aplica a un Web Service con servidor propio;
si el Dashboard muestra ese tipo, revisar su comando de inicio y configurar
el fallback en dicho servidor antes de desplegar.

### Validación después de aplicar la configuración

1. Abrir directamente `/auth/verificar-correo?token=TOKEN_DE_PRUEBA` y
   `/auth/restablecer-contrasena?token=TOKEN_DE_PRUEBA` en el dominio del frontend.
2. Confirmar en Network que la petición del documento devuelve **200** con
   `index.html`, que carga la pantalla correspondiente y que la URL conserva
   `?token=TOKEN_DE_PRUEBA`. Un token ficticio puede mostrar un error de token;
   el documento no debe responder `Not Found`.
3. Recargar ambas páginas y comprobar la navegación interna.
4. Abrir un enlace real recibido por correo con una cuenta de prueba y un
   token vigente para validar el flujo completo.
5. Comprobar que JavaScript, CSS e imágenes siguen cargando correctamente.

Sin aplicar la regla en Render no se corrige el 404 de producción.
La compilación y las pruebas locales no sustituyen esta comprobación.

Referencias: [rewrites de Render](https://render.com/docs/redirects-rewrites)
y [Blueprint YAML](https://render.com/docs/blueprint-spec).

## Running unit tests

To execute unit tests with the [Vitest](https://vitest.dev/) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
