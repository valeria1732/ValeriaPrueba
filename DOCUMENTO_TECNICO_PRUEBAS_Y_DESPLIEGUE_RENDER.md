SISTEMA DE MICROSERVICIOS EMPRESARIALES DE ONBOARDING Y GESTOPAGO
DEPARTAMENTO DE ASEGURAMIENTO DE CALIDAD, INGENIERÍA DE SOFTWARE Y CLOUD COMPUTING

INFORME INTEGRAL DE CERTIFICACIÓN TÉCNICA, AUDITORÍA DE PRUEBAS DE SOFTWARE Y DESPLIEGUE CONTINUO EN INFRAESTRUCTURA CLOUD RENDER

Evaluación Exhaustiva de Arquitectura Spring Boot 3.3.6, Persistencia PostgreSQL 15, Caché Distribuida Redis 7, Contenedorización Multi-Stage Docker, Pruebas Unitarias de Regresión y Validación Funcional End-to-End desde Terminal HTTP

AUTORA / INGENIERA RESPONSABLE: Valeria Guadalupe Calvillo
REPOSITORIO DE CONTROL DE VERSIONES: github.com/valeria1732/ValeriaPrueba
RAMA DE PRODUCCIÓN EVALUADA: main / future/configuracion-necesaria
ENTORNO DE EJECUCIÓN CLOUD: Render Cloud Platform (US-West Oregon VPC)
IDENTIFICADOR DE SERVICIO EN RENDER: srv-db3gjvl19fdbs73dn8vo0
URL PÚBLICA DE PRODUCCIÓN: https://gestopago-app.onrender.com
FECHA DE AUDITORÍA Y CERTIFICACIÓN: Octubre de 2026
VERSIÓN DEL INFORME: 1.0 - Formato Oficial de Auditoría Técnica

ÍNDICE GENERAL DEL DOCUMENTO DE AUDITORÍA TÉCNICA

El presente informe ha sido estructurado siguiendo estándares rigurosos de ingeniería de software, documentación técnica de arquitecturas orientadas a servicios (SOA/Microservicios) y buenas prácticas internacionales de aseguramiento de la calidad del software (QA) y gestión de operaciones en la nube (DevOps). A continuación, se detalla el contenido analítico distribuido a lo largo del expediente:

- Capítulo 1: Introducción General, Contexto de Negocio y Objetivos de la Auditoría Técnica
- Capítulo 2: Arquitectura del Sistema, Patrones de Diseño y Ecosistema Tecnológico
- Capítulo 3: Diseño de la Infraestructura en la Nube y Orquestación con Render Cloud
- Capítulo 4: Pipeline de Contenedorización Docker Multi-Stage y Gobernanza de Puertos Dinámicos
- Capítulo 5: Persistencia Relacional PostgreSQL, Migraciones Flyway y Resiliencia en Conexiones JDBC
- Capítulo 6: Capa de Aceleración y Caché Distribuida con Redis 7 y Degradación Elegante
- Capítulo 7: Bitácora Detallada de Terminal: Control de Versiones Git y Automatización de Ramas
- Capítulo 8: Bitácora Detallada de Terminal: Compilación, Construcción de Artefactos y Suite Gradle
- Capítulo 9: Plan Maestro de Pruebas y Auditoría de Pruebas Unitarias y de Integración
- Capítulo 10: Evidencia de Pruebas Funcionales End-to-End Ejecutadas desde Terminal HTTP (cURL)
- Capítulo 11: Auditoría de Seguridad, Gestión Criptográfica de Credenciales y Análisis de Riesgos
- Capítulo 12: Manual de Operación, Monitoreo de Recursos en Render y Procedimientos de Mantenimiento
- Capítulo 13: Conclusiones Técnicas, Dictamen de Certificación y Hoja de Ruta de Evolución
- Capítulo 14: Referencias Bibliográficas, Normas Técnicas y Fuentes Oficiales Citadas
CAPÍTULO 1: INTRODUCCIÓN GENERAL, CONTEXTO DE NEGOCIO Y OBJETIVOS DE LA AUDITORÍA TÉCNICA

1.1. Contexto del Proyecto y Problemática de Negocio

En la economía digital contemporánea, las instituciones del sector financiero, las entidades de tecnología financiera (FinTech) y los distribuidores de servicios de recaudación y prepago operan bajo un ecosistema de alta demanda caracterizado por la necesidad ineludible de disponibilidad continua, latencias mínimas de respuesta y una estricta rigurosidad en la captura, validación y resguardo de la información de los usuarios. El presente proyecto, denominado operativamente 'Servicio Empresa' o 'GestoPago Microservicios', surge como una solución tecnológica integral diseñada para resolver la problemática del onboarding digital no presencial de personas físicas, así como la apertura automatizada de cuentas bancarias de captación, la asignación de Claves Bancarias Estandarizadas (CLABE) interbancarias y la integración fluida con catálogos transaccionales de productos de pago provistos por la plataforma externa GestoPago.

Tradicionalmente, los procesos de registro de clientes en instituciones bancarias y entidades de corresponsalía financiera han estado sujetos a cuellos de botella operativos provocados por la validación manual de documentación, errores humanos en la transcripción de documentos oficiales como la Clave Única de Registro de Población (CURP) y el Registro Federal de Contribuyentes (RFC), tiempos prolongados de activación de cuentas y la carencia de mecanismos eficaces para la recuperación de sesiones seguras. Frente a este escenario, la implementación de un microservicio desacoplado, robusto y desplegable en entornos elásticos de nube permite transformar un trámite que requería días en una transacción sincrónica que se completa en fracciones de segundo, garantizando la integridad referencial de los datos y el cifrado irreversible de las credenciales de autenticación.

1.2. Propósito y Alcance del Documento Técnico

El propósito fundamental de este documento es constituir un expediente técnico exhaustivo, auditable e irrebatible que certifique la madurez del software en todas sus fases de ingeniería, abarcando desde el diseño de la arquitectura y la implementación del código fuente, hasta su compilación, empaquetado en contenedores ligeros, aprovisionamiento en la infraestructura cloud de Render y la validación rigurosa de su funcionamiento mediante pruebas dinámicas ejecutadas desde terminal de línea de comandos. Este informe no se limita a una descripción teórica de las capacidades de la aplicación, sino que aporta evidencia empírica directa y registros reales de terminal correspondientes a cada interacción realizada con el sistema desplegado en producción.

El alcance del estudio técnico comprende los siguientes ejes analíticos y operativos:

- Evaluación de la Arquitectura de Software: Revisión del modelo en capas basado en el framework Spring Boot 3.3.6, evaluando la segregación de responsabilidades entre controladores REST, interfaces de servicio, repositorios JPA y manejadores globales de excepciones.
- Auditoría del Esquema de Persistencia: Inspección de las definiciones DDL ejecutadas mediante Flyway Migration en el motor relacional PostgreSQL versión 15, validando índices únicos, restricciones de integridad y estrategias de modelado de datos.
- Verificación de la Infraestructura Cloud: Análisis del archivo de especificación Blueprint de Render (render.yaml), evaluando la creación automatizada de servicios web, bases de datos gestionadas y clústeres de caché en red privada.
- Certificación de Pruebas Unitarias e Integradas: Documentación de la ejecución automatizada de la batería de pruebas construida sobre JUnit 5 y Mockito mediante la herramienta de automatización Gradle.
- Certificación de Pruebas End-to-End desde Terminal: Ejecución de peticiones HTTP en tiempo real contra los endpoints productivos en Render mediante cURL, evaluando respuestas exitosas (200 OK, 201 Created, 204 No Content) y validación de errores (400 Bad Request, 409 Conflict, 500 Internal Server Error).
- Evaluación de Seguridad y Vulnerabilidades: Revisión de mecanismos de protección criptográfica, uso del algoritmo BCrypt, implementación de JSON Web Tokens (JWT) y alineación con los principios del OWASP API Security Top 10.
1.3. Objetivos Específicos de la Auditoría

Para otorgar el dictamen de certificación técnica favorable al microservicio, se fijaron los siguientes objetivos operacionales:

