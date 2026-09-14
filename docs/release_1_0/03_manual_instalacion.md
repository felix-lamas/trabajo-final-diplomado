# Manual de Instalacion

## 1. Requisitos
- Java 21
- Maven 3.9 o superior
- Node.js 20 o superior
- Angular CLI 21
- PostgreSQL 16
- Docker y Docker Compose, opcional

## 2. Configuracion local

Copiar `.env.example` como `.env` en la raiz del repositorio y reemplazar los marcadores por valores locales seguros. El archivo `.env` contiene secretos, esta ignorado por Git y no debe compartirse ni versionarse.

La configuracion del backend y Docker Compose obtiene de ese archivo las variables de PostgreSQL, JWT, pgAdmin y usuarios de demostracion. No se deben escribir credenciales en `application.yml` ni en `docker/docker-compose.yml`.

## 3. Java 21
Verificar instalacion:
```bash
java -version
```

## 4. Maven
Verificar instalacion:
```bash
mvn -version
```

## 5. Angular
Verificar instalacion:
```bash
ng version
```

## 6. Docker
Si el entorno usa contenedores, levantar servicios con Docker Compose segun la configuracion institucional del proyecto.

## 7. Variables de entorno
Variables principales:
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASS`
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `FRONTEND_RESET_PASSWORD_URL`
- `MAIL_FROM`
- `UPLOADS_BASE_DIR`
- `PGADMIN_EMAIL`
- `PGADMIN_PASSWORD`
- `DEMO_PASSWORD`

## 8. Ejecucion
### Backend
```bash
cd backend
mvn spring-boot:run
```

### Docker Compose (opcional)
```bash
cd docker
docker compose --env-file ../.env up -d
```

### Frontend
```bash
cd frontend
npm install
ng serve
```


Credenciales demo en base nueva:
  admin@uajms.edu.bo / valor local de `DEMO_PASSWORD`
  Los demás usuarios demo también usan el valor local de `DEMO_PASSWORD`.

  `DEMO_PASSWORD` es exclusiva para desarrollo/demo y debe configurarse en el archivo local `.env`; no debe reutilizarse como una credencial de producción.

## 9. Validacion
- Confirmar respuesta del backend en `/swagger-ui.html`.
- Confirmar autenticacion y flujo de UI.
- Verificar conexion a PostgreSQL.
- Validar carga de archivos y rutas protegidas.
