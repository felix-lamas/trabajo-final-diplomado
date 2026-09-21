# 13. DESPLIEGUE

Proveedor de alojamiento gratuito: por definir.

La documentación deberá cubrir instalación, variables de entorno, base de datos, backend, frontend web, configuración móvil y pruebas.

No incluir secretos en el repositorio.

## Perfiles y datos demo
El arranque normal no ejecuta datos de demostración. `DatosInicialesSeed` solo se habilita con el perfil Spring `demo`:

```bash
SPRING_PROFILES_ACTIVE=demo mvn spring-boot:run
```

La activación exige configurar `DEMO_PASSWORD` localmente. Los entornos de producción no deben incluir `demo` en `SPRING_PROFILES_ACTIVE` ni definir el seed como parte de su procedimiento de despliegue.
