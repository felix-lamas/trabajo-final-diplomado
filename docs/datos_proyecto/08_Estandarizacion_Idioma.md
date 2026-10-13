# Estándar Oficial de Idioma y Nomenclatura

A partir de esta fase, el **ESPAÑOL** es el único idioma oficial para el proyecto. Se prohíbe el uso de términos en inglés en la estructura de base de datos, nombres de clases, atributos y documentación técnica.

## 1. Tabla de Equivalencias (Diccionario Técnico)

| Término Anterior (Inglés) | Término Oficial (Español) | Contexto |
| :--- | :--- | :--- |
| **User** | **Usuario** | Entidad, Tabla, Clase |
| **Role** | **Rol** | Entidad, Tabla, Clase |
| **Permission** | **Permiso** | Entidad, Tabla, Clase |
| **Event** | **Evento** | Entidad, Tabla, Clase |
| **Category** | **CategoriaEvento** | Entidad, Tabla, Clase |
| **Registration / Inscription** | **Inscripcion** | Entidad, Tabla, Clase |
| **Attendance** | **Asistencia** | Entidad, Tabla, Clase |
| **Payment** | **Pago** | Entidad, Tabla, Clase |
| **Certificate** | **Certificado** | Entidad, Tabla, Clase |
| **Notification** | **Notificacion** | Entidad, Tabla, Clase |
| **Audit Log** | **Auditoria** | Entidad, Tabla, Clase |
| **Dashboard** | **PanelAdministrativo** | Interfaz, Lógica |
| **Report** | **Reporte** | Funcionalidad |
| **Faculty** | **Facultad** | Organización Académica |
| **Career** | **Carrera** | Organización Académica |
| **Receipt / Voucher** | **Comprobante** | Atributo, Pago |
| **Status** | **Estado** | Atributo |
| **Modality** | **Modalidad** | Atributo |
| **Capacity** | **Cupo** | Atributo |
| **Start Date** | **FechaInicio** | Atributo |
| **End Date** | **FechaFin** | Atributo |
| **DNI / SSN** | **CI** | Cédula de Identidad |

## 2. Convenciones de Nomenclatura Oficial

### 2.1. Base de Datos (PostgreSQL)
*   **Formato:** `snake_case` (minúsculas).
*   **Tablas:** Nombres en singular (ej. `usuario`, `inscripcion`, `categoria_evento`).
*   **Columnas:** Nombres descriptivos (ej. `fecha_creacion`, `correo_electronico`).

### 2.2. Código Backend (Java 21)
*   **Clases:** `PascalCase` (ej. `Usuario`, `InscripcionEvento`).
*   **Atributos/Variables:** `camelCase` (ej. `fechaInicio`, `codigoVerificacion`).
*   **Métodos:** `camelCase` (ej. `validarPago`, `generarQr`).

### 2.3. Código Frontend (Angular 21)
*   **Componentes:** `kebab-case` para archivos, `PascalCase` para clases.
*   **Módulos:** Agrupados por funcionalidad en español (ej. `modulo-eventos`, `modulo-pagos`).

## 3. Matriz de Atributos Estandarizados

| Concepto | Tabla (snake_case) | Clase Java (PascalCase) | Campo Java (camelCase) |
| :--- | :--- | :--- | :--- |
| Correo Electrónico | `correo_electronico` | `Usuario` | `correoElectronico` |
| Fecha de Inicio | `fecha_inicio` | `Evento` | `fechaInicio` |
| Cupo Máximo | `cupo_maximo` | `Evento` | `cupoMaximo` |
| Código de Verificación | `codigo_verificacion` | `Certificado` | `codigoVerificacion` |
| Imagen Comprobante | `url_comprobante` | `Pago` | `urlComprobante` |
| Datos del QR | `datos_qr` | `Inscripcion` | `datosQr` |
