# 7. Requerimientos Funcionales

Los requerimientos se han categorizado por módulos funcionales:

## 7.1. Módulo de Seguridad y Usuarios (RF-01)
*   **RF-01.01 Registro de Usuarios:** El sistema debe permitir el registro de usuarios externos mediante formulario.
*   **RF-01.02 Autenticación:** El sistema debe permitir el inicio de sesión seguro mediante correo y contraseña con JWT.
*   **RF-01.03 Gestión de Perfil:** Los usuarios podrán actualizar sus datos personales (Nombre, Apellido, CI, Profesión).
*   **RF-01.04 Recuperación de Contraseña:** Flujo de recuperación mediante envío de correo electrónico.

## 7.2. Módulo de Gestión de Eventos (RF-02)
*   **RF-02.01 Creación de Eventos:** El sistema permitirá a los coordinadores crear eventos con todos los campos definidos (nombre, descripción, categoría, etc.).
*   **RF-02.02 Gestión de Cupos:** El sistema debe controlar el número máximo de inscritos.
*   **RF-02.03 Catálogo Público:** Visualización de eventos disponibles con filtros por categoría y fecha.
*   **RF-02.04 Gestión de Estados:** Los eventos pasarán por estados (Borrador -> Pendiente -> Publicado -> Finalizado).

## 7.3. Módulo de Inscripciones y Pagos (RF-03)
*   **RF-03.01 Inscripción Gratuita:** El sistema permitirá la inscripción inmediata si el evento es gratuito y hay cupos.
*   **RF-03.02 Inscripción de Pago:** El usuario deberá adjuntar una imagen/PDF del comprobante de pago.
*   **RF-03.03 Validación de Pagos:** El Validador Financiero podrá Aprobar o Rechazar el pago. El usuario recibirá notificación.
*   **RF-03.04 Lista de Espera:** (Opcional) Registro de interesados cuando los cupos se agotan.

## 7.4. Módulo de Control de Acceso (RF-04)
*   **RF-04.01 Generación de QR:** Al aprobarse la inscripción, se generará un código QR único encriptado para el participante.
*   **RF-04.02 Credencial Digital:** Generación de una vista o PDF "Credencial" con datos del evento y el QR.
*   **RF-04.03 Escaneo de QR:** El Registrador podrá marcar asistencia escaneando el QR con la cámara de un dispositivo.
*   **RF-04.04 Registro Manual:** Búsqueda por CI o Código de Inscripción para marcar asistencia si el usuario no tiene su QR.

## 7.5. Módulo de Certificación (RF-05)
*   **RF-05.01 Generación de Certificados:** El sistema generará el certificado en PDF automáticamente tras el cierre del evento.
*   **RF-05.02 Criterios de Certificación:** Solo se generarán certificados para quienes cumplan con el % de asistencia definido.
*   **RF-05.03 Verificación Pública:** Buscador por código único (UUID) que muestre los datos de validez del certificado.

## 7.6. Módulo de Reportes (RF-06)
*   **RF-06.01 Reporte de Participación por Facultad:** Estadísticas de usuarios inscritos por unidad académica.
*   **RF-06.02 Reporte Económico:** Suma de ingresos por evento y por rango de fechas.
*   **RF-06.03 Reporte de Asistencia:** Lista de participantes con hora exacta de registro.