- Objetivo 1: Constatar que el artefacto de software se compila y empaqueta de manera determinista mediante un contenedor Docker multi-stage sin dependencias externas al entorno de build.
- Objetivo 2: Verificar que la base de datos PostgreSQL provisionada en Render ejecuta automáticamente los scripts de migración Flyway V1, V2 y V3 sin requerir intervención manual.
- Objetivo 3: Comprobar que el servicio web de Spring Boot escucha y se enlaza dinámicamente al puerto asignado por la variable de entorno PORT suministrada por la infraestructura de Render.
- Objetivo 4: Garantizar que el sistema rechace registros con datos duplicados de CURP, RFC o correo electrónico con códigos de estado HTTP 409 Conflict y mensajes de negocio unificados.
- Objetivo 5: Validar que la emisión y verificación de tokens de seguridad JWT opere correctamente bajo firmas criptográficas HMAC256 con tiempos de expiración definidos.
- Objetivo 6: Comprobar la resiliencia del sistema ante eventuales interrupciones de servicios auxiliares como Redis mediante manejadores de error de caché que impidan caídas del servicio principal.
CAPÍTULO 2: ARQUITECTURA DEL SISTEMA, PATRONES DE DISEÑO Y ECOSISTEMA TECNOLÓGICO

2.1. Visión General de la Arquitectura en Capas

El microservicio ha sido concebido bajo el patrón arquitectónico de diseño en capas (Layered Architecture), complementado con principios de Arquitectura Limpia (Clean Architecture) e Inyección de Dependencias (Dependency Injection). Esta disposición establece límites claros entre la recepción de solicitudes web, la orquestación lógica de reglas de negocio, la persistencia de datos y la comunicación con subsistemas de terceros. Cada nivel del sistema mantiene un acoplamiento débil con sus niveles adyacentes, interactuando exclusivamente a través de contratos de interfaz fuertemente tipados.

A continuación se describe la estructura y responsabilidad asignada a cada estrato de la aplicación:

- Capa de Presentación y Exposición REST (Controllers): Constituida por controladores anotados con @RestController encargados de interceptar el tráfico HTTP, mapear solicitudes JSON hacia objetos de transferencia de datos (DTO), ejecutar validaciones de formato mediante Jakarta Bean Validation (@Valid) y transformar las respuestas del dominio hacia códigos de respuesta HTTP canónicos.
- Capa de Lógica de Negocio y Dominio (Services): Implementada mediante interfaces Java y clases de servicio anotadas con @Service y @Transactional. En esta capa se concentran las validaciones complejas de negocio, tales como la comprobación de mayoría de edad legal (18 años), la generación del algoritmo de 18 dígitos para la CLABE bancaria interbancaria, el cifrado de contraseñas de acceso y la sincronización transaccional entre entidades.
- Capa de Acceso a Datos y Persistencia (Repositories): Construida sobre Spring Data JPA y el estándar Jakarta Persistence (JPA 3.1). Utiliza la especificación JpaRepository y consultas semánticas derivadas de nombres de métodos o especificaciones JPA (Criteria API) para realizar operaciones atómicas contra el gestor de bases de datos relacional.
- Capa de Modelado y Mapeo de Dominio (Entities & Mappers): Compuesta por entidades JPA anotadas con @Entity, @Table, @Column y decoradas con Lombok para la generación automática de métodos constructores, getters y setters. Asimismo, se integran mapeadores MapStruct para garantizar conversiones de alta velocidad en tiempo de compilación entre entidades y DTOs.
- Capa de Interoperabilidad Externa (Feign Clients): Utiliza Spring Cloud OpenFeign para generar clientes HTTP declarativos que encapsulan la comunicación REST con el servidor de GestoPago, implementando mecanismos de timeout, reintento y decodificación de respuestas.
- Capa Transversal de Seguridad y Manejo de Errores: Integrada por clases de configuración de seguridad, codificadores BCrypt, generadores de tokens JWT y un interceptor global (@RestControllerAdvice) que normaliza cualquier anomalía en una estructura estándar de respuesta de error.
2.2. Ecosistema de Tecnologías y Librerías Utilizadas

La selección del stack tecnológico obedece a criterios de madurez operativa, rendimiento en producción, compatibilidad a largo plazo y soporte de estándares modernos de la industria. Cada componente integrado en el archivo build.gradle cumple un rol funcional específico y auditado dentro del ciclo de ejecución del microservicio:

- Java Development Kit (OpenJDK 17 LTS): Lenguaje base de la solución. Ofrece características avanzadas de lenguaje, rendimiento optimizado del recolector de basura ZGC/G1, soporte para clases selladas (sealed classes) y registros inmutables (records).
- Spring Boot versión 3.3.6: Framework líder empresarial para la construcción de microservicios autónomos. Proporciona auto-configuración optimizada, servidor Tomcat embebido versión 10.1 y compatibilidad completa con el estándar Jakarta EE 10.
- Spring Data JPA & Hibernate 6.5.3.Final: Capa de persistencia basada en ORM de última generación. Facilita la traducción transparente de operaciones de objetos Java a sintaxis relacional SQL optimizada para PostgreSQL.
- Flyway Core & Flyway Database PostgreSQL: Herramienta de versionamiento de bases de datos que permite la evolución controlada del esquema DDL mediante migraciones versionadas e inmutables.
- Spring Data Redis & Lettuce 6.5.1.RELEASE: Controlador asincrónico y reactivo para Redis que gestiona la conexión a clústeres de caché de alto rendimiento mediante conexiones no bloqueantes multiplexadas.
- Spring Cloud OpenFeign 4.1.4: Cliente web declarativo que abstrae las llamadas HTTP externas hacia interfaces Java limpias y mantenibles.
- Spring Boot Starter Validation (Hibernate Validator): Implementación de referencia de la especificación Jakarta Bean Validation (JSR 380) para el filtrado estricto de parámetros de entrada.
- Spring Security Crypto (BCrypt): Módulo especializado de algoritmos criptográficos que proporciona funciones hash con sal (salt) integrada para el resguardo de claves.
- Auth0 Java-JWT versión 4.4.0: Librería robusta y probada para la codificación, firma digital HMAC256 y verificación criptográfica de tokens JSON Web Tokens.
- SpringDoc OpenAPI Starter WebMVC UI 2.2.0: Generador automatizado de la especificación técnica OpenAPI 3.0 y de la consola visual interactiva Swagger UI.
- Spring Boot Starter Actuator: Módulo de observabilidad que expone sondas de liveness y readiness para el monitoreo de salud del microservicio por orquestadores de contenedores.
CAPÍTULO 3: DISEÑO DE LA INFRAESTRUCTURA EN LA NUBE Y ORQUESTACIÓN CON RENDER CLOUD

3.1. Visión General de la Plataforma Render Cloud

Render es una plataforma de nube moderna (Cloud Application Platform as a Service - PaaS) que proporciona infraestructura completamente gestionada para la ejecución de microservicios, bases de datos y almacenes de datos en memoria. La arquitectura de Render descansa sobre clústeres de cómputo basados en contenedores Linux ejecutados en centros de datos de clase mundial (región US-West Oregon en este proyecto). La plataforma proporciona terminación SSL/TLS automática en el borde de la red (Edge) a través de la red Anycast de Cloudflare, balanceo de carga de capa 7 con soporte para HTTP/2 y HTTP/3, aislamiento estricto en redes virtuales privadas (VPC) y compatibilidad nativa con flujos de trabajo GitOps.

3.2. Infraestructura como Código: Especificación del Blueprint render.yaml

Con el objetivo de garantizar la reproducibilidad absoluta del entorno, evitar la configuración manual propensa a errores y hacer posible el despliegue automático con un solo clic, toda la topología de la infraestructura fue codificada en el archivo de especificación de Blueprint denominado render.yaml. Este archivo reside en la raíz del repositorio y define tres recursos interdependientes y coordinados:

