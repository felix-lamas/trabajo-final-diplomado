# PLAN MAESTRO DE IMPLEMENTACIÓN

> **Documento histórico/supersedido como fuente normativa (E3.4).** Se conserva por trazabilidad. Sus estimaciones, perfil, decisiones y estados de sprint no deben tomarse como descripción vigente ni como evidencia de ejecución. Use `01_estado_actual.md` y los documentos auxiliares reconciliados; la monografía se actualizará en E3.5. E2 es antecedente, no límite del alcance final.
## Plataforma web y móvil para la gestión de eventos universitarios – UAJMS

**Versión:** 1.0  
**Fecha inicial:** 20/09/2026  
**Estado:** Documento base de implementación

## 1. Propósito
Este documento es la fuente de verdad funcional y técnica para adaptar progresivamente el sistema existente al Perfil de Proyecto.

## 2. Reglas de trabajo con Codex
1. Consultar este documento antes de implementar una tarea.
2. Consultar únicamente los documentos relacionados con la tarea y el código directamente afectado.
3. No realizar una auditoría general del repositorio salvo solicitud explícita.
4. No introducir roles, módulos, tecnologías o funcionalidades fuera del alcance.
5. Antes de modificar una funcionalidad existente, clasificarla como CONSERVAR, ADAPTAR, COMPLETAR, REEMPLAZAR, ELIMINAR o NUEVO.
6. Mantener Java 21, Spring Boot 3.3.0, Angular 21, Flutter 3.44.8 y PostgreSQL 16, salvo decisión documentada.
7. No incorporar microservicios.
8. No introducir credenciales, secretos ni datos personales reales en el repositorio.
9. Toda modificación funcional debe incluir pruebas o evidencia de verificación.
10. Después de una modificación importante, actualizar la documentación correspondiente y la bitácora.
11. Si existe contradicción entre el código actual y este plan, identificarla y registrarla; no asumir.

## 3. Alcance objetivo
Plataforma para gestionar eventos universitarios de la Universidad Autónoma Juan Misael Saracho. Incluye eventos académicos, educativos, diplomados, conferencias, culturales, deportivos e institucionales.

## 4. Roles definitivos
- Administrador
- Organizador
- Usuario

Estudiante UAJMS y participante externo son tipos de usuario, no roles.

## 5. Arquitectura objetivo
Angular 21 / Flutter 3.44.8 → HTTPS/JSON → Spring Boot 3.3.0 / Java 21 → JDBC/SQL → PostgreSQL 16.

Se utilizará un monolito modular profesional. No se contemplan microservicios.

## 6. Flujo principal
Registro/login → consulta de eventos → inscripción → pago cuando corresponda → validación → asistencia QR + GPS → finalización → certificación cuando corresponda.

## 7. Fuera de alcance
Pasarela de pago, chat interno, videoconferencias/streaming, facturación electrónica, reconocimiento facial, blockchain, red social/comentarios, múltiples universidades, aplicación móvil de Administrador u Organizador, IA para decisiones complejas y sistema complejo de notificaciones multicanal.

## 8. Clasificación de adaptación
Cada módulo existente será clasificado como:
- CONSERVAR
- ADAPTAR
- COMPLETAR
- REEMPLAZAR
- ELIMINAR
- NUEVO

## 9. Criterio de cierre
Una tarea termina cuando la funcionalidad coincide con este plan, el código compila, las pruebas previstas pasan, no existen regresiones conocidas y la documentación afectada está actualizada.
