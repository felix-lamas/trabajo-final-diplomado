# 8. Requerimientos No Funcionales

Para asegurar la calidad de la plataforma, se definen los siguientes atributos:

## 8.1. Rendimiento (RNF-01)
*   **RNF-01.01 Tiempo de Respuesta:** Las consultas al catálogo de eventos no deben exceder los 2 segundos bajo una carga normal de 100 usuarios concurrentes.
*   **RNF-01.02 Generación de PDF:** La generación de certificados masivos debe realizarse de forma asíncrona para no bloquear la interfaz.

## 8.2. Seguridad (RNF-02)
*   **RNF-02.01 Protección de Datos:** El sistema debe cumplir con principios básicos de privacidad, cifrando contraseñas mediante BCrypt.
*   **RNF-02.02 Sesiones:** Los tokens JWT deben tener un tiempo de expiración configurable (ej. 8 horas).
*   **RNF-02.03 Encriptación QR:** Los datos contenidos en el QR deben estar firmados para evitar su generación fraudulenta fuera del sistema.

## 8.3. Usabilidad (RNF-03)
*   **RNF-03.01 Diseño Responsivo:** La interfaz debe ser 100% funcional en dispositivos móviles (smartphones y tablets).
*   **RNF-03.02 Accesibilidad:** Cumplimiento de estándares WCAG 2.1 básicos para el contraste de colores y legibilidad.

## 8.4. Disponibilidad y Mantenibilidad (RNF-04)
*   **RNF-04.01 Arquitectura Modular:** El sistema debe estar estructurado en módulos independientes para facilitar actualizaciones sin afectar todo el sistema.
*   **RNF-04.02 Logging:** Registro de errores críticos en el servidor para facilitar el diagnóstico (uso de SLF4J/Logback).

## 8.5. Capacidad (RNF-05)
*   **RNF-05.01 Almacenamiento:** El sistema debe ser capaz de gestionar hasta 50,000 certificados generados por año sin degradación de rendimiento en la base de datos PostgreSQL.
