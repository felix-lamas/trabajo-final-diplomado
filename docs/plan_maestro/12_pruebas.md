# 12. Pruebas y evidencia

## Últimos resultados registrados (E3.2)

| Aplicación | Comando | Resultado registrado | Alcance |
|---|---|---|---|
| Backend | `mvn test` | 460 tests, 0 failures, 0 errors, 1 skipped; BUILD SUCCESS | Ejecución local; smoke de Supabase omitido. |
| Angular | `npm test -- --watch=false` | 271/271 PASS; build PASS con warnings de presupuesto | Ejecución local. |
| Flutter | `flutter test --no-pub --reporter expanded` | Interrumpido sin resultado concluyente | No afirmar PASS/FAIL. |

Estos resultados son referencia de E3.2 en `fc9ce15`, no pruebas de producción ni una nueva ejecución en E3.4. Mantener reporte reproducible versionable, con comando/versiones/commit/fecha.

## Cobertura requerida

- Cada CA de MUST (RF-01,03,04,05,06,07,08): camino feliz y error, datos de prueba, esperado/obtenido/estado/evidencia.
- Negativos: 401, 403 por rol insuficiente, 400 validación, 404 no encontrado/scope, 409 estado/conflicto, tamaño/MIME invalido.
- RF-07: pago externo, upload, persistencia Storage, revisor con ownership y Flutter futuro.
- RF-08: sesión, QR vencido/rotado, ubicación fuera radio/precisión, duplicidad y permisos cámara/location en Android.
- RF-09/13: elegibilidad, PDF y verificación pública sin datos privados.
- RNF-01…RNF-05: pruebas de rendimiento, seguridad, usabilidad, compatibilidad y disponibilidad con métricas de `docs/08_Requerimientos_No_Funcionales.md` sin cambiarlas.

Las pruebas unitarias/mock no acreditan Storage/SMTP/DB reales ni disponibilidad. Las ejecuciones externas deben ser autorizadas, fechadas y sanitizadas.
