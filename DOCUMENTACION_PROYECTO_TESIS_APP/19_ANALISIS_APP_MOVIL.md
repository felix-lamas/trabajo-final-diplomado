# Análisis para aplicación móvil

## Objetivo del documento
Distinguir servicios reutilizables de propuestas futuras.

## Información encontrada
**EXISTENTE reutilizable:** auth JWT, perfil, catálogo/detalle de eventos, inscripciones, pagos propios, credenciales/QR, certificados, encuestas y verificación pública.

**PROPUESTA:** cliente móvil Android/iOS que consuma la misma API REST, use almacenamiento seguro de token, presentación/lectura QR y notificaciones. No existe proyecto móvil, FCM, offline ni endpoints de push.

**APIs faltantes para móvil (PROPUESTA):** refresh/revocación de token, paginación/filtros estables, notificaciones y sincronización offline de control.

## Evidencias
`application.yml`; controladores; `frontend/.../jwt.interceptor.ts`; entidades de eventos/inscripciones/QR.

## Estado
EXISTENTE (API reutilizable) / PROPUESTA (móvil).

## Observaciones
Arquitectura propuesta: cliente móvil → API REST existente → backend monolítico → PostgreSQL.

## Inconsistencias
El token expira, pero no se encontró endpoint de refresh.

## Información faltante
NO VERIFICABLE: requisitos móviles, plataforma, conectividad y UX.