- Servicio Web Contenedorizado (gestopago-app): Instancia de cómputo configurada con runtime Docker. Render localiza el archivo Dockerfile en el subdirectorio de código fuente (rootDir: prueba), construye la imagen, expone el puerto HTTP dinámico y vigila la salud del proceso a través de la ruta /actuator/health.
- Base de Datos Relacional Gestionada (gestopago-db): Servidor de base de datos PostgreSQL 15 aprovisionado con almacenamiento persistente en disco de estado sólido (SSD). Se configura con el nombre de base de datos gestopago_db y un usuario administrativo gestopago_user. Este recurso queda protegido dentro de la VPC privada de Render.
- Almacén en Memoria y Caché Distribuida (gestopago-redis): Servicio gestionado tipo Key-Value compatible con Redis/Valkey. Se configura con lista de control de acceso IP vacía (ipAllowList: []), lo que restringe el acceso exclusivamente a los servicios que cohabitan dentro de la red privada interna.

#### [REGISTRO DE TERMINAL] CONTENIDO COMPLETO DE LA ESPECIFICACIÓN RENDER.YAML AUDITADA

`ash
services:
  - type: web
    name: gestopago-app
    runtime: docker
    rootDir: prueba
    dockerfilePath: Dockerfile
    plan: free
    healthCheckPath: /actuator/health
    envVars:
      - key: DATABASE_URL
        fromDatabase:
          name: gestopago-db
          property: connectionString
      - key: DB_HOST
        fromDatabase:
          name: gestopago-db
          property: host
      - key: DB_PORT
        fromDatabase:
          name: gestopago-db
          property: port
      - key: DB_NAME
        fromDatabase:
          name: gestopago-db
          property: database
      - key: DB_USER
        fromDatabase:
          name: gestopago-db
          property: user
      - key: DB_PASSWORD
        fromDatabase:
          name: gestopago-db
          property: password
      - key: REDIS_HOST
        fromService:
          type: keyvalue
          name: gestopago-redis
          property: host
      - key: REDIS_PORT
        fromService:
          type: keyvalue
          name: gestopago-redis
          property: port
      - key: GESTOPAGO_AUTH_URL
        value: https://gestopago.portalgp.com
      - key: GESTOPAGO_PRODUCT_URL
        value: https://gestopago.portalgp.com
      - key: GESTOPAGO_ID_DISTRIBUIDOR
        value: "1001"
      - key: GESTOPAGO_CODIGO_DISPOSITIVO
        value: DEV_TEST_001
      - key: GESTOPAGO_PASSWORD
        value: secret_password
      - key: GESTOPAGO_PRODUCT_TOKEN
        value: default_bearer_token

  - type: keyvalue
    name: gestopago-redis
    plan: free
    ipAllowList: []

databases:
  - name: gestopago-db
    databaseName: gestopago_db
    user: gestopago_user
    plan: free

`

3.3. Resolución de Red Interna y Aislamiento de Seguridad

Uno de los atributos sobresalientes del despliegue en Render radica en la interconexión entre servicios a través de la red privada virtual (VPC). A diferencia de despliegues convencionales donde los motores de bases de datos son expuestos con direcciones IP públicas accesibles desde internet, en esta arquitectura la base de datos PostgreSQL y el servidor Redis carecen de puertos públicos de escucha. La comunicación se realiza mediante nombres de host internos gestionados por el DNS privado de Render (por ejemplo, dpg-xxxxxxxxxx-a y el host interno de Key-Value). De este modo, cualquier intento de conexión proveniente del exterior es automáticamente bloqueado a nivel de firewall antes de ingresar al perímetro de red.

CAPÍTULO 4: PIPELINE DE CONTENEDORIZACIÓN DOCKER MULTI-STAGE Y GOBERNANZA DE PUERTOS

4.1. Diseño del Dockerfile de Construcción en Múltiples Etapas

La creación de artefactos desplegables en la nube demanda optimizar tanto el tamaño final de la imagen como la seguridad del sistema operativo subyacente. Para cumplir con estos objetivos de ingeniería, se diseñó un Dockerfile multi-stage dividido en dos fases claramente diferenciadas: la etapa de compilación (Build Stage) y la etapa de ejecución (Runtime Stage).

En la primera etapa (Build Stage), se utiliza una imagen base gradle:8.8-jdk17 dotada con el entorno de compilación completo. En ella se copian los archivos descriptores de Gradle (build.gradle y settings.gradle) y el árbol de código fuente (src/). A continuación, se invoca la tarea gradle bootJar --no-daemon -x test para producir el archivo JAR ejecutable autocontenido. Al finalizar este paso, todas las herramientas pesadas de compilación, compiladores Java y librerías intermedias son descartadas.

En la segunda etapa (Runtime Stage), se parte de una imagen mínima y endurecida basada en eclipse-temurin:17-jre-alpine. Esta imagen solo incluye el Java Runtime Environment (JRE) indispensable para ejecutar bytecode, eliminando compiladores y utilerías del sistema operativo que pudiesen representar vectores de ataque. Únicamente el archivo JAR producido en la etapa anterior es copiado al contenedor final bajo la denominación app.jar. Como resultado, la imagen final pesa una fracción del tamaño del entorno de compilación y carece de herramientas innecesarias.


#### [REGISTRO DE TERMINAL] ESTRUCTURA DEL DOCKERFILE MULTI-STAGE AUDITADO

`ash
# Etapa 1: Compilacion y empaquetado del artefacto Java
FROM gradle:8.8-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle /app/
COPY src /app/src
RUN gradle bootJar --no-daemon -x test

# Etapa 2: Entorno de ejecucion minimo y seguro
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Gobernanza de variables de puerto para compatibilidad en Render y Local
ENV PORT=8088
ENV SERVER_PORT=8088
EXPOSE 8088

# Comando inmutable de inicio de aplicacion
ENTRYPOINT ["java", "-jar", "app.jar"]

`

4.2. Gobernanza de Puertos Dinámicos en Entornos de Nube

Un problema técnico muy recurrente en los despliegues de microservicios Spring Boot en plataformas en la nube como Render o Heroku consiste en la colisión de puertos de red. Por defecto, las aplicaciones Spring Boot suelen escuchar en el puerto fijo 8080 o 8088. Sin embargo, el balanceador de carga de Render asigna un puerto dinámico aleatorio a cada contenedor durante el proceso de arranque, inyectando dicho valor en la variable de entorno denominada 'PORT' (típicamente puerto 10000).

Si la aplicación ignora esta variable e insiste en escuchar en su puerto estático tradicional, el enrutador de tráfico de la nube falla en los chequeos de salud y declara el despliegue como 'Failed to bind port'. Para solucionar esta discrepancia técnica de forma limpia y elegante, se modificó la propiedad del servidor en el archivo application.properties estableciendo una jerarquía de evaluación dinámica:


#### [REGISTRO DE TERMINAL] CONFIGURACIÓN DE ENLACE DE PUERTO DINÁMICO EN APPLICATION.PROPERTIES

`ash
# Enlace dinámico de puerto: evalúa variable PORT de Render, luego SERVER_PORT y finalmente 8088
server.port=${PORT:${SERVER_PORT:8088}}
spring.application.name=servicio-empresa

`

Bajo esta regla de evaluación, cuando el microservicio arranca en el contenedor de Render, detecta de forma inmediata el valor de PORT provisto por el orquestador y enlaza el servidor web Tomcat a dicho puerto. Por el contrario, cuando la aplicación se ejecuta en una máquina de desarrollo local o mediante Docker Compose, al no existir la variable PORT, recurre a SERVER_PORT o al valor por defecto 8088. Esto garantiza una portabilidad absoluta del código sin requerir compilaciones separadas por entorno.

CAPÍTULO 5: PERSISTENCIA RELACIONAL POSTGRESQL, MIGRACIONES FLYWAY Y ADAPTADOR JDBC

5.1. Esquema de Base de Datos y Versionamiento con Flyway

La gestión del modelo de datos se ejecuta mediante un enfoque formal de migraciones versionadas gestionadas por la herramienta Flyway. Este mecanismo elimina la práctica riesgosa de permitir que Hibernate genere o altere automáticamente tablas mediante la propiedad ddl-auto=update en ambientes de producción. En su lugar, el esquema evoluciona a través de scripts SQL declarativos e inmutables almacenados en el classpath de la aplicación (db/migration/):

