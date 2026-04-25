# Guia de codificacion

## Objetivo
Estandarizar como realizar codigo en `ms-usuarios` para mantener orden, consistencia y calidad.

## Alcance
Aplica a todo codigo nuevo en:
- `controller`
- `service/contratos`
- `service/implementacion`
- `dao`
- `model`
- `mapper`
- `dto/request` y `dto/response`
- `test`

## Arquitectura obligatoria
Flujo obligatorio:

```text
controller -> service (contrato + impl) -> dao -> model -> mapper -> dto
```

Reglas:
- `controller` no contiene logica de negocio compleja.
- `service` concentra reglas de negocio y orquestacion.
- `dao` solo acceso a datos.
- `mapper` (MapStruct) hace conversion entidad <-> DTO.
- Nunca devolver entidades JPA desde endpoints.

## Reglas de realizar codigo

### 1) Estructura y nombres
- Crear clases con convencion:
  - `*Controller`
  - `*Service` (interface)
  - `*ServiceImpl` (implementacion)
  - `*Dao`
  - `*Mapper`
  - `*Request` / `*Response`
- Mantener paquetes por capa sin mezclar responsabilidades.

### 2) Endpoints REST
- Versionado obligatorio: `/v1/<recurso>`.
- Usar `ResponseEntity` con codigos HTTP explicitos.
- Aplicar `@Valid` en request body.
- Mantener nombres de endpoints y metodos claros.

### 3) DTO y validaciones
- Definir DTOs de entrada/salida para cada endpoint.
- Usar Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Email`, etc.).
- No hacer validaciones repetitivas con `if` si pueden ir en anotaciones.

### 4) Mapper
- Usar MapStruct (`@Mapper(componentModel = "spring")`).
- Toda conversion entidad/DTO debe pasar por mapper.
- Evitar mapeo manual en controller o service salvo casos excepcionales documentados.

### 5) Manejo de errores
- Usar excepciones de negocio especificas.
- Centralizar respuestas de error en `ControllerAdvice`.
- No usar `Exception` generica para casos de negocio.

### 6) Seguridad
- No hardcodear secretos, tokens, passwords o credenciales.
- No loggear datos sensibles.
- Mantener compatibilidad con JWT y reglas de seguridad existentes.

### 7) Logging
- Usar logs estructurados con contexto.
- Evitar `System.out.println`.
- Mensajes de error con informacion util para soporte sin exponer datos sensibles.

### 8) Testing minimo
Para cada modulo nuevo, incluir como minimo:
- Test de controller (ruta feliz y al menos un caso de error).
- Test de service (reglas de negocio principales).

## Orden recomendado para generar un modulo nuevo
1. Crear DTO `Request` y `Response`.
2. Crear `model` y `dao`.
3. Crear `service` (contrato + implementacion).
4. Crear `mapper`.
5. Crear `controller`.
6. Crear/actualizar manejo de errores si aplica.
7. Crear tests.
8. Ajustar OpenAPI annotations si cambia API.

## Definition of Done (DoD) para codigo
Antes de entregar codigo, validar:
- [ ] Respeta arquitectura por capas.
- [ ] Endpoints bajo `/v1/<recurso>`.
- [ ] DTOs completos y sin exponer entidades.
- [ ] Validaciones Bean Validation aplicadas.
- [ ] Mapper MapStruct implementado.
- [ ] Manejo de errores consistente.
- [ ] Sin secretos hardcodeados.
- [ ] Tests minimos incluidos.
- [ ] Compila y tests pasan.

## Plantilla de prompt recomendada para IA

```text
Genera codigo para ms-usuarios siguiendo arquitectura por capas:
controller -> service (contrato + impl) -> dao -> model -> mapper -> dto.

Reglas obligatorias:
1) Endpoints bajo /v1/<recurso> con ResponseEntity y codigos HTTP explicitos.
2) No exponer entidades JPA en responses; usar DTO + MapStruct.
3) Agregar Bean Validation en DTO request.
4) Usar excepciones de negocio y manejo centralizado con ControllerAdvice.
5) No hardcodear secretos ni loggear datos sensibles.
6) Incluir pruebas minimas de controller y service.
7) Mantener convenciones de nombres y paquetes del proyecto.
```

## Referencias internas
- `docs/CONTRIBUTING.md`
- `docs/ARCHITECTURE.md`
- `docs/README-STARTER.md`

