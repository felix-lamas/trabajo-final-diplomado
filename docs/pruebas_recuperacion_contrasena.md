# Casos de Prueba Postman - Recuperacion de Contrasena

Coleccion importable:

`postman/recuperacion_contrasena.postman_collection.json`

## Variables

- `baseUrl`: `http://localhost:8080`
- `correo`: correo registrado del usuario.
- `tokenRecuperacion`: token recibido por correo o visible en logs si no hay SMTP configurado.

## Endpoints

### Solicitar recuperacion

`POST {{baseUrl}}/api/auth/recuperar-contrasena`

```json
{
  "correoElectronico": "{{correo}}"
}
```

Respuesta esperada: `200 OK`, sin revelar si el correo existe.

### Restablecer contrasena

`POST {{baseUrl}}/api/auth/restablecer-contrasena`

```json
{
  "token": "{{tokenRecuperacion}}",
  "nuevaContrasena": "NuevaClave123!",
  "confirmacion": "NuevaClave123!"
}
```

Respuesta esperada: `200 OK`.

## Casos negativos incluidos

- Token reutilizado: `400 Bad Request`.
- Token invalido: `400 Bad Request`.
- Confirmacion distinta: `400 Bad Request`.
- Contrasena insegura: `400 Bad Request` con `codigo = VALIDACION_ERROR`.

## Politica de contrasena

La contrasena debe tener al menos 8 caracteres e incluir mayuscula, minuscula, numero y caracter especial.
