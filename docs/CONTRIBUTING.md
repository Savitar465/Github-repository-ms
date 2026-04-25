# Guía de Contribuciones - ms-usuarios

Gracias por interés en contribuir a `ms-usuarios`. Este documento define el proceso, estándares y expectativas.

## Tabla de contenidos
- [Código de conducta](#código-de-conducta)
- [Cómo empezar](#cómo-empezar)
- [Tipos de contribuciones](#tipos-de-contribuciones)
- [Proceso de desarrollo](#proceso-de-desarrollo)
- [Estándares de código](#estándares-de-código)
- [Pull Request](#pull-request)
- [Revisión de código](#revisión-de-código)

## Código de conducta
- Ser respetuoso y profesional
- Reportar bugs constructivamente
- Colaborar para resolver conflictos
- Enfoque en el beneficio del proyecto

## Cómo empezar

### 1. Fork y clona
```bash
git clone https://github.com/<tu-usuario>/ms-usuarios.git
cd ms-usuarios
git remote add upstream https://github.com/<org>/ms-usuarios.git
```

### 2. Crea rama de trabajo
```bash
git checkout -b feat/descripcion-breve
# o
git checkout -b fix/descripcion-breve
```

**Convención de nombres**:
- `feat/<descripcion>` - nueva funcionalidad
- `fix/<descripcion>` - corrección de bug
- `refactor/<descripcion>` - refactorización
- `docs/<descripcion>` - cambios de documentación
- `test/<descripcion>` - adición/mejora de tests

### 3. Configura local
```bash
./mvnw clean install
```

## Tipos de contribuciones

### Bugs (reportar)
Usar template: [.github/ISSUE_TEMPLATE/bug_report.md](../.github/ISSUE_TEMPLATE/bug_report.md)

Incluir:
- Descripción clara del problema
- Pasos para reproducir
- Comportamiento esperado vs. actual
- Logs/evidencia
- Ambiente (versión, entorno)

### Features (proponer)
Usar template: [.github/ISSUE_TEMPLATE/feature_request.md](../.github/ISSUE_TEMPLATE/feature_request.md)

Incluir:
- Objetivo y necesidad
- Alcance técnico
- Criterios de aceptación
- Riesgos y dependencias

### Mejoras (code review, refactor, docs)
Abrir issue para discusión antes de invertir tiempo significativo.

## Proceso de desarrollo

### Fase 1: Análisis
1. **Leer documentación**
   - [ARCHITECTURE.md](ARCHITECTURE.md) - arquitectura y patrones
   - Código similar existente

2. **Entender el cambio**
   - ¿Qué problema resuelve?
   - ¿Afecta seguridad, performance, contratos API?
   - ¿Hay dependencias con otros servicios?

### Fase 2: Implementación
1. **Respetar arquitectura**
   ```
   controller -> service (contrato + impl) -> dao -> model -> mapper -> dto
   ```

2. **Nomenclatura estandar**
   - `*Controller`, `*Service`, `*ServiceImpl`, `*Dao`, `*Mapper`
   - Paquetes por capa: `controller`, `service`, `dao`, `model`, `mapper`, `dto`

3. **Validaciones**
   - Bean Validation en DTOs request
   - Manejo de errores con `ControllerAdvice`
   - Excepciones específicas de negocio

4. **Tests**
   - Unitarios: lógica de servicio
   - Integración: controller + service
   - Cobertura mínima: 70%

### Fase 3: Calidad
1. **Compilar sin errores**
   ```bash
   ./mvnw clean compile -q
   ```

2. **Pasar todos los tests**
   ```bash
   ./mvnw test -q
   ```

3. **Revisar código**
   - Sin secretos/credenciales
   - Logs sin datos sensibles
   - Nombres claros y descriptivos
   - Evitar código duplicado

4. **Documentar**
   - OpenAPI annotations si es endpoint
   - Comments para lógica compleja
   - README si cambio estructura

## Estándares de código

### Java/Spring
- **Java**: versión 21+, modern syntax
- **Style guide**: Google Java Style / conventions existentes
- **Imports**: sin wildcard (`import com.inspire.*` ❌)

### Arquitectura
- **DTO en respuestas**: nunca exponer entidades JPA
- **Mapper**: usar MapStruct (no manual)
- **Validación**: Bean Validation, no if-based
- **Excepciones**: custom exceptions, no genéricas

### Naming
```java
// ❌ Evitar
public void process(String s) {}
UserEntity u = new UserEntity();

// ✅ Usar
public void processUserRegistration(String email) {}
Usuario usuario = new Usuario();
```

### Logging
```java
// ❌ Evitar
System.out.println("Usuario creado");
log.info("ok");

// ✅ Usar
log.info("Usuario creado con ID: {}", usuario.getId());
log.error("Error al crear usuario con email: {}", email, exception);
```

### Tests
```java
// ✅ Usar descriptive names
void debeCrearUsuarioConDatosValidos() {}
void debeLanzarExcepcionSiEmailYaExiste() {}

// ✅ Estructura AAA (Arrange-Act-Assert)
@Test
void debeObtenerUsuarioPorId() {
    // Arrange
    Usuario usuario = crearUsuarioTest();

    // Act
    UsuarioResponse resultado = usuarioService.obtener(usuario.getId());

    // Assert
    assertThat(resultado).isNotNull();
}
```

## Pull Request

### Antes de crear PR

1. **Sincronizar con main**
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Commits limpios**
   - Un commit por feature lógica
   - Mensajes descriptivos
   - No commits de merge innecesarios

3. **Usar template** (automático)
   - `.github/pull_request_template.md`
   - Rellenar todos los campos obligatorios

### Crear PR

```bash
git push origin feat/descripcion
```

Ir a GitHub, crear PR hacia `develop` o `main`:
- **Título**: `[feat/fix] descripción breve`
- **Descripción**: usar template
- **Labels**: asignar según tipo (feat, bug, refactor, docs)
- **Reviewers**: asignar compañeros
- **Linked issues**: si aplica

### Checklist de PR (obligatorio)
- [ ] **Compilación**: `mvnw clean package`
- [ ] **Tests**: `mvnw test` - todos pasan
- [ ] **Arquitectura**: respeta capas, naming, contratos
- [ ] **DTOs**: si es endpoint, nuevo DTO + mapper
- [ ] **Validaciones**: Bean Validation o manejo de errores
- [ ] **Documentación**: OpenAPI, README, código comentado si es necesario
- [ ] **Seguridad**: sin secretos, sin datos sensibles en logs
- [ ] **Sin TODOs**: código completado, no parcial

## Revisión de código

### Para reviewers (Do/Don't)

**Do**:
- Revisar arquitectura primero (capas, separación)
- Verificar seguridad (secretos, validaciones)
- Pedir tests y casos borde
- Confirmar DTOs + mappers
- Validar OpenAPI si cambia API

**Don't**:
- No aprobar sin contexto (criterios de aceptación)
- No mezclar refactor + funcionalidad (justificar)
- No aceptar secretos en código
- No ignorar logs insuficientes
- No comentarios vagos (indicar línea + motivo)

### Comentarios constructivos
```
❌ "Esto está mal"
✅ "En línea 45, falta validación de null. Usar @NotNull en DTO."

❌ "Hace falta testing"
✅ "Agregar test para caso where email ya existe. Usar debeThrowExceptionSiEmailDuplicado()"
```

### Aprobación
- ✅ Mínimo 1 aprobación antes de merge
- ✅ Todos los checks CI/CD deben pasar
- ✅ Resolver comentarios bloqueantes

## Después del merge

1. **Eliminar rama local**
   ```bash
   git checkout main
   git branch -d feat/descripcion
   ```

2. **Monitoreo**
   - Ver logs en staging/prod
   - Responder a issues/feedback

3. **Documentación**
   - Actualizar CHANGELOG si es necesario
   - Notificar a stakeholders si es cambio mayor

## Recursos útiles

- [ARCHITECTURE.md](ARCHITECTURE.md) - arquitectura y patrones
- [GUIA_IA_CODIFICACION.md](CODE-RULES.md) - reglas para generar nuevo codigo con IA
- [Spring Boot Best Practices](https://spring.io/guides)
- [OWASP Security Guidelines](https://owasp.org/)

## Preguntas frecuentes

**¿Cómo me comunico con el equipo?**
- GitHub Issues para bugs/features
- Pull Requests para código
- Slack `#ms-usuarios-dev` para discusiones

**¿Cuánto tiempo toma la revisión?**
- ~24-48 horas para feedback inicial
- Iteraciones según complejidad

**¿Hay restricciones de contenido?**
- No contribuir código de terceros sin licencia compatible
- Respetar GPL/MIT/Apache 2.0

---

**Gracias por contribuir a mejorar ms-usuarios** 🚀