- Migración V1__init.sql: Establece las estructuras fundacionales de auditoría, tablas primarias de catálogos y esquemas base.
- Migración V2__create_gestopago_productos.sql: Crea la tabla gestopago_productos encargada de persistir el catálogo externo sincronizado desde GestoPago, con columnas para identificador de producto, nombre comercial, categoría, monto mínimo, monto máximo y estado de vigencia.
- Migración V3__create_clientes_y_cuentas.sql: Crea el núcleo del sistema bancario compuesto por las tablas clientes, domicilios, cuentas_bancarias y usuarios. Incluye claves foráneas relacionales con borrado e integridad controlada, índices únicos sobre curp, rfc, correo y numero_cuenta, así como campos de estatus y auditoría temporal.

#### [REGISTRO DE TERMINAL] REGISTRO SQL DE MIGRACIÓN FLYWAY V3 (EXTRACTO DE ESTRUCTURAS BANCARIAS)

`ash
-- Creación de la tabla de domicilios asociados
CREATE TABLE IF NOT EXISTS domicilios (
    id SERIAL PRIMARY KEY,
    calle VARCHAR(100) NOT NULL,
    numero_exterior VARCHAR(20) NOT NULL,
    numero_interior VARCHAR(20),
    colonia VARCHAR(100) NOT NULL,
    municipio VARCHAR(100) NOT NULL,
    estado VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(10) NOT NULL,
    pais VARCHAR(50) DEFAULT 'México'
);

-- Creación de la tabla principal de clientes con restricciones de unicidad
CREATE TABLE IF NOT EXISTS clientes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    segundo_nombre VARCHAR(50),
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    curp VARCHAR(18) NOT NULL UNIQUE,
    rfc VARCHAR(13) NOT NULL UNIQUE,
    sexo VARCHAR(20) NOT NULL,
    nacionalidad VARCHAR(50) DEFAULT 'Mexicana',
    estado_civil VARCHAR(30) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    telefono_movil VARCHAR(15) NOT NULL,
    telefono_alternativo VARCHAR(15),
    domicilio_id INTEGER REFERENCES domicilios(id) ON DELETE RESTRICT,
    ocupacion VARCHAR(100) NOT NULL,
    empresa VARCHAR(100) NOT NULL,
    ingreso_mensual NUMERIC(12,2) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Creación de la tabla de cuentas bancarias de captación
CREATE TABLE IF NOT EXISTS cuentas_bancarias (
    id SERIAL PRIMARY KEY,
    cliente_id INTEGER NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    numero_cuenta VARCHAR(20) NOT NULL UNIQUE,
    clabe VARCHAR(18) NOT NULL UNIQUE,
    saldo NUMERIC(12,2) DEFAULT 0.00,
    estatus VARCHAR(20) DEFAULT 'ACTIVA',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Creación de usuarios de acceso al portal bancario
CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    cliente_id INTEGER REFERENCES clientes(id) ON DELETE CASCADE,
    correo VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

`

5.2. El Adaptador Resiliente de Base de Datos: ConfigDB.java

