# Cuenta USUARIO + ORGANIZADOR

La aprobacion conserva las asignaciones actuales y agrega ORGANIZADOR una sola vez.
La solicitud sigue requiriendo USUARIO, estado PENDIENTE y resolucion por ADMINISTRADOR.
Una solicitud pendiente que ya tiene ORGANIZADOR se resuelve sin duplicar la asignacion.
Una solicitud ya APROBADA sigue rechazando una segunda transicion. ADMINISTRADOR no
se convierte automaticamente en participante ni en organizador.

Las lecturas compartidas permiten el registro personal con USUARIO o el evento propio
con ORGANIZADOR. Las operaciones de gestion mantienen su alcance anterior. La consulta
de sesiones como participante sigue requiriendo inscripcion CONFIRMADA. Las reglas de
QR, GPS, pagos, inscripciones y certificados no cambian. Los DTO y JWT no cambian.
Flutter mantiene exclusivamente la experiencia de participante.

## Verificacion local y pasos pendientes

Consulta de solo lectura del esquema local el 2026-10-08:
- 1 cuenta tiene ORGANIZADOR sin USUARIO.
- 0 pares (usuario_id, rol_id) duplicados.
- usuario_rol tiene PK id y FK hacia usuario y rol, sin UNIQUE del par.
- El seed demo declara 1 organizador aprobado, ahora con USUARIO + ORGANIZADOR.

No se ejecutaron actualizaciones de cuentas, migraciones ni el seed. No se consulto
la base desplegada. El cambio de codigo no restaura retroactivamente las asignaciones.

Antes de regularizar cuentas existentes, revisar en cada entorno los UUID afectados,
la aprobacion y su procedencia; excluir cuentas administrativas y resolver inconsistencias.
Agregar solo la asignacion USUARIO faltante a las cuentas organizadoras que correspondan,
en una operacion transaccional revisada y con respaldo. Mantener intactos usuario.id,
inscripciones, pagos, asistencias, certificados y eventos. No realizar un UPDATE masivo.

La unicidad del par es recomendable como defensa de integridad, pero no se agrega en
esta entrega. Requiere una migracion Flyway nueva, previa revision de duplicados y de
sus referencias, y plan de bloqueo/despliegue. No editar V1, V2 ni V3.

Consultas de revision (solo lectura):

```sql
SELECT usuario_id, rol_id, COUNT(*)
FROM usuario_rol
GROUP BY usuario_id, rol_id
HAVING COUNT(*) > 1;

SELECT u.id, u.estado_solicitud_organizador
FROM usuario u
WHERE EXISTS (
  SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id = ur.rol_id
  WHERE ur.usuario_id = u.id AND r.nombre = 'ORGANIZADOR'
) AND NOT EXISTS (
  SELECT 1 FROM usuario_rol ur JOIN rol r ON r.id = ur.rol_id
  WHERE ur.usuario_id = u.id AND r.nombre = 'USUARIO'
);
```

## Validacion manual posterior al despliegue

Registrar/usar una cuenta USUARIO, solicitar y aprobar desde otra cuenta ADMINISTRADOR.
Refrescar perfil: deben aparecer USUARIO y ORGANIZADOR con el mismo UUID. Con esa cuenta,
participar en un evento organizado por otra persona y comprobar inscripcion, comprobante,
pago, sesiones, QR/GPS, certificado e historial en Flutter y Web. Verificar la gestion de
sus propios eventos en Web y la denegacion de datos ajenos fuera de ese alcance.
Confirmar que ADMINISTRADOR conserva sus permisos y que ORGANIZADOR sin USUARIO no
adquiere permisos personales por inferencia.

Las pruebas automatizadas de servicios usan repositorios simulados y las de clientes
usan HTTP simulado; no sustituyen esta validacion contra una API desplegada y un telefono.
La prueba opcional de almacenamiento Supabase requiere RUN_SUPABASE_STORAGE_SMOKE y
no es necesaria para este cambio de roles.
