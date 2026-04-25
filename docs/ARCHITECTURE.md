# Plantilla estandar de estructura y arquitectura para microservicios

## 1) Objetivo
Este documento define un formato base reusable para construir microservicios con arquitectura por capas.
Sirve como guia de implementacion para equipos de desarrollo y como contexto para generacion asistida por IA.

## 2) Stack de referencia (adaptable)
- Java 21+
- Spring Boot 3.x
- Spring Data JPA
- Spring Security (OAuth2 Resource Server con JWT)
- OpenAPI (springdoc)
- MapStruct + Lombok
- Mensajeria asicrona (RabbitMQ/Kafka, opcional)
- Base de datos relacional (PostgreSQL/MySQL)
- Docker + Kubernetes

> Nota: este stack es una base sugerida. Ajustar segun el contexto del dominio y requerimientos no funcionales.

## 3) Estructura recomendada

```text
src/
  main/
    java/com/<organizacion>/<servicio>/
      config/
        security/
        amqp/               # opcional
      controller/
      dao/
      datasources/          # opcional (si hay mas de 1 datasource)
      dto/
        request/
        response/
      mapper/
      model/
      service/
        contratos/
        implementacion/
      util/
        errorhandling/
    resources/
      application.yaml
      application-<perfil>.yaml
      policy-enforcer-*.json  # opcional (Keycloak policy enforcer)
  test/
    java/com/<organizacion>/<servicio>/
```

## 4) Arquitectura aplicada

### 4.1 Capas
- `controller`: expone endpoints REST, valida entrada y delega al servicio.
- `service/contratos`: define interfaces de negocio.
- `service/implementacion`: implementa reglas de negocio y orquestacion.
- `dao`: acceso a datos con repositorios Spring Data.
- `model`: entidades del dominio persistente.
- `mapper`: transforma entidad <-> DTO (MapStruct).
- `dto/request|response`: contratos de entrada/salida de API.
- `config`: configuraciones transversales (security, openapi, datasource, mensajeria).
- `util/errorhandling`: manejo estandarizado de excepciones.

### 4.2 Flujo canonico de una peticion
1. `Controller` recibe request y aplica validaciones.
2. `Service` evalua reglas de negocio.
3. `DAO` persiste o consulta datos.
4. `Mapper` transforma a DTO de respuesta.
5. (Opcional) se publica evento de dominio.

### 4.3 Seguridad
- API stateless con JWT.
- Authorization por roles/permisos con anotaciones o policy enforcer.
- Conversion de claims del token a authorities en un converter dedicado.

### 4.4 Persistencia
- Caso simple: un datasource.
- Caso avanzado: multiples datasources con `EntityManagerFactory` y `TransactionManager` dedicados por contexto.

## 5) Convenciones para mantener consistencia

### 5.1 Nomenclatura
- `*Controller`
- `*Service` (interface)
- `*ServiceImpl` (implementacion)
- `*Dao`
- `*Mapper`
- `*Request` / `*Response`

### 5.2 Endpoints
- Versionado obligatorio: `/v1/<recurso>`.
- Usar `ResponseEntity` con codigos HTTP explicitos.
- Validaciones con Bean Validation (`@Valid`, `@NotNull`, `@Size`, etc.).

### 5.3 Manejo de errores
- Excepciones de negocio especificas (`EntityNotFoundException`, `EntityConflictException`, etc.).
- Manejo centralizado con `@ControllerAdvice`.
- Error response estable y consistente para consumidores.

### 5.4 DTO y mapeo
- Nunca exponer entidades JPA directamente en la API.
- Centralizar conversiones en MapStruct.

### 5.5 Configuracion y ambientes
- Separar configuracion por perfiles (`default`, `staging`, `prod`).
- Mantener compatibilidad con config externa (Config Server u otro mecanismo).

## 6) Buenas practicas recomendadas
- Separacion clara de responsabilidades por capa.
- Interfaces en servicios para facilitar pruebas y cambio de implementacion.
- Documentacion OpenAPI mantenida junto al codigo.
- Logging estructurado con contexto de transaccion/correlacion.
- Integraciones asincronas desacopladas por eventos.
- Principio de minimo privilegio en seguridad.

## 7) Reglas de seguridad y cumplimiento
- No versionar secretos ni credenciales.
- Consumir secretos via variables de entorno o Secret Manager.
- Definir politica de rotacion de credenciales.
- Aplicar chequeos de seguridad en CI/CD (SAST/dependencies scan).

## 8) Plantilla minima para nuevos modulos de dominio
Para crear un modulo `<Recurso>`:

1. DTOs:
   - `dto/request/<Recurso>Request.java`
   - `dto/response/<Recurso>Response.java`
2. Modelo y DAO:
   - `model/<Recurso>.java`
   - `dao/<Recurso>Dao.java`
3. Servicio:
   - `service/contratos/<Recurso>Service.java`
   - `service/implementacion/<Recurso>ServiceImpl.java`
4. Mapper:
   - `mapper/<Recurso>Mapper.java`
5. Controller:
   - `controller/<Recurso>Controller.java`
6. Pruebas:
   - `test/.../<Recurso>ControllerTest.java`

## 9) Guia para IA (prompt base)

