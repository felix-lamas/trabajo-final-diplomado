# Documento de Despliegue

## 1. Servidor
Ambiente recomendado:
- Sistema operativo Linux para produccion.
- Java 21 instalado.
- Nginx o Apache como reverse proxy.

## 2. Base de datos
- PostgreSQL 16.
- Configurar usuario y privilegios restringidos.
- Habilitar backups automaticos.

## 3. Backups
Estrategia:
- Backup diario completo.
- Backup incremental o de WAL segun criticidad.
- Retencion minima acorde a politica institucional.
- Prueba de restauracion programada.

## 4. SSL
Recomendado:
- Certificado TLS valido.
- Redireccion HTTP a HTTPS.
- Proteccion de cookies o cabeceras si se usa proxy.

## 5. Produccion
Checklist:
- Variables de entorno definidas.
- Conexiones a BD verificadas.
- Swagger restringido segun politica interna.
- Uploads almacenados fuera del web root.
- Logs centralizados.
- Monitorizacion de errores y rendimiento.

## 6. Flujo de despliegue
1. Preparar base de datos.
2. Aplicar migraciones o validaciones de esquema.
3. Publicar backend.
4. Publicar frontend.
5. Verificar login, inscripcion, pago, QR, certificados y encuestas.
6. Revisar dashboard y reportes.