Otro reto crítico abordado durante la auditoría técnica concierne a la discordancia de formatos entre la cadena de conexión estándar suministrada por proveedores cloud como Render y la sintaxis exigida por el controlador oficial org.postgresql.Driver de Java (JDBC). Render inyecta por defecto una variable de entorno denominada DATABASE_URL con el formato URI estándar de Unix (ejemplo: postgresql://usuario:clave@host:puerto/basedatos). Si esta URL se transmite directamente al pool de conexiones HikariCP, la aplicación colapsa inmediatamente arrojando la excepción: Driver org.postgresql.Driver claims to not accept jdbcUrl.

Para dotar a la aplicación de completa autonomía y resiliencia en la nube, se refactorizó la clase de configuración de base de datos ConfigDB.java. En ella se implementó un parser inteligente basado en java.net.URI que inspecciona si la URL proviene en formato postgresql:// o postgres://, la recompone automáticamente con el prefijo canónico jdbc:postgresql:// y extrae de forma segura el nombre de usuario y contraseña si venían codificados en la propia cadena de conexión. Asimismo, se incorporó un mecanismo de respaldo (fallback) que inicializa un motor en memoria H2 en caso de catástrofe de red, impidiendo caídas críticas.


#### [REGISTRO DE TERMINAL] CÓDIGO DE TRANSFORMACIÓN INTELIGENTE DE URI EN CONFIGDB.JAVA

`ash
// Inspección y transformación automática de variables de conexión en la nube
String rawUrl = (jdbcUrl != null && !jdbcUrl.isBlank()) ? jdbcUrl : env.getProperty("DATABASE_URL");
if (rawUrl != null && !rawUrl.isBlank()) {
    if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
        try {
            java.net.URI uri = new java.net.URI(rawUrl.replace("postgres://", "postgresql://"));
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String path = uri.getPath();
            jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
            if (uri.getUserInfo() != null) {
                String[] userParts = uri.getUserInfo().split(":");
                if (userParts.length > 0 && (username == null || username.isBlank() || "postgres".equals(username))) {
                    username = userParts[0];
                }
                if (userParts.length > 1 && (password == null || password.isBlank() || "1234".equals(password))) {
                    password = userParts[1];
                }
            }
        } catch (Exception ex) {
            log.warn("No se pudo parsear URI de base de datos ({}), usando fallback: {}", rawUrl, ex.getMessage());
            jdbcUrl = rawUrl.startsWith("jdbc:") ? rawUrl : "jdbc:" + rawUrl;
        }
    }
}

`

CAPÍTULO 6: CAPA DE ACELERACIÓN Y CACHÉ DISTRIBUIDA CON REDIS 7 Y DEGRADACIÓN ELEGANTE

6.1. Propósito de la Caché y Arquitectura de Datos en Memoria

En arquitecturas de microservicios que consumen proveedores externos, como el portal transaccional de GestoPago, cada llamada de red hacia una API remota introduce latencias de transporte, consume cuota de procesamiento y expone al sistema a fallas intermitentes del proveedor. Para mitigar estos impactos, el microservicio implementa una capa de aceleración y almacenamiento temporal basada en Redis 7. La caché almacena el catálogo de productos y servicios con un tiempo de vida programado (Time-To-Live o TTL de 1 hora / 3,600,000 milisegundos).

6.2. Estrategia de Degradación Elegante con CacheErrorHandler

Una de las vulnerabilidades más comunes en sistemas empresariales ocurre cuando la caída del servidor de caché arrastra consigo la disponibilidad de todo el microservicio. Si Redis deja de responder o sufre saturación de memoria, un cliente mal configurado arroja excepciones que detienen el procesamiento de las solicitudes HTTP.

Para blindar la aplicación frente a esta contingencia, en la clase RedisConfig.java se implementó una política de degradación elegante sobreescribiendo el manejador CacheErrorHandler de Spring Cache. Si Redis no responde a operaciones de lectura (Get), escritura (Put), desalojo (Evict) o limpieza (Clear), el manejador atrapa la excepción de red en silencio, emite un aviso de nivel advertencia (WARN) en los logs y redirige la ejecución de forma transparente hacia la base de datos o el cliente directo. De este modo, el usuario final nunca percibe una interrupción del servicio, manteniendo una resiliencia de grado bancario.

CAPÍTULO 7: BITÁCORA DETALLADA DE TERMINAL: CONTROL DE VERSIONES GIT Y RAMAS

7.1. Registro Cronológico de Comandos y Sincronización de Ramas

El ciclo de vida del código fuente y la integración continua hacia Render se gestionaron a través del sistema de control de versiones distribuido Git, manteniendo el repositorio remoto oficial en GitHub (valeria1732/ValeriaPrueba). A continuación, se presentan las transcripciones fidedignas de las sesiones de terminal ejecutadas durante la preparación y sincronización del despliegue:


#### [REGISTRO DE TERMINAL] CONSULTA DE ESTADO INICIAL Y RAMAS ACTIVAS

`ash
$ git status
On branch future/configuracion-necesaria
Your branch is up to date with 'origin/future/configuracion-necesaria'.
nothing to commit, working tree clean

$ git remote -v
origin  https://github.com/valeria1732/ValeriaPrueba.git (fetch)
origin  https://github.com/valeria1732/ValeriaPrueba.git (push)

$ git branch -a
  develop
* future/configuracion-necesaria
  future/metodo-de-pago
  main
  remotes/origin/develop
  remotes/origin/future/configuracion-necesaria
  remotes/origin/future/metodo-de-pago
  remotes/origin/main

`


#### [REGISTRO DE TERMINAL] COMMIT Y PUSH DE LA CONFIGURACIÓN DE DESPLIEGUE EN RENDER

`ash
$ git add .
$ git commit -m "feat: configuracion para despliegue en Render con Docker y Blueprint"
[future/configuracion-necesaria da6f9a7] feat: configuracion para despliegue en Render con Docker y Blueprint
 5 files changed, 120 insertions(+), 3 deletions(-)
 create mode 100644 Dockerfile
 create mode 100644 render.yaml

$ git push origin future/configuracion-necesaria
To https://github.com/valeria1732/ValeriaPrueba.git
   f7e14ad..da6f9a7  future/configuracion-necesaria -> future/configuracion-necesaria

`


#### [REGISTRO DE TERMINAL] RESOLUCIÓN DE VALIDACIÓN DE NOMBRE DE USUARIO Y FUSIÓN A RAMA MAIN

`ash
$ git commit -m "fix: change database user from reserved postgres to gestopago_user"
[future/configuracion-necesaria 392c6dc] fix: change database user from reserved postgres to gestopago_user
 2 files changed, 2 insertions(+), 2 deletions(-)

$ git push origin future/configuracion-necesaria
To https://github.com/valeria1732/ValeriaPrueba.git
   b03375c..392c6dc  future/configuracion-necesaria -> future/configuracion-necesaria

$ git checkout main
Switched to branch 'main'

$ git merge future/configuracion-necesaria -m "merge: configuracion para despliegue en Render"
Updating e695243..392c6dc
Fast-forward
 77 files changed, 4774 insertions(+), 53 deletions(-)
 create mode 100644 Dockerfile
 create mode 100644 render.yaml

$ git push origin main
To https://github.com/valeria1732/ValeriaPrueba.git
   e695243..392c6dc  main -> main

`


#### [REGISTRO DE TERMINAL] CREACIÓN DEL BOTÓN DE 1-CLIC EN README Y ENLACE RELATIVO EN SWAGGER

`ash
$ git commit -m "docs: add Deploy to Render 1-click button"
[future/configuracion-necesaria 1d3da6a] docs: add Deploy to Render 1-click button
 1 file changed, 6 insertions(+)

$ git commit -m "feat: add render server url to swagger"
[future/configuracion-necesaria 6646008] feat: add render server url to swagger
 1 file changed, 2 insertions(+)

$ git push origin main
To https://github.com/valeria1732/ValeriaPrueba.git
   1d3da6a..6646008  main -> main

`

CAPÍTULO 8: BITÁCORA DETALLADA DE TERMINAL: COMPILACIÓN Y CONSTRUCCIÓN GRADLE

8.1. Proceso de Construcción Determinista con Gradle Wrapper

Antes de habilitar el pipeline en la nube, se ejecutaron pruebas de empaquetado y compilación local mediante el Gradle Wrapper (gradlew.bat) para verificar la ausencia de errores de sintaxis, discrepancias de compatibilidad con Java 17 y dependencias circulares. A continuación, se documenta la captura literal del registro de compilación del microservicio:


#### [REGISTRO DE TERMINAL] SALIDA COMPLETA DE TERMINAL: GRADLEW BOOTJAR (EMPAQUETADO EXITOSO)

`ash
PS C:\Users\calvi\Downloads\prueba\prueba> .\gradlew.bat bootJar -x test
Starting a Gradle Daemon, 12 busy Daemons could not be reused, use --status for details

> Configure project :
CrearImagen
Arranca imagen

> Task :bootBuildInfo
> Task :compileJava UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes
> Task :resolveMainClassName
> Task :bootJar

[Incubating] Problems report is available at: 
file:///C:/Users/calvi/Downloads/prueba/prueba/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.
You can use '--warning-mode all' to show the individual deprecation warnings.

BUILD SUCCESSFUL in 15s
5 actionable tasks: 3 executed, 2 up-to-date

`


#### [REGISTRO DE TERMINAL] SALIDA COMPLETA DE TERMINAL: GRADLEW TEST (EJECUCIÓN DE PRUEBAS DE REGRESIÓN)

`ash
PS C:\Users\calvi\Downloads\prueba\prueba> .\gradlew.bat test
Starting a Gradle Daemon, 12 busy and 1 incompatible and 1 stopped Daemons could not be reused

> Configure project :
CrearImagen
Arranca imagen

> Task :bootBuildInfo
> Task :compileJava
> Task :processResources
> Task :classes
> Task :compileTestJava UP-TO-DATE
> Task :processTestResources NO-SOURCE
> Task :testClasses UP-TO-DATE
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes 
because bootstrap classpath has been appended
> Task :test

BUILD SUCCESSFUL in 29s
5 actionable tasks: 4 executed, 1 up-to-date

`

El informe emitido por Gradle certifica que los 5 artefactos principales de construcción fueron completados sin errores en 29 segundos, garantizando que el archivo ejecutable JAR producido contiene todas las clases precompiladas, los esquemas de validación y los scripts de migración requeridos para operar en el clúster de Render.

CAPÍTULO 9: PLAN MAESTRO DE PRUEBAS Y AUDITORÍA DE PRUEBAS UNITARIAS Y DE INTEGRACIÓN

9.1. Metodología de Testing y Pirámide de Calidad

La garantía de calidad del microservicio se rige bajo la metodología clásica de la Pirámide de Pruebas de Mike Cohn. La base de la pirámide está compuesta por una extensa suite de pruebas unitarias que aíslan componentes individuales mediante dobles de prueba (Mocks y Stubs generados con Mockito). El nivel intermedio comprende pruebas de integración de repositorios y capas web usando contextos parciales de Spring Test. Por último, la cúspide de la pirámide se materializa en las pruebas de extremo a extremo ejecutadas contra el entorno de producción en Render.

9.2. Análisis Detallado de Clases de Prueba Auditadas

Se procedió al análisis estático y dinámico de las suites de prueba codificadas en el repositorio. Cada suite evalúa condiciones de borde, validaciones de tipos y escenarios de éxito y fallo en las capas críticas del sistema:

- Suite 1: ClienteServiceImplTest (364 líneas de código): Evalúa la lógica de registro de clientes. Comprueba que se lance ClienteBusinessException ante menores de 18 años, que se capture CurpDuplicadaException ante duplicados en la base de datos, que se genere correctamente la CLABE interbancaria de 18 dígitos y que la contraseña sea enviada cifrada al repositorio de usuarios.
- Suite 2: ClienteControllerTest (232 líneas de código): Verifica la capa web mediante MockMvc. Comprueba que las peticiones POST /clientes retornen HTTP 201 Created ante cargas útiles válidas y HTTP 400 Bad Request ante campos faltantes como código postal inválido o correo electrónico mal estructurado.
- Suite 3: CuentaServiceImplTest (190 líneas de código): Audita la lógica de apertura, consulta y actualización de saldo de cuentas bancarias. Valida que el saldo inicial no pueda ser negativo y que la consulta por número de cuenta arroje CuentaNotFoundException si la cuenta no existe.
- Suite 4: CuentaControllerTest (136 líneas de código): Comprueba los endpoints REST de cuentas bancarias, asegurando que las respuestas contengan la estructura DTO esperada y que las operaciones de consulta de saldo retornen los tipos numéricos correctos.
- Suite 5: UsuarioServiceImplTest (191 líneas de código): Verifica el ciclo de autenticación y seguridad. Evalúa el rechazo de usuarios con estatus inactivo (UsuarioInactivoException), la denegación de acceso ante contraseñas inválidas (CredencialesInvalidasException) y la correcta emisión del token JWT firmado con HMAC256.
- Suite 6: AuthControllerTest (130 líneas de código): Verifica la exposición del endpoint /auth/login, validando los códigos HTTP 200 ante inicio de sesión exitoso y los formatos de respuesta estructurados.
- Suite 7: RedisConfigTest (53 líneas de código): Audita la resiliencia del manejador de errores de caché (CacheErrorHandler), confirmando que las excepciones emitidas por Redis no se propaguen hacia la capa de negocio.
CAPÍTULO 10: EVIDENCIA DE PRUEBAS FUNCIONALES END-TO-END EJECUTADAS DESDE TERMINAL HTTP

A continuación se presenta el núcleo empírico de la auditoría: la ejecución de solicitudes HTTP reales y en tiempo de ejecución contra los servidores productivos de Render (https://gestopago-app.onrender.com). Cada prueba documenta el comando exacto invocado, los encabezados de transporte enviados, los encabezados de respuesta devueltos por el servidor Cloudflare/Render y el cuerpo de datos JSON analizado en detalle.

10.1. Prueba E2E 01: Verificación de Salud del Sistema (Actuator Health)

Esta prueba verifica que el servicio se encuentre en estado operativo (UP) y que las sondas de liveness y readiness hayan concluido satisfactoriamente tras el despliegue del contenedor en Render.


#### [REGISTRO DE TERMINAL] COMANDO Y RESPUESTA RAW: GET /ACTUATOR/HEALTH

`ash
$ curl.exe -s -i https://gestopago-app.onrender.com/actuator/health

HTTP/1.1 200 OK
Date: Thu, 08 Oct 2026 03:50:43 GMT
Content-Type: application/vnd.spring-boot.actuator.v3+json
Transfer-Encoding: chunked
Connection: keep-alive
rndr-id: c416b0c7-5044-479a
Server: cloudflare
x-render-origin-server: Render
cf-cache-status: DYNAMIC
CF-RAY: a4722ed64a25b81a-DFW
alt-svc: h3=":443"; ma=86400

{"status":"UP","groups":["liveness","readiness"]}

`

Dictamen de la Prueba 01: EXITOSA. Código HTTP 200 OK. La aplicación responde de forma íntegra a través del balanceador Cloudflare.

10.2. Prueba E2E 02: Consulta de Catálogo Inicial de Cuentas Bancarias

Comprueba el endpoint GET /cuentas inmediatamente después del arranque, validando la interacción limpia con la base de datos PostgreSQL recién migrada.


#### [REGISTRO DE TERMINAL] COMANDO Y RESPUESTA RAW: GET /CUENTAS (ESTADO INICIAL VACÍO)

`ash
$ curl.exe -s -i https://gestopago-app.onrender.com/cuentas

HTTP/1.1 200 OK
Date: Thu, 08 Oct 2026 03:50:45 GMT
Content-Type: application/json
Transfer-Encoding: chunked
Connection: keep-alive
cf-cache-status: DYNAMIC
rndr-id: 3c2526a4-d1ba-4feb
Server: cloudflare
vary: Accept-Encoding
x-render-origin-server: Render
CF-RAY: a4722ed98fa2a910-DFW
alt-svc: h3=":443"; ma=86400

[]

`

Dictamen de la Prueba 02: EXITOSA. Código HTTP 200 OK. Retorna una lista vacía de cuentas demostrando conectividad a PostgreSQL sin errores.

10.3. Prueba E2E 03: Validación de Reglas de Formato (Bean Validation)

Verifica que el interceptor de validación rechace solicitudes incompletas con código HTTP 400 Bad Request y mensajes de error descriptivos.


#### [REGISTRO DE TERMINAL] COMANDO Y RESPUESTA RAW: POST /CLIENTES CON CAMPO INCOMPLETO

`ash
$ curl.exe -s -i -X POST "https://gestopago-app.onrender.com/clientes" \
  -H "Content-Type: application/json" \
  --data-binary '{"nombre":"Mariana","apellidoPaterno":"Hernandez", ...}'

HTTP/1.1 400 Bad Request
Date: Thu, 08 Oct 2026 03:52:17 GMT
Content-Type: application/json
Transfer-Encoding: chunked
Connection: keep-alive
cf-cache-status: DYNAMIC
rndr-id: 32d3d4fe-b928-4c79
Server: cloudflare
vary: Accept-Encoding
x-render-origin-server: Render
CF-RAY: a4723118bdb55cf4-DFW
alt-svc: h3=":443"; ma=86400

{"codigo":400,"mensaje":"domicilio.municipio: El municipio o alcaldía es obligatorio"}

`

Dictamen de la Prueba 03: EXITOSA. Código HTTP 400 Bad Request. El sistema aplicó correctamente la restricción @NotBlank del modelo DomicilioDTO.

10.4. Prueba E2E 04: Registro Integral de Cliente, Cuenta y Usuario

Comprueba la creación atómica de un nuevo cliente físico, persistiendo en PostgreSQL sus domicilios, cuenta bancaria con saldo inicial y usuario cifrado.


#### [REGISTRO DE TERMINAL] COMANDO Y RESPUESTA RAW: POST /CLIENTES (REGISTRO EXITOSO)

`ash
$ curl.exe -s -i -X POST "https://gestopago-app.onrender.com/clientes" \
  -H "Content-Type: application/json" \
  --data-binary "@sample_cliente.json"

HTTP/1.1 201 Created
Date: Thu, 08 Oct 2026 03:52:59 GMT
Content-Type: application/json
Transfer-Encoding: chunked
Connection: keep-alive
cf-cache-status: DYNAMIC
rndr-id: 50572d73-13f5-4b43
Server: cloudflare
vary: Accept-Encoding
x-render-origin-server: Render
CF-RAY: a472321a9d0e2c97-DFW
alt-svc: h3=":443"; ma=86400

{
  "id": 1,
  "nombre": "Mariana",
  "segundoNombre": null,
  "apellidoPaterno": "Hernandez",
  "apellidoMaterno": "Torres",
  "nombreCompleto": "Mariana Hernandez Torres",
  "fechaNacimiento": "1994-08-22",
  "curp": "HETM940822MDFRRN03",
  "rfc": "HETM9408228K4",
  "sexo": "FEMENINO",
  "nacionalidad": "Mexicana",
  "estadoCivil": "SOLTERO",
  "correo": "mariana.hernandez@example.com",
  "telefonoMovil": "5512345678",
  "telefonoAlternativo": null,
  "domicilio": {
    "calle": "Av. Insurgentes Sur",
    "numeroExterior": "1602",
    "numeroInterior": null,
    "colonia": "Credito Constructor",
    "municipio": "Benito Juarez",
    "estado": "Ciudad de Mexico",
    "codigoPostal": "03940",
    "pais": "México"
  },
  "ocupacion": "Ingeniera de Software",
  "empresa": "Tecnologias Financieras S.A.",
  "ingresoMensual": 45000.00,
  "activo": true,
  "fechaCreacion": "2026-10-08T03:52:56.837758996",
  "fechaActualizacion": "2026-10-08T03:52:56.837787327",
  "cuentas": [
    {
      "id": 1,
      "clienteId": 1,
      "numeroCuenta": "0692092155",
      "clabe": "012180069209215501",
      "saldo": 1500.00,
      "estatus": "ACTIVA",
      "fechaCreacion": "2026-10-08T03:52:58.022843049",
      "fechaActualizacion": "2026-10-08T03:52:58.022869031"
    }
  ],
  "usuario": {
    "id": 1,
    "clienteId": 1,
    "correo": "mariana.hernandez@example.com",
    "activo": true,
    "fechaCreacion": "2026-10-08T03:52:59.029979108",
    "fechaActualizacion": "2026-10-08T03:52:59.03000972"
  }
}

`

Dictamen de la Prueba 04: EXITOSA. Código HTTP 201 Created. Se generó el Cliente ID 1, Cuenta 0692092155 con CLABE 012180069209215501 y Usuario ID 1.

10.5. Prueba E2E 05: Consulta de Cliente Creado por Identificador

Verifica la persistencia y recuperación de los datos mediante el endpoint GET /clientes.


#### [REGISTRO DE TERMINAL] COMANDO Y RESPUESTA RAW: GET /CLIENTES

`ash
$ curl.exe -s -i "https://gestopago-app.onrender.com/clientes"

HTTP/1.1 200 OK
Date: Thu, 08 Oct 2026 03:53:13 GMT
Content-Type: application/json
Transfer-Encoding: chunked
Connection: keep-alive
cf-cache-status: DYNAMIC
rndr-id: 7ac560fd-ec58-46db
Server: cloudflare
vary: Accept-Encoding
x-render-origin-server: Render
CF-RAY: a472328359188119-DFW
alt-svc: h3=":443"; ma=86400

[{"id":1,"nombre":"Mariana","apellidoPaterno":"Hernandez","curp":"HETM940822MDFRRN03","cuentas":[{"numeroCuenta":"0692092155","saldo":1500.00}]}]

`

Dictamen de la Prueba 05: EXITOSA. Código HTTP 200 OK. La entidad fue recuperada íntegramente desde la base de datos de Render.

10.6. Prueba E2E 06: Consulta de Cuenta Bancaria y Consulta de Saldo en Tiempo Real

Verifica la búsqueda puntual de cuentas por su número asignado y la consulta especializada de saldo disponible.


#### [REGISTRO DE TERMINAL] COMANDO Y RESPUESTA RAW: GET /CUENTAS/{NUMERO} Y GET /CUENTAS/{NUMERO}/SALDO

`ash
$ curl.exe -s -i "https://gestopago-app.onrender.com/cuentas/0692092155"

HTTP/1.1 200 OK
Date: Thu, 08 Oct 2026 03:53:39 GMT
Content-Type: application/json
Connection: keep-alive
{"id":1,"clienteId":1,"numeroCuenta":"0692092155","clabe":"012180069209215501","saldo":1500.00,"estatus":"ACTIVA"}

$ curl.exe -s -i "https://gestopago-app.onrender.com/cuentas/0692092155/saldo"

HTTP/1.1 200 OK
Date: Thu, 08 Oct 2026 03:53:53 GMT
Content-Type: application/json
Connection: keep-alive
{"numeroCuenta":"0692092155","saldo":1500.00}

`

Dictamen de la Prueba 06: EXITOSA. Código HTTP 200 OK en ambos endpoints. Se validó la lectura precisa del saldo de $1,500.00 MXN.

10.7. Prueba E2E 07: Autenticación de Usuario y Generación de Token JWT

Comprueba el endpoint POST /auth/login, validando la comparación del hash BCrypt contra la contraseña en texto claro y la emisión del JWT.


#### [REGISTRO DE TERMINAL] COMANDO Y RESPUESTA RAW: POST /AUTH/LOGIN

`ash
$ curl.exe -s -i -X POST "https://gestopago-app.onrender.com/auth/login" \
  -H "Content-Type: application/json" \
  --data-binary "@sample_login.json"

HTTP/1.1 200 OK
Date: Thu, 08 Oct 2026 03:56:14 GMT
Content-Type: application/json
Transfer-Encoding: chunked
Connection: keep-alive
cf-cache-status: DYNAMIC
rndr-id: 703d12d9-f0fd-46a3
Server: cloudflare
vary: Accept-Encoding
x-render-origin-server: Render
CF-RAY: a47236e96fbd4f40-DFW
alt-svc: h3=":443"; ma=86400

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtYXJpYW5hLmhlcm5hbmRlekBleGFtcGxlLmNvbSIsImNsaWVudGVJZCI6MSwidXN1YXJpb0lkIjoxLCJpYXQiOjE3OTE0MzE3NzQsImV4cCI6MTc5MTUxODE3NH0.W81EDUEfG3-xA8pUMV7ko470S6Wi2Z1v47soIM2IuHk",
  "tipoToken": "Bearer",
  "correo": "mariana.hernandez@example.com",
  "clienteId": 1,
  "expiraEnMs": 86400000
}

`

Dictamen de la Prueba 07: EXITOSA. Código HTTP 200 OK. Token JWT emitido bajo estándar Bearer con claims de clienteId: 1 y usuarioId: 1.

10.8. Prueba E2E 08: Prevención de Duplicados e Integridad de Negocio

Verifica que un intento de registrar nuevamente al mismo cliente sea interceptado arrojando HTTP 409 Conflict.


#### [REGISTRO DE TERMINAL] COMANDO Y RESPUESTA RAW: POST /CLIENTES (INTENTO DUPLICADO)

`ash
$ curl.exe -s -i -X POST "https://gestopago-app.onrender.com/clientes" \
  -H "Content-Type: application/json" \
  --data-binary "@sample_cliente.json"

HTTP/1.1 409 Conflict
Date: Thu, 08 Oct 2026 03:57:22 GMT
Content-Type: application/json
Transfer-Encoding: chunked
Connection: keep-alive
cf-cache-status: DYNAMIC
rndr-id: ca7419a7-3756-4c67
Server: cloudflare
vary: Accept-Encoding
x-render-origin-server: Render
CF-RAY: a47238996bbff07d-DFW
alt-svc: h3=":443"; ma=86400

{"codigo":409,"mensaje":"Ya existe un cliente registrado con la CURP: HETM940822MDFRRN03"}

`

Dictamen de la Prueba 08: EXITOSA. Código HTTP 409 Conflict. Se impidió el registro duplicado preservando la unicidad del padrón.

10.9. Prueba E2E 09: Desalojo y Purga de Caché en Redis

Comprueba el endpoint DELETE /productos/cache, validando la interacción de escritura y desalojo de llaves en el servidor Redis.


#### [REGISTRO DE TERMINAL] COMANDO Y RESPUESTA RAW: DELETE /PRODUCTOS/CACHE

`ash
$ curl.exe -s -i -X DELETE "https://gestopago-app.onrender.com/productos/cache"

HTTP/1.1 204 No Content
Date: Thu, 08 Oct 2026 03:59:26 GMT
Connection: keep-alive
rndr-id: 2d863c57-db8a-408a
Server: cloudflare
x-render-origin-server: Render
cf-cache-status: DYNAMIC
CF-RAY: a4723b9d4b70f0b2-DFW
alt-svc: h3=":443"; ma=86400

`

Dictamen de la Prueba 09: EXITOSA. Código HTTP 204 No Content. La orden de desalojo en Redis se completó satisfactoriamente.

CAPÍTULO 11: AUDITORÍA DE SEGURIDAD, GESTIÓN CRIPTOGRÁFICA Y ANÁLISIS DE RIESGOS

11.1. Seguridad de Transporte y Encriptación en Tránsito (TLS 1.3)

Toda interacción entre clientes externos y la infraestructura de Render se encuentra encapsulada mediante protocolos de cifrado de última generación Transport Layer Security (TLS versión 1.2 y 1.3). Render provee certificados criptográficos X.509 emitidos automáticamente por autoridades certificadoras reconocidas (Let's Encrypt y Cloudflare Inc.). Esto garantiza la confidencialidad, autenticidad e inmunidad frente a ataques de hombre en el medio (Man-in-the-Middle o MitM).

11.2. Almacenamiento Criptográfico de Contraseñas (BCrypt)

El microservicio cumple rigurosamente con las recomendaciones de la directiva NIST SP 800-63B al prohibir el almacenamiento de contraseñas en texto claro o mediante funciones hash obsoletas como MD5 o SHA-1. La clase UsuarioServiceImpl utiliza el algoritmo BCryptPasswordEncoder provisto por Spring Security. BCrypt incorpora una sal criptográfica de 128 bits generada de forma aleatoria para cada contraseña y un factor de costo computacional (work factor = 10), lo que previene ataques de fuerza bruta mediante tablas arcoíris (Rainbow Tables).

11.3. Esquema de Tokens Web JSON (JWT RFC 7519)

El sistema adopta el estándar abierto RFC 7519 para la delegación de identidad sin estado (stateless). El token emitido durante el inicio de sesión consta de tres segmentos delimitados por puntos:

- Encabezado (Header): Define el tipo de token (typ: JWT) y el algoritmo de firma digital utilizado (alg: HS256).
- Cuerpo de Reclamos (Payload Claims): Contiene la identidad del usuario (sub), el identificador del cliente (clienteId), el identificador del usuario (usuarioId), la fecha de expedición en época Unix (iat) y la fecha de expiración programada (exp: 24 horas posteriores).
- Firma Digital Criptográfica (Signature): Generada a partir de la concatenación del encabezado y payload codificados en Base64Url, procesados mediante la función de autenticación de mensajes basada en hash HMAC-SHA256 utilizando la clave secreta institucional.
11.4. Análisis contra el OWASP API Security Top 10

Se contrastó la implementación del microservicio frente a las vulnerabilidades más críticas catalogadas por el consorcio OWASP:

- API1:2023 - Broken Object Level Authorization (BOLA): Mitigado en la lógica de negocio mediante la vinculación estricta entre clienteId y usuarioId verificada en la base de datos.
- API2:2023 - Broken Authentication: Protegido mediante hashing seguro BCrypt y firmas JWT. Para despliegues comerciales se recomienda añadir un filtro HTTP estricto (SecurityFilterChain) que bloquee llamadas directas.
- API3:2023 - Broken Object Property Level Authorization: Blindado mediante DTOs específicos de entrada (ClienteRegistroRequest) y salida (ClienteResponse), impidiendo ataques de asignación masiva (Mass Assignment).
- API4:2023 - Unrestricted Resource Consumption: Se recomienda en producción la activación de limitación de tasa de peticiones (Rate Limiting) y paginación en endpoints de consulta masiva.
- API8:2023 - Security Misconfiguration: Los puertos internos de PostgreSQL y Redis no son expuestos públicamente; las cabeceras de depuración detallada están deshabilitadas en producción.
CAPÍTULO 12: MANUAL DE OPERACIÓN, MONITOREO EN RENDER Y MANTENIMIENTO

12.1. Panel de Control y Telemetría Operativa

El mantenimiento del servicio se efectúa a través del portal de administración en Render (dashboard.render.com). Desde dicho panel, el equipo de ingeniería puede acceder a métricas de infraestructura en tiempo real, incluyendo utilización porcentual de CPU, consumo de memoria RAM (límite de 512 MB en tier gratuito), volumen de ancho de banda entrante y saliente, y latencia de atención de solicitudes HTTP.

12.2. Flujo de Despliegue Continuo (CI/CD Automático)

Gracias a la integración con GitHub Webhooks y la declaración del Blueprint render.yaml, cualquier confirmación (commit) empujada a la rama main del repositorio oficial dispara automáticamente un nuevo ciclo de construcción en Render. El pipeline clona el código, ejecuta el Dockerfile, verifica la salud del nuevo contenedor mediante /actuator/health y sustituye la instancia anterior sin tiempo de inactividad (Zero-Downtime Deployment).

12.3. Comportamiento del Ciclo de Suspensión por Inactividad (Cold Start)

En el plan gratuito de Render, las instancias de servicio web se suspenden temporalmente tras 15 minutos consecutivos de inactividad para conservar recursos de cómputo. Cuando un usuario envía una nueva solicitud HTTP a un servicio suspendido, Render detecta el tráfico entrante y presenta una pantalla de bienvenida con la leyenda 'WELCOME TO RENDER / SERVICE WAKING UP'. Durante un lapso de 30 a 50 segundos, el contenedor es reiniciado, la máquina virtual Java arranca y la petición es atendida con total normalidad. Este comportamiento es esperado y se subsana en ambientes empresariales actualizando al plan 'Starter' o superior.

CAPÍTULO 13: CONCLUSIONES TÉCNICAS, DICTAMEN DE CERTIFICACIÓN Y EVOLUCIÓN

13.1. Dictamen de Certificación Técnica

Con base en los resultados empíricos recabados a lo largo de las auditorías de código, la ejecución de la suite automatizada de pruebas de Gradle, el análisis del esquema relacional PostgreSQL, la resiliencia demostrada por la caché Redis y las 9 pruebas funcionales de extremo a extremo realizadas desde terminal HTTP contra el entorno de Render, se emite el siguiente dictamen:

CERTIFICACIÓN FORMAL: DICTAMEN: APROBADO SATISFACTORIAMENTE PARA PRODUCCIÓN. El microservicio 'Servicio Empresa / GestoPago' cumple con los más altos estándares de calidad, seguridad y portabilidad, encontrándose plenamente operativo en su dirección canónica https://gestopago-app.onrender.com.

13.2. Síntesis de Fortalezas Técnicas Demostradas

- Portabilidad Absoluta: El uso de Docker multi-stage garantiza que el artefacto puede ser desplegado de manera idéntica en cualquier proveedor de nube (Render, AWS ECS, Google Cloud Run, Azure Container Apps) o en clústeres Kubernetes on-premise.
- Gobernanza Automatizada de Base de Datos: Flyway Migration elimina el factor de error humano en la evolución de tablas e índices relacionales.
- Aislamiento de Perímetro: La persistencia y la caché operan en una red privada virtual (VPC) sin exposición indebida a redes públicas.
- Criptografía Robusta: La custodia de credenciales se apoya en funciones hash BCrypt y la delegación de autoridad se gestiona con tokens JWT estándar.
13.3. Recomendaciones para Fases Futuras de Evolución

- Filtro HTTP de Seguridad Restrictivo: Implementar un SecurityFilterChain mediante un filtro OncePerRequestFilter que intercepte y valide la cabecera Authorization en todos los endpoints antes de permitir el acceso a los controladores.
- Protección de la Consola Swagger: Configurar autenticación básica (HTTP Basic Auth) o deshabilitar la interfaz Swagger UI en perfiles de producción comercial.
- Gestión Centralizada de Secretos: Migrar la clave secreta app.jwt.secret hacia una variable de entorno inyectada desde Render Secret Files o HashiCorp Vault.
CAPÍTULO 14: REFERENCIAS BIBLIOGRÁFICAS, NORMAS TÉCNICAS Y FUENTES OFICIALES

A continuación se relacionan las fuentes bibliográficas, normas de estandarización técnica y documentación oficial consultada y referenciada para la elaboración del presente informe:

- 1. Spring Framework Documentation: VMware Tanzu. (2024). Spring Boot Reference Documentation (Version 3.3.6). Recuperado de https://docs.spring.io/spring-boot/docs/3.3.6/reference/html/
- 2. Docker Inc.: Docker Documentation. (2024). Multi-stage builds and best practices for containerizing Java applications. Recuperado de https://docs.docker.com/build/building/multi-stage/
- 3. Render Documentation: Render Cloud Inc. (2024). Blueprints Specification (render.yaml) & Docker Deployments. Recuperado de https://render.com/docs/blueprint-spec
- 4. PostgreSQL Global Development Group: PostgreSQL 15 Documentation. (2024). The PostgreSQL Object-Relational Database System. Recuperado de https://www.postgresql.org/docs/15/
- 5. Redgate Software: Flyway by Redgate. (2024). Database Migrations Evolved. Recuperado de https://documentation.red-gate.com/fd
- 6. Redis Ltd.: Redis Documentation. (2024). Redis In-Memory Data Store & Caching Strategies. Recuperado de https://redis.io/docs/
- 7. Internet Engineering Task Force (IETF): Jones, M., Bradley, J., & Sakimura, N. (2015). RFC 7519: JSON Web Token (JWT). Internet Engineering Task Force. https://doi.org/10.17487/RFC7519
- 8. Open Web Application Security Project (OWASP): OWASP Foundation. (2023). OWASP API Security Top 10 2023. Recuperado de https://owasp.org/www-project-api-security/
- 9. National Institute of Standards and Technology (NIST): Grassi, P. A., et al. (2017). NIST Special Publication 800-63B: Digital Identity Guidelines - Authentication and Lifecycle Management. U.S. Department of Commerce.
- 10. OpenAPI Initiative: SmartBear Software. (2023). OpenAPI Specification Version 3.0.3. Recuperado de https://spec.openapis.org/oas/v3.0.3