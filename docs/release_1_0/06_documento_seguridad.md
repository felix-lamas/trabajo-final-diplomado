# Documento de Seguridad

## 1. JWT
La plataforma usa JWT como mecanismo principal de autenticacion.

Propiedades:
- Stateless.
- Firma verificable.
- Adecuado para APIs REST.
- Control de expiracion por configuracion.

## 2. Roles
Roles funcionales:
- Administrador
- Organizador
- Estudiante
- Participante Externo
- Personal Control

## 3. Permisos
El acceso a endpoints se limita por rol y por contexto de operacion.

Ejemplos:
- Administracion completa para administradores.
- Gestion de eventos para organizadores.
- Registro de asistencia para personal de control.
- Consultas privadas para usuarios autenticados.

## 4. QR unico
El codigo QR se usa como token de acceso unico para control presencial. Su validacion evita:
- Reutilizacion indebida.
- Acceso fuera de contexto.
- Marcas duplicadas no autorizadas.

## 5. Validacion visual
La validacion visual complementa la verificacion tecnica:
- Fotografia del participante.
- Nombre y documento.
- Estado de habilitacion.
- Estado del pago e inscripcion.

## 6. Recuperacion de contrasena
La recuperacion usa token temporal:
- Token unico.
- Expiracion temporal.
- Un solo uso.
- Enlace enviado por correo.
- Contrasea nueva con politica segura.

## 7. Subida segura de archivos
La estrategia de seguridad incluye:
- Validacion de MIME real.
- Validacion de extension.
- Validacion de tamano.
- Renombrado aleatorio.
- Bloqueo de extensiones peligrosas.
- Limite de dimension y compresion para imagenes.