```text
Genera codigo para un microservicio Spring Boot siguiendo esta arquitectura por capas:
controller -> service (contrato + impl) -> dao -> model -> mapper -> dto.

Reglas obligatorias:
1) Endpoints bajo /v1/<recurso>, con ResponseEntity y codigos HTTP explicitos.
2) No exponer entidades JPA en responses; usar DTO + MapStruct.
3) Agregar validaciones Bean Validation en requests.
4) Centralizar manejo de errores con ControllerAdvice.
5) Mantener nombres de paquetes por capa.
6) Si aplica seguridad, usar JWT y conversion de claims a authorities.
7) Si aplica mensajeria, publicar eventos desde la capa de servicio.
8) Agregar pruebas minimas de controller y service.
9) No hardcodear secretos; usar propiedades externas.
10) Seguir nomenclatura estandar de clases y paquetes.
```

## 10) Ejemplo canonico CRUD completo
Seccion de referencia para implementar un recurso `Producto`.

### 10.1 DTOs
```java
// dto/request/ProductoRequest.java
public record ProductoRequest(
        @jakarta.validation.constraints.NotBlank String nombre,
        @jakarta.validation.constraints.Positive java.math.BigDecimal precio
) {}
```

```java
// dto/response/ProductoResponse.java
public record ProductoResponse(Long id, String nombre, java.math.BigDecimal precio) {}
```

### 10.2 Entidad y DAO
```java
// model/Producto.java
@jakarta.persistence.Entity
@jakarta.persistence.Table(name = "producto")
@lombok.Getter @lombok.Setter @lombok.NoArgsConstructor @lombok.AllArgsConstructor @lombok.Builder
public class Producto {
    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Column(nullable = false, length = 120)
    private String nombre;

    @jakarta.persistence.Column(nullable = false)
    private java.math.BigDecimal precio;
}
```

```java
// dao/ProductoDao.java
public interface ProductoDao extends org.springframework.data.jpa.repository.JpaRepository<Producto, Long> {}
```

### 10.3 Mapper
```java
// mapper/ProductoMapper.java
@org.mapstruct.Mapper(componentModel = "spring")
public interface ProductoMapper {
    Producto toEntity(ProductoRequest request);
    ProductoResponse toResponse(Producto entity);
    void update(@org.mapstruct.MappingTarget Producto entity, ProductoRequest request);
}
```

### 10.4 Servicio
```java
// service/contratos/ProductoService.java
public interface ProductoService {
    ProductoResponse crear(ProductoRequest request);
    ProductoResponse obtener(Long id);
    org.springframework.data.domain.Page<ProductoResponse> listar(int page, int size);
    ProductoResponse actualizar(Long id, ProductoRequest request);
    void eliminar(Long id);
}
```

```java
// service/implementacion/ProductoServiceImpl.java
@org.springframework.stereotype.Service
@lombok.RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoDao productoDao;
    private final ProductoMapper productoMapper;

    @Override
    public ProductoResponse crear(ProductoRequest request) {
        Producto entity = productoMapper.toEntity(request);
        return productoMapper.toResponse(productoDao.save(entity));
    }

    @Override
    public ProductoResponse obtener(Long id) {
        Producto entity = productoDao.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Producto no encontrado: " + id));
        return productoMapper.toResponse(entity);
    }

    @Override
    public org.springframework.data.domain.Page<ProductoResponse> listar(int page, int size) {
        return productoDao.findAll(org.springframework.data.domain.PageRequest.of(page, size))
                .map(productoMapper::toResponse);
    }

    @Override
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto entity = productoDao.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Producto no encontrado: " + id));
        productoMapper.update(entity, request);
        return productoMapper.toResponse(productoDao.save(entity));
    }

    @Override
    public void eliminar(Long id) {
        if (!productoDao.existsById(id)) {
            throw new jakarta.persistence.EntityNotFoundException("Producto no encontrado: " + id);
        }
        productoDao.deleteById(id);
    }
}
```

### 10.5 Controller
```java
// controller/ProductoController.java
@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping("/v1/productos")
@lombok.RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @org.springframework.web.bind.annotation.PostMapping
    public org.springframework.http.ResponseEntity<ProductoResponse> crear(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody ProductoRequest request) {
        return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(productoService.crear(request));
    }

    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public org.springframework.http.ResponseEntity<ProductoResponse> obtener(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        return org.springframework.http.ResponseEntity.ok(productoService.obtener(id));
    }

    @org.springframework.web.bind.annotation.GetMapping
    public org.springframework.http.ResponseEntity<org.springframework.data.domain.Page<ProductoResponse>> listar(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        return org.springframework.http.ResponseEntity.ok(productoService.listar(page, size));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public org.springframework.http.ResponseEntity<ProductoResponse> actualizar(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody ProductoRequest request) {
        return org.springframework.http.ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<Void> eliminar(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        productoService.eliminar(id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
```

### 10.6 Test de controller
```java
// test/controller/ProductoControllerTest.java
@org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest(controllers = ProductoController.class)
class ProductoControllerTest {

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private ProductoService productoService;

    @org.junit.jupiter.api.Test
    void debeCrearProducto() throws Exception {
        ProductoResponse response = new ProductoResponse(1L, "Laptop", new java.math.BigDecimal("3500.00"));
        org.mockito.Mockito.when(productoService.crear(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/v1/productos")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Laptop\",\"precio\":3500.00}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.id").value(1));
    }
}
```

## 11) Checklist de calidad antes de merge
- Compila y pasa tests.
- Endpoints documentados en OpenAPI.
- Validaciones de entrada completas.
- DTO + Mapper implementados (sin exponer entidades).
- Excepciones de negocio correctamente mapeadas.
- Sin secretos en codigo ni YAML versionado.
- Logging y trazabilidad adecuados.

---

Esta plantilla define una base comun de arquitectura, implementacion y calidad para nuevos microservicios.
