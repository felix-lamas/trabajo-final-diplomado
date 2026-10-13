# Pruebas Sprint 1.3 - Catálogos Académicos

## 1. Escenarios de Prueba Funcional

### Facultades
| Caso | Método | Endpoint | Body (JSON) | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| Crear Facultad | POST | `/api/v1/facultades` | `{"nombre": "Facultad de Ciencias y Tecnología", "descripcion": "Área técnica"}` | 201 Created |
| Nombre Duplicado| POST | `/api/v1/facultades` | `{"nombre": "Facultad de Ciencias y Tecnología"}` | 400 Bad Request |
| Listar Todas | GET | `/api/v1/facultades` | N/A | 200 OK (Lista) |
| Actualizar | PUT | `/api/v1/facultades/{id}` | `{"nombre": "F.C.y T.", "descripcion": "Actualizado", "estado": "ACTIVO"}` | 200 OK |
| Eliminar | DELETE | `/api/v1/facultades/{id}` | N/A | 204 No Content |

### Carreras
| Caso | Método | Endpoint | Body (JSON) | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| Crear Carrera | POST | `/api/v1/carreras` | `{"nombre": "Ingeniería Informática", "facultadId": "UUID-FAC"}` | 201 Created |
| Listar por Facultad | GET | `/api/v1/facultades/{id}/carreras` | N/A | 200 OK (Lista) |
| Eliminar | DELETE | `/api/v1/carreras/{id}` | N/A | 204 No Content |

## 2. Ejemplos de JSON

### FacultadResponse
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombre": "Facultad de Ciencias y Tecnología",
  "descripcion": "Área técnica y científica de la UAJMS",
  "estado": "ACTIVO",
  "cantidadCarreras": 5
}
```

### CarreraResponse
```json
{
  "id": "b1a2c3d4-e5f6-4321-8765-098765432109",
  "nombre": "Ingeniería Informática",
  "descripcion": "Gestión de sistemas y software",
  "estado": "ACTIVO",
  "facultadId": "550e8400-e29b-41d4-a716-446655440000",
  "facultadNombre": "Facultad de Ciencias y Tecnología"
}
```

## 3. Colección Postman (Fragmento)

**Variables:**
- `baseUrl`: `http://localhost:8080/api/v1`
- `token`: `{{jwt_token_admin}}`

**Endpoint Crear Facultad:**
- **URL:** `{{baseUrl}}/facultades`
- **Headers:** `Authorization: Bearer {{token}}`
- **Body:**
```json
{
    "nombre": "Facultad de Ciencias Económicas",
    "descripcion": "Gestión y administración"
}
```
