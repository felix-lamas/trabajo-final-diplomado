# Actores y roles de Vidia

Los roles funcionales son exactamente `ADMINISTRADOR`, `ORGANIZADOR` y `USUARIO`. Las acciones son autorizadas por backend; ocultar controles en cliente no constituye autorización.

| Actor funcional | Rol técnico | Responsabilidades principales |
|---|---|---|
| Administrador | `ADMINISTRADOR` | Resolver solicitudes de organizador; administrar categorías; revisar/publicar/rechazar eventos; consultar o resolver operaciones globales solo donde el backend lo permita; consultar reportes autorizados. |
| Organizador | `ORGANIZADOR` | Administrar eventos propios; enviarlos a revisión; configurar QR de pago opcional en estados permitidos; gestionar sesiones; consultar inscripciones/asistencia y validar pagos/certificados en su ámbito. |
| Usuario/Participante | `USUARIO` | Consultar eventos; inscribirse; gestionar sus pagos y comprobantes; registrar asistencia mediante el cliente móvil previsto; consultar su historial/certificados. |
| Tercero verificador | Sin rol ni autenticación requerida | Consultar el resultado público de verificación de certificado por código, con acceso únicamente a los datos que expone el contrato público. |

“Participante” es una descripción funcional de `USUARIO`, no un cuarto rol. Usuario interno UAJMS y externo pueden ser tipos/datos de registro, pero no autoridades. No existen roles funcionales separados de Super Administrador, Coordinador, Validador Financiero, Registrador, Estudiante, Participante Externo o Personal de Control.
