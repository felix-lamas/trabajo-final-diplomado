# Diagramas existentes

## Objetivo del documento
Catalogar diagramas y su relación con la implementación.

## Información encontrada
Se localizaron 14 fuentes `.wsd` en `docs/design/`: casos de uso general/módulos; secuencias Auth, Certificado, Inscripción-Pago; actividades de certificación/control/inscripción-pago; modelo conceptual; clases de análisis; ER/MER; arquitectura por capas/general. Sus títulos internos confirman esta clasificación; `08_Arquitectura_General.wsd` declara explícitamente “Monolito Modular”.

## Evidencias
`docs/design/01_Casos_de_Uso_General.wsd` hasta `08_Arquitectura_General.wsd`.

## Estado
EXISTENTE.

## Observaciones
La clasificación deriva del nombre de archivo; son fuentes de PlantUML/WSd, no imágenes renderizadas revisadas durante esta auditoría.

## Inconsistencias
Coincidencias generales: arquitectura en capas, auth, inscripción/pago, certificado y control existen en código. Diferencias/faltantes detallados: NO VERIFICABLE sin lectura/parseo completo de cada fuente y render comparativo. Código además contiene facultades, carreras, categorías, credenciales, encuestas, reportes y asistencias.

## Información faltante
NO ENCONTRADO EN EL PROYECTO: diagrama de despliegue ejecutable, diagramas renderizados validados y control de versiones de diagramas.
