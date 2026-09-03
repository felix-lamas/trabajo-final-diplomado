# Autenticación y seguridad

## Objetivo del documento
Identificar controles y riesgos sustentados en configuración/código.

## Información encontrada
**EXISTENTE:** autenticación JWT sin sesión (`STATELESS`); filtro JWT antes de `UsernamePasswordAuthenticationFilter`; contraseña con BCrypt; autorización a nivel de método con `@PreAuthorize`; endpoints públicos explícitos; recuperación con token de 30 minutos según anotación/controlador; validación Bean Validation; límite multipart 5 MB; comprobantes solo jpg/jpeg/png/pdf y MIME validado con Tika. Swagger queda público.

| Hallazgo | Nivel | Evidencia | Motivo |
|---|---|---|---|
| Secretos/credenciales de BD por defecto y `.env` versionado | Alto | `.env`, `application.yml` | permite exposición de secreto JWT y acceso si se despliega sin sustitución |
| CSRF deshabilitado | Bajo/condicional | `SecurityConfig.java` | coherente con Bearer stateless; debe reevaluarse si se usan cookies |
| `ddl-auto: update` y `show-sql: true` | Medio | `application.yml` | riesgo de cambios/esquema o revelación de consultas en entornos no controlados |
| Sin CORS explícito encontrado | Medio | `SecurityConfig.java` | no se verifica política de origen en backend |
| Ruta de archivo configurable local | Medio | `ArchivoSeguroServicio.java` | se normaliza la ruta, pero no se verificó un control adicional de exposición HTTP |

## Evidencias
`backend/.../core/seguridad/SecurityConfig.java`, `JwtService.java`; `application.yml`; `ArchivoSeguroServicio.java`.

## Estado
EXISTENTE; evaluación dinámica NO VERIFICABLE.

## Observaciones
JWT se firma con una clave Base64 configurada y expira según propiedad (por defecto 24 horas).

## Inconsistencias
No se observó configuración CORS aunque frontend y backend se declaran en puertos distintos.

## Información faltante
NO ENCONTRADO EN EL PROYECTO: rate limiting, MFA, revocación de JWT, cabeceras de seguridad, análisis de dependencias y pruebas de penetración.
