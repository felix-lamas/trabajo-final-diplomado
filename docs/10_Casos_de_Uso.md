# 10. Casos de Uso Identificados

Se presentan los casos de uso principales agrupados por actor predominante.

## 10.1. Casos de Uso del Participante
*   **CU-01:** Registrarse en la plataforma.
*   **CU-02:** Ver catálogo de eventos.
*   **CU-03:** Inscribirse a un evento gratuito.
*   **CU-04:** Inscribirse a un evento de pago (Subir comprobante).
*   **CU-05:** Descargar credencial con QR.
*   **CU-06:** Descargar certificado digital.

## 10.2. Casos de Uso del Coordinador/Administrador
*   **CU-07:** Crear un nuevo evento.
*   **CU-08:** Validar comprobante de pago (Validador Financiero).
*   **CU-09:** Registrar asistencia por QR.
*   **CU-10:** Finalizar evento y liberar certificados.
*   **CU-11:** Generar reportes estadísticos.

## 10.3. Casos de Uso de Sistema
*   **CU-12:** Generar QR de acceso único.
*   **CU-13:** Notificar cambio de estado de inscripción por correo.
*   **CU-14:** Verificar autenticidad de certificado (Público).

---

### Detalle de Caso de Uso Crítico: CU-04 Inscribirse a un evento de pago
1. **Actor:** Participante.
2. **Pre-condición:** El usuario debe estar autenticado y el evento debe tener cupos disponibles.
3. **Flujo Principal:**
    a. El usuario selecciona el evento de pago.
    b. El sistema muestra los datos de cuenta bancaria/instrucciones de pago.
    c. El usuario carga la imagen del comprobante.
    d. El sistema crea la inscripción en estado "PENDIENTE_PAGO".
    e. El sistema notifica al Validador Financiero.
4. **Post-condición:** La inscripción queda a la espera de validación manual.
