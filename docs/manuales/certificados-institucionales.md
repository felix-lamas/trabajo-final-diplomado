# Certificados institucionales

El backend genera el PDF al descargar el certificado persistido. La plantilla usa A4 horizontal, marco vectorial azul marino y rojo, los datos vinculados al certificado y el código QR de su URL de verificación persistida. El código, la URL y las reglas de emisión no cambian al volver a descargarlo.

## Configuración opcional

Configure estas variables en el entorno del backend. Los nombres y rutas de imágenes son configuración de despliegue y no se guardan en los certificados:

| Variable | Uso |
| --- | --- |
| `CERTIFICATE_VERIFICATION_BASE_URL` | URL pública de la página web `/verificar-certificado`. En perfil `prod`, el valor predeterminado apunta al frontend público actual; defina la URL del dominio si cambia. |
| `CERTIFICATE_FACULTY` | Nombre de facultad que aparece bajo el encabezado. |
| `CERTIFICATE_LOGO_PATH` | Ruta local a un logotipo institucional autorizado. Mantiene la proporción de la imagen. |
| `CERTIFICATE_AUTHORITY_1_NAME` / `CERTIFICATE_AUTHORITY_1_TITLE` | Nombre y cargo de la primera autoridad. |
| `CERTIFICATE_AUTHORITY_1_SIGNATURE_PATH` | Ruta a una imagen de firma autorizada para la primera autoridad. |
| `CERTIFICATE_AUTHORITY_2_NAME` / `CERTIFICATE_AUTHORITY_2_TITLE` | Nombre y cargo de la segunda autoridad. |
| `CERTIFICATE_AUTHORITY_2_SIGNATURE_PATH` | Ruta a una imagen de firma autorizada para la segunda autoridad. |

Las imágenes deben ser accesibles al proceso del backend. Si falta una ruta o no se puede cargar, el PDF se genera sin esa imagen. El repositorio inspeccionado no contiene un logotipo oficial UAJMS ni imágenes de firmas institucionales apropiadas; no reutilice el logotipo de Vidia como marca universitaria. Proporcione archivos aprobados por la institución antes de configurar esos recursos.

El modelo actual contempla certificados `CURRICULAR` y `NO_CURRICULAR`; sus reglas existentes determinan si son emitibles. No hay campos para texto de reconocimiento personalizado, ni un editor administrativo de plantillas, autoridades o facultades. La plantilla toma identidad, evento, tipo, horas, titular y código de las entidades persistidas. Una interfaz de administración y textos configurables requieren una fase separada con autorización y cambios explícitos al modelo.

## Prueba local

Configure la URL local `http://localhost:4200/verificar-certificado` en desarrollo (es el valor predeterminado del perfil actual), inicie backend y frontend con la configuración local habitual e inicie sesión como administrador o como organizador propietario de un evento. Emita/descargue un certificado mediante la pantalla existente de certificados. El QR debe abrir la ruta pública de verificación del frontend con el código persistido.

La prueba automatizada `CertificadoDocumentoServiceTest` verifica dimensiones/orientación, datos, nombres/eventos extensos con tildes, recursos opcionales ausentes, QR incluido y decodificación de la URL.
