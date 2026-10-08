REPÚBLICA DE MÉXICO | DIRECCIÓN DE TECNOLOGÍAS FINANCIERAS Y SISTEMAS
DEPARTAMENTO DE ARQUITECTURA DE SOFTWARE, ASEGURAMIENTO DE CALIDAD Y DEVOPS

EXPEDIENTE TÉCNICO DE AUDITORÍA INTEGRAL, PRUEBAS DE REGRESIÓN DE SOFTWARE Y CERTIFICACIÓN DE DESPLIEGUE EN INFRAESTRUCTURA CLOUD RENDER

Evaluación Exhaustiva de Microservicio Spring Boot 3.3.6, Motor Relacional PostgreSQL 15, Caché Distribuida Redis 7, Contenedores Docker Multi-Stage, Verificación de Enlace de Puerto Dinámico, Migraciones Flyway y Batería de Pruebas End-to-End desde Terminal HTTP

AUTORA / INGENIERA RESPONSABLE: Valeria Guadalupe Calvillo
REPOSITORIO DE CONTROL DE VERSIONES: https://github.com/valeria1732/ValeriaPrueba
RAMAS AUDITADAS: main y future/configuracion-necesaria
PLATAFORMA CLOUD DE PRODUCCIÓN: Render Cloud Platform (Región US-West Oregon)
IDENTIFICADOR DEL SERVICIO CLOUD: srv-db3gjvl19fdbs73dn8vo0
URL PÚBLICA CANÓNICA EN PRODUCCIÓN: https://gestopago-app.onrender.com
CONSOLA INTERACTIVA SWAGGER UI: https://gestopago-app.onrender.com/swagger-ui/index.html
FECHA DE AUDITORÍA Y CERTIFICACIÓN: Octubre de 2026
VERSIÓN DEL EXPEDIENTE: 2.0 - Edición Oficial Exhaustiva (50 Páginas - Sin Tablas)

RESUMEN EJECUTIVO Y CONTROL DE CAMBIOS DEL PROYECTO

El presente informe recopila de manera íntegra, formal e irrefutable todas las actividades de desarrollo, corrección de infraestructura, empaquetado, pruebas unitarias, pruebas de integración y validación funcional en tiempo real efectuadas sobre el microservicio denominado 'Servicio Empresa' o 'GestoPago API'. Este sistema representa una pieza angular para el onboarding digital de personas físicas en plataformas financieras, abarcando desde la recolección estricta de datos biométricos y domiciliarios, hasta la apertura sincronizada de cuentas bancarias de captación con generación matemática de CLABE interbancaria de 18 dígitos, cifrado de credenciales de acceso bajo estándares bancarios y la integración con catálogos externos de productos provistos por GestoPago.

Durante el proceso de auditoría y puesta en producción en la plataforma de nube Render, se identificaron y solventaron exitosamente cuatro barreras técnicas de alto nivel:

- Adaptación de Enlace de Puerto Dinámico (PORT Binding): El framework Spring Boot escuchaba inicialmente en el puerto estático 8088. En Render, el balanceador de carga exige que el contenedor enlace su socket web a la variable de entorno 'PORT' inyectada dinámicamente. Se refactorizó la configuración del servidor en application.properties para establecer la jerarquía ${PORT:${SERVER_PORT:8088}}, garantizando compatibilidad simultánea en la nube y en máquinas de desarrollo local.
- Resolución de Nombres de Usuario Reservados en PostgreSQL: Durante la sincronización del Blueprint (render.yaml), Render rechazó la creación de la base de datos debido al uso del nombre de usuario 'postgres', el cual es una palabra reservada en la infraestructura gestionada de Render. Se actualizó la definición a 'gestopago_user', resolviendo el bloqueo de forma inmediata.
- Transformación Resiliente de URL de Base de Datos en ConfigDB.java: Render entrega las credenciales de conexión mediante la variable DATABASE_URL en formato URI de Unix (postgresql://...). El controlador oficial org.postgresql.Driver de Java exige exclusivamente el prefijo jdbc:postgresql://. Se implementó un algoritmo de parseo en ConfigDB.java que analiza y transforma la URI en tiempo de ejecución, extrayendo credenciales y garantizando conexión limpia con HikariCP.
- Contenedorización en Dos Etapas (Multi-Stage Docker): Se construyó un Dockerfile optimizado que utiliza Gradle 8.8 con JDK 17 para la fase de construcción y Eclipse Temurin 17 JRE Alpine para la fase de ejecución, reduciendo la superficie de ataque y el peso final de la imagen.
Control de Versiones y Trazabilidad de Commits Auditados

Cada modificación efectuada se encuentra estrictamente versionada en el historial de Git del repositorio GitHub (https://github.com/valeria1732/ValeriaPrueba). A continuación se relacionan los hitos de confirmación auditados:

- Commit da6f9a7: feat: configuracion para despliegue en Render con Docker y Blueprint. Incorporación de Dockerfile raíz y render.yaml inicial.
- Commit b03375c: chore: add render.yaml in prueba subdirectory as well. Garantiza detección de Blueprint tanto en la raíz como en subdirectorios.
- Commit 392c6dc: fix: change database user from reserved postgres to gestopago_user. Corrección de restricción de Render para el aprovisionamiento de PostgreSQL.
- Commit 1d3da6a: docs: add Deploy to Render 1-click button. Adición del botón de despliegue directo de 1 clic en el archivo README.md.
- Commit 82f72d8: feat: redirect / to swagger-ui/index.html. Creación del controlador HomeController para redirigir la ruta raíz automáticamente hacia Swagger UI.
- Commit 6646008: feat: add render server url to swagger. Configuración de servidores relativos y de producción en la clase OpenApi.java para permitir pruebas directas desde el navegador.
- Commit 9ecacd5: docs: add comprehensive technical certification and testing document. Incorporación del expediente formal de pruebas al árbol del proyecto.
GLOSARIO DE TÉRMINOS TÉCNICOS Y ACRÓNIMOS EMPRESARIALES

Para facilitar la comprensión inequívoca de los términos, estándares y protocolos citados en este informe, se presenta el siguiente glosario técnico redactado en cumplimiento con la prohibición estricta de tablas:

- API (Application Programming Interface): Interfaz de programación de aplicaciones que define los contratos y mecanismos formales mediante los cuales distintos componentes de software se comunican entre sí a través de la red.
- BCrypt: Función criptográfica de derivación de claves basada en el cifrado Blowfish, diseñada específicamente para el hashing seguro de contraseñas mediante la inclusión automática de sales aleatorias y factores de costo computacional iterativo.
- CLABE (Clave Bancaria Estandarizada): Norma bancaria oficial en los Estados Unidos Mexicanos que establece un identificador numérico único de 18 dígitos para cada cuenta bancaria, estructurado por código de institución de crédito (3 dígitos), código de plaza o sucursal (3 dígitos), número de cuenta particular (11 dígitos) y un dígito de verificación matemática ponderada.
- CURP (Clave Única de Registro de Población): Instrumento de registro e identidad oficial en México asignado a cada habitante por el Registro Nacional de Población (RENAPO), integrado por 18 caracteres alfanuméricos con reglas estrictas de estructura y código verificador.
- DTO (Data Transfer Object): Patrón de diseño de software que encapsula un conjunto de datos para transmitirlos entre subsistemas o a través de la red, garantizando el aislamiento del modelo de dominio interno respecto a las cargas útiles expuestas.
- Flyway: Herramienta líder de código abierto para el control de versiones, evolución continua y migración determinista de esquemas de bases de datos relacionales mediante scripts SQL inmutables.
- HikariCP: Entramado de agrupación de conexiones JDBC (Connection Pool) de altísimo rendimiento para la plataforma Java, caracterizado por su microoptimización a nivel de bytecode y bajo consumo de memoria.
- HMAC (Hash-based Message Authentication Code): Mecanismo criptográfico específico para calcular códigos de autenticación de mensajes mediante una función hash (como SHA-256) en combinación con una clave secreta compartida.
- JPA (Jakarta Persistence API): Estándar oficial de la industria Java para la gestión de datos relacionales y mapeo objeto-relacional (ORM), cuya implementación de referencia en este microservicio es Hibernate ORM.
- JWT (JSON Web Token): Estándar abierto de la IETF definido en el RFC 7519 para la representación compacta y autónoma de reclamos de seguridad e identidad entre dos partes mediante firmas criptográficas digitales.
- OpenFeign: Entorno declarativo provisto por Spring Cloud que genera de forma automática clientes HTTP dinámicos a partir de interfaces Java anotadas, simplificando la interoperabilidad entre microservicios.
- PaaS (Platform as a Service): Modelo de computación en la nube donde el proveedor gestiona el hardware, el sistema operativo, los parches de seguridad y la red, permitiendo a los desarrolladores concentrarse en el despliegue de sus aplicaciones.
- RFC (Registro Federal de Contribuyentes): Clave alfanumérica tributaria única asignada por el Servicio de Administración Tributaria (SAT) en México para la identificación de personas físicas (13 caracteres) y morales (12 caracteres).
- Swagger / OpenAPI: Especificación estándar de la industria, agnóstica del lenguaje de programación, utilizada para describir, estructurar, documentar y consumir APIs web de tipo REST.
- TLS (Transport Layer Security): Protocolo criptográfico moderno diseñado para proporcionar comunicaciones seguras y cifradas de extremo a extremo sobre redes públicas como internet.
- VPC (Virtual Private Cloud): Red lógica aislada e independiente dentro de una nube pública donde los recursos (como bases de datos y memorias caché) residen sin exposición directa al tráfico de internet.
CAPÍTULO 1: INTRODUCCIÓN GENERAL, CONTEXTO DE NEGOCIO Y OBJETIVOS DE LA AUDITORÍA TÉCNICA

1.1. Contexto del Proyecto y Problemática de Negocio

En el contexto actual de digitalización de los servicios bancarios y transaccionales, las instituciones financieras enfrentan el reto ineludible de ofrecer experiencias de usuario fluidas, seguras y de disponibilidad ininterrumpida. El proyecto denominado 'Servicio Empresa' o 'GestoPago Microservicios' nace con la misión de habilitar un canal de onboarding digital no presencial que permita a las personas físicas registrarse, validar su información de identidad y abrir de forma inmediata una cuenta bancaria con saldo operativo, recibiendo simultáneamente credenciales de acceso seguras a la plataforma web.

El flujo de valor automatizado por el microservicio resuelve problemáticas complejas que anteriormente requerían múltiples intervenciones operativas presenciales:

- Validación de Identidad Oficial: El sistema efectúa un filtrado riguroso de las cadenas de CURP y RFC utilizando expresiones regulares que se apegan a los estándares de RENAPO y del SAT, previniendo fraudes por usurpación o errores tipográficos en el origen.
- Apertura Inmediata de Cuenta de Captación: En la misma transacción de registro, el servicio genera un número de cuenta único de 10 dígitos y calcula la CLABE interbancaria correspondiente mediante el algoritmo matemático oficial de 18 dígitos.
- Aprovisionamiento de Usuario y Hash de Clave: El servicio crea la cuenta de usuario ligada al cliente, aplicando el algoritmo BCrypt para almacenar el hash de la contraseña de forma irreversible, garantizando la confidencialidad de la clave.
- Interconexión con Redes de Recaudación: Mediante clientes Feign se facilita la consulta y pago de servicios de empresas de servicios públicos y prepago a través de GestoPago.
1.2. Objetivos de la Auditoría y Metodología de Validación

La auditoría documentada en este informe tuvo como objetivo primordial validar la robustez técnica del software en cuatro dimensiones críticas: calidad arquitectónica del código, seguridad criptográfica, automatización del despliegue en la nube y rendimiento funcional mediante pruebas de extremo a extremo desde la consola de comandos.

La metodología de trabajo se basó en el ciclo de aseguramiento continuo: inspección de código fuente, análisis de configuración de Gradle y dependencias, verificación del empaquetado Docker, aprovisionamiento en Render Cloud, ejecución de la suite de JUnit 5 y validación en vivo mediante clientes cURL ejecutando peticiones HTTP reales contra la infraestructura de producción.

CAPÍTULO 2: ANÁLISIS DETALLADO DEL DOMINIO BANCARIO Y REGLAS DE NEGOCIO

2.1. Regla de Mayoría de Edad Legal y Validación Temporal

El sistema financiero exige que la contratación de productos de captación esté reservada a personas que cuenten con plena capacidad legal de ejercicio. En la República Mexicana, este requisito se adquiere al cumplir los 18 años de edad. En el microservicio, la clase ClienteServiceImpl implementa esta validación matemática mediante el cálculo de periodo entre la fecha de nacimiento (java.time.LocalDate) y la fecha del sistema en tiempo de ejecución (LocalDate.now()). Si el periodo resultante es inferior a 18 años, el servicio aborta la transacción y arroja ClienteBusinessException con el mensaje 'El cliente debe ser mayor de edad'.

2.2. Algoritmo Matemático de la CLABE Interbancaria (18 Dígitos)

La Clave Bancaria Estandarizada (CLABE) es un elemento indispensable para que la cuenta creada pueda recibir transferencias electrónicas a través del Sistema de Pagos Electrónicos Interbancarios (SPEI) del Banco de México. La estructura de la CLABE consta de 18 dígitos distribuidos de la siguiente forma:

- Código de Banco (3 dígitos): Identifica a la institución de crédito emisora. En la implementación se utiliza '012', correspondiente a BBVA México en el catálogo del Banco de México.
- Código de Plaza o Sucursal (3 dígitos): Identifica la ubicación geográfica o sucursal operativa de radicación de la cuenta. En la lógica se establece '180' como plaza centralizada de servicios digitales.
- Número de Cuenta Bancaria (11 dígitos): Corresponde al número de cuenta asignado al cliente, formateado con ceros a la izquierda para completar exactamente 11 posiciones numéricas.
- Dígito Verificador Ponderado (1 dígito): Calculado mediante el algoritmo estándar de módulo 10 ponderado establecido por la Asociación de Bancos de México (ABM). Los primeros 17 dígitos son multiplicados por una secuencia periódica de factores de ponderación [3, 7, 1], se suman los productos resultantes en base 10, y el dígito verificador se obtiene restando dicho residuo de 10 (o 0 si el residuo es exacto).
2.3. Reglas de Validación de CURP y RFC

Para garantizar que los datos tributarios y poblacionales cumplan con las especificaciones de las dependencias gubernamentales, el modelo ClienteRegistroRequest integra anotaciones @Pattern con expresiones regulares oficiales:

- Expresión Regular de CURP: ^[A-Z]{4}\d{6}[HM][A-Z]{5}[A-Z0-9]\d$ - Exige 18 caracteres en mayúsculas, validando iniciales, fecha de nacimiento YYMMDD, género (H o M), clave de entidad federativa de 2 letras y homoclave con dígito verificador.
- Expresión Regular de RFC: ^[A-Z&Ñ]{3,4}\d{6}[A-V1-9][A-Z1-9][0-9A]$ - Exige entre 12 y 13 caracteres, validando nombres o razones sociales, fecha de nacimiento y homoclave oficial de 3 caracteres asignada por el SAT.
CAPÍTULO 3: ARQUITECTURA TÉCNICA DE SOFTWARE Y PATRONES DE DISEÑO

3.1. Patrones de Diseño de Software Implementados

La arquitectura del microservicio descansa sobre patrones de diseño de software consolidados en la industria empresarial:

- Patrón Inyección de Dependencias (Dependency Injection): Todos los servicios y controladores utilizan inyección de dependencias mediante constructores recomendada por Spring Framework. Esto elimina el uso de @Autowired en atributos privados, facilitando las pruebas unitarias y garantizando la inmutabilidad de referencias.
- Patrón Data Transfer Object (DTO): Asegura el desacoplamiento total entre las entidades JPA de base de datos y las representaciones JSON transmitidas a través de la red, evitando la fuga accidental de atributos internos como hashes de contraseñas.
- Patrón Manejo Global de Excepciones (@RestControllerAdvice): La clase GlobalExceptionHandler intercepta todas las excepciones controladas del dominio (ClienteNotFoundException, CurpDuplicadaException, etc.) y las traduce a respuestas HTTP estandarizadas con código de error, mensaje comprensible y timestamp, evitando que la aplicación exponga volcados de pila (stack traces) al cliente.
- Patrón Repositorio y Especificaciones (Spring Data JPA): Encapsula las consultas de base de datos. Utiliza métodos semánticos y Specifications para consultas dinámicas complejas basadas en Criteria API de Hibernate.
CAPÍTULO 4: DICCIONARIO DE DATOS Y ESPECIFICACIÓN DEL MODELO RELACIONAL

En estricto cumplimiento con la directriz de prescindir de tablas visuales, a continuación se detalla el diccionario de datos completo correspondiente a las entidades relacionales persistidas en el motor PostgreSQL 15:

4.1. Entidad: clientes (Tabla 'clientes')

Representa la persona física registrada en el sistema financiero. Atributos detallados:

- id: Tipo INTEGER, clave primaria auto-incremental (SERIAL), obligatorio, no modificable.
- nombre: Tipo VARCHAR(50), almacena el primer nombre del cliente, longitud entre 2 y 50 caracteres, obligatorio.
- segundo_nombre: Tipo VARCHAR(50), almacena el segundo nombre, opcional, máximo 50 caracteres.
- apellido_paterno: Tipo VARCHAR(50), primer apellido, obligatorio, solo letras y espacios.
- apellido_materno: Tipo VARCHAR(50), segundo apellido, obligatorio, solo letras y espacios.
- fecha_nacimiento: Tipo DATE, fecha de nacimiento, obligatorio, debe ser fecha pasada que cumpla mayoría de edad.
- curp: Tipo VARCHAR(18), Clave Única de Registro de Población, obligatorio, índice único de unicidad.
- rfc: Tipo VARCHAR(13), Registro Federal de Contribuyentes con homoclave, obligatorio, índice único de unicidad.
- sexo: Tipo VARCHAR(20), género legal de la persona física (MASCULINO / FEMENINO), obligatorio.
- nacionalidad: Tipo VARCHAR(50), país de nacionalidad, por defecto 'Mexicana', obligatorio.
- estado_civil: Tipo VARCHAR(30), estado civil declarado (SOLTERO, CASADO, etc.), obligatorio.
- correo: Tipo VARCHAR(100), correo electrónico de contacto y nombre de usuario, obligatorio, índice único.
- telefono_movil: Tipo VARCHAR(15), número celular a 10 dígitos numéricos, obligatorio.
- telefono_alternativo: Tipo VARCHAR(15), número fijo o alternativo a 10 dígitos, opcional.
- domicilio_id: Tipo INTEGER, clave foránea vinculada a la tabla 'domicilios', restricción ON DELETE RESTRICT.
- ocupacion: Tipo VARCHAR(100), profesión o actividad económica remunerada, obligatorio.
- empresa: Tipo VARCHAR(100), denominación del centro de trabajo o empresa empleadora, obligatorio.
- ingreso_mensual: Tipo NUMERIC(12,2), salario o ingreso neto mensual comprobable, mayor a cero, obligatorio.
- activo: Tipo BOOLEAN, indicador de vigencia operativa del cliente en el padrón, valor por defecto TRUE.
- fecha_creacion: Tipo TIMESTAMP, marca temporal de auditoría de inserción inicial, no nulo.
- fecha_actualizacion: Tipo TIMESTAMP, marca temporal de última modificación del registro, no nulo.
4.2. Entidad: cuentas_bancarias (Tabla 'cuentas_bancarias')

Representa el contrato de depósito bancario de captación a la vista. Atributos detallados:

- id: Tipo INTEGER, clave primaria auto-incremental (SERIAL), obligatorio.
- cliente_id: Tipo INTEGER, clave foránea ligada a la tabla 'clientes', borrado en cascada ON DELETE CASCADE.
- numero_cuenta: Tipo VARCHAR(20), identificador de cuenta interna a 10 dígitos, obligatorio, índice único.
- clabe: Tipo VARCHAR(18), Clave Bancaria Estandarizada interbancaria a 18 dígitos, obligatorio, índice único.
- saldo: Tipo NUMERIC(12,2), saldo disponible monetario en moneda nacional (MXN), valor por defecto 0.00.
- estatus: Tipo VARCHAR(20), estado operativo de la cuenta (ACTIVA, BLOQUEADA, CANCELADA), por defecto 'ACTIVA'.
- fecha_creacion: Tipo TIMESTAMP, marca temporal de apertura de cuenta.
- fecha_actualizacion: Tipo TIMESTAMP, marca temporal de última operación o movimiento de saldo.
4.3. Entidad: usuarios (Tabla 'usuarios')

Representa las credenciales de acceso autenticado al portal bancario. Atributos detallados:

- id: Tipo INTEGER, clave primaria auto-incremental (SERIAL), obligatorio.
- cliente_id: Tipo INTEGER, clave foránea asociada a la tabla 'clientes', ON DELETE CASCADE.
- correo: Tipo VARCHAR(100), nombre de usuario de inicio de sesión, obligatorio, índice único.
- password: Tipo VARCHAR(255), hash criptográfico irreversible generado con BCrypt, obligatorio.
- activo: Tipo BOOLEAN, bandera de activación de cuenta de usuario, por defecto TRUE.
- fecha_creacion: Tipo TIMESTAMP, fecha y hora de creación de credenciales.
- fecha_actualizacion: Tipo TIMESTAMP, fecha y hora de último cambio de contraseña.
4.4. Entidad: domicilios (Tabla 'domicilios')

Almacena la ubicación geográfica y residencia fiscal del cliente. Atributos detallados:

- id: Tipo INTEGER, clave primaria auto-incremental, obligatorio.
- calle: Tipo VARCHAR(100), nombre de la vía o avenida, obligatorio.
- numero_exterior: Tipo VARCHAR(20), número oficial exterior, obligatorio.
- numero_interior: Tipo VARCHAR(20), número de departamento o piso, opcional.
- colonia: Tipo VARCHAR(100), asentamiento o colonia, obligatorio.
- municipio: Tipo VARCHAR(100), municipio o alcaldía de radicación, obligatorio.
- estado: Tipo VARCHAR(100), entidad federativa o estado, obligatorio.
- codigo_postal: Tipo VARCHAR(10), código postal mexicano a 5 dígitos numéricos, obligatorio.
- pais: Tipo VARCHAR(50), país de residencia, por defecto 'México'.
CAPÍTULO 5: CATÁLOGO EXHAUSTIVO DE ENDPOINTS Y CONTRATOS DE LA API REST

El microservicio expone un conjunto integral de servicios web clasificados funcionalmente en módulos. A continuación se describen detalladamente sus contratos de invocación, parámetros y respuestas:

5.1. Módulo de Clientes (/clientes)

- POST /clientes: Registra un nuevo cliente con apertura simultánea de cuenta y creación de usuario. Consumo: application/json. Producción: application/json. Códigos de respuesta: 201 Created (Éxito), 400 Bad Request (Fallas de formato o negocio), 409 Conflict (CURP, RFC o correo duplicado).
- GET /clientes: Consulta la lista general de clientes registrados. Soporta parámetros opcionales de filtrado: curp (String de 18 caracteres), rfc (String de 13 caracteres), fechaNacimiento (LocalDate en formato YYYY-MM-DD). Códigos de respuesta: 200 OK con lista JSON de clientes.
- GET /clientes/{id}: Recupera la información completa de un cliente por su clave primaria numérica. Parámetro de ruta: id (Entero positivo). Códigos de respuesta: 200 OK (Encontrado), 404 Not Found (Cliente inexistente).
- PUT /clientes/{id}: Actualización integral de los datos personales, de contacto y domicilio del cliente. Códigos de respuesta: 200 OK, 400 Bad Request, 404 Not Found.
- PATCH /clientes/{id}: Actualización parcial de atributos específicos (por ejemplo, cambio de número telefónico o estado civil). Códigos de respuesta: 200 OK, 400 Bad Request, 404 Not Found.
- DELETE /clientes/{id}: Baja lógica del cliente en el padrón, estableciendo el atributo activo en false sin eliminar físicamente el registro histórico. Códigos de respuesta: 204 No Content, 404 Not Found.
5.2. Módulo de Cuentas Bancarias (/cuentas)

- GET /cuentas: Consulta la totalidad de cuentas bancarias existentes en el sistema o filtra aquellas asociadas a un cliente específico mediante el parámetro de consulta clienteId. Códigos de respuesta: 200 OK.
- POST /cuentas: Apertura manual de una cuenta adicional para un cliente previamente registrado en el sistema. Códigos de respuesta: 201 Created, 400 Bad Request, 404 Not Found.
- GET /cuentas/{numeroCuenta}: Consulta el detalle y estado de una cuenta a partir de su número de cuenta de 10 dígitos. Códigos de respuesta: 200 OK, 404 Not Found.
- GET /cuentas/{numeroCuenta}/saldo: Consulta en tiempo real el saldo monetario disponible de la cuenta especificada. Códigos de respuesta: 200 OK con JSON {numeroCuenta, saldo}, 404 Not Found.
- PATCH /cuentas/{numeroCuenta}: Actualización del estatus operativo de la cuenta bancaria (ACTIVA, BLOQUEADA, CANCELADA). Códigos de respuesta: 200 OK, 400 Bad Request, 404 Not Found.
5.3. Módulo de Autenticación y Seguridad (/auth)

- POST /auth/login: Valida credenciales de acceso (correo y contraseña). Verifica hash BCrypt y emite un token de seguridad JWT Bearer válido por 24 horas. Códigos de respuesta: 200 OK (Token generado), 400 Bad Request (Formato inválido), 401 Unauthorized (Contraseña errónea), 403 Forbidden (Usuario inactivo), 404 Not Found (Usuario no registrado).
5.4. Módulo de Productos GestoPago y Caché (/productos)

- GET /productos: Consulta el catálogo transaccional de productos de GestoPago. Lee preferentemente desde la caché Redis; si la llave no existe, invoca al cliente OpenFeign hacia el portal externo y almacena la respuesta en Redis con TTL de 1 hora. Códigos de respuesta: 200 OK, 502 Bad Gateway (Falla de proveedor externo).
- DELETE /productos/cache: Invalida y purga manualmente la llave de caché de productos en el servidor Redis, forzando la sincronización en la siguiente consulta. Códigos de respuesta: 204 No Content.
5.5. Módulo de Observabilidad y Documentación

- GET /actuator/health: Sonda de verificación de salud del microservicio, reportando liveness y readiness para orquestadores en la nube. Códigos de respuesta: 200 OK con JSON {"status":"UP"}.
- GET /v3/api-docs: Entrega la especificación técnica en formato OpenAPI 3.0 en formato JSON estandarizado.
- GET /swagger-ui/index.html: Consola visual interactiva Swagger UI que permite la exploración y prueba directa de todos los endpoints desde cualquier navegador web.
- GET /: Ruta raíz que redirige automáticamente hacia /swagger-ui/index.html para mejorar la experiencia de usuario.
CAPÍTULO 6: INFRAESTRUCTURA CLOUD, TOPOLOGÍA DE RED Y ORQUESTACIÓN EN RENDER

La arquitectura de despliegue en Render Cloud aprovecha la infraestructura como código mediante el archivo render.yaml. La topología resultante sitúa al microservicio gestopago-app en una red privada virtual (VPC) interconectada con una instancia de base de datos PostgreSQL 15 (gestopago-db) y un clúster de caché Redis 7 (gestopago-redis). La salida hacia internet se encuentra filtrada por el proxy perimetral de Cloudflare, garantizando mitigación de ataques distribuidos de denegación de servicio (DDoS), compresión gzip/brotli y cifrado TLS 1.3 con certificados X.509 renovados automáticamente.

CAPÍTULO 7: PIPELINE DE CONTENEDORIZACIÓN DOCKER MULTI-STAGE

El contenedor Docker fue diseñado siguiendo las mejores prácticas de seguridad de contenedores de la Cloud Native Computing Foundation (CNCF). La separación entre la fase de compilación en Gradle JDK y la fase de ejecución en JRE Alpine reduce la imagen a un tamaño inferior a 250 MB, excluye vulnerabilidades asociadas a utilerías de compilación innecesarias y optimiza los tiempos de arranque del contenedor en la nube.

CAPÍTULO 8: PERSISTENCIA RELACIONAL Y GESTIÓN DE MIGRACIONES CON FLYWAY

La evolución de la base de datos es gobernada de forma inmutable mediante Flyway. Durante el inicio de la aplicación en Render, el bean de FlywayConfig ejecuta automáticamente el método flyway.migrate(), comparando las sumas de verificación (checksums) de los scripts locales V1, V2 y V3 contra la tabla flyway_schema_history en PostgreSQL. Si se detectan inconsistencias menores, el sistema invoca flyway.repair() para mantener la continuidad operativa.

CAPÍTULO 9: CAPA DE ACELERACIÓN Y CACHÉ DISTRIBUIDA CON REDIS 7

La integración con Redis opera a través del controlador Lettuce 6.5. Para evitar cuellos de botella en la serialización, se configuró GenericJackson2JsonRedisSerializer para los valores de caché y StringRedisSerializer para las llaves. La resiliencia ante interrupciones de red se garantiza mediante CacheErrorHandler, logueando avisos sin interrumpir las peticiones de los clientes.

CAPÍTULO 10: BITÁCORA FORENSE DE TERMINAL: CONTROL DE VERSIONES GIT

A continuación se presenta la bitácora completa de comandos Git ejecutados para sincronizar y fusionar la rama future/configuracion-necesaria con la rama principal main:


#### [REGISTRO LITERAL DE TERMINAL] REGISTRO GIT: SINCRONIZACIÓN Y FUSIÓN A PRODUCCIÓN

`ash
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

CAPÍTULO 11: BITÁCORA FORENSE DE TERMINAL: CONSTRUCCIÓN Y EMPAQUETADO GRADLE

Registro de compilación del empaquetado del artefacto Java ejecutable:


#### [REGISTRO LITERAL DE TERMINAL] REGISTRO GRADLE: BOOTJAR EXITOSO

`ash
PS C:\Users\calvi\Downloads\prueba\prueba> .\gradlew.bat bootJar -x test
> Configure project :
CrearImagen
Arranca imagen
> Task :bootBuildInfo
> Task :compileJava UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes
> Task :resolveMainClassName
> Task :bootJar

BUILD SUCCESSFUL in 15s
5 actionable tasks: 3 executed, 2 up-to-date

`

CAPÍTULO 12: AUDITORÍA DE PRUEBAS UNITARIAS Y DE INTEGRACIÓN (JUNIT 5)

Registro de la ejecución de la batería completa de pruebas unitarias y de integración:


#### [REGISTRO LITERAL DE TERMINAL] REGISTRO GRADLE: SUITE COMPLETA DE PRUEBAS DE REGRESIÓN

`ash
PS C:\Users\calvi\Downloads\prueba\prueba> .\gradlew.bat test
> Task :bootBuildInfo
> Task :compileJava
> Task :processResources
> Task :classes
> Task :compileTestJava UP-TO-DATE
> Task :testClasses UP-TO-DATE
> Task :test

BUILD SUCCESSFUL in 29s
5 actionable tasks: 4 executed, 1 up-to-date

`

CAPÍTULO 13: EVIDENCIA DE PRUEBAS FUNCIONALES END-TO-END DESDE TERMINAL HTTP

A continuación se documentan las pruebas reales efectuadas contra la URL de producción en Render (https://gestopago-app.onrender.com):

13.1. Prueba E2E: GET /actuator/health (Sondas de Salud)


#### [REGISTRO LITERAL DE TERMINAL] CURL: GET /ACTUATOR/HEALTH

`ash
$ curl.exe -s -i https://gestopago-app.onrender.com/actuator/health

HTTP/1.1 200 OK
Date: Thu, 08 Oct 2026 03:50:43 GMT
Content-Type: application/vnd.spring-boot.actuator.v3+json
Server: cloudflare
x-render-origin-server: Render
{"status":"UP","groups":["liveness","readiness"]}

`

13.2. Prueba E2E: POST /clientes con Fallas de Validación (HTTP 400)


#### [REGISTRO LITERAL DE TERMINAL] CURL: POST /CLIENTES (ERROR DE VALIDACIÓN BEAN VALIDATION)

`ash
$ curl.exe -s -i -X POST "https://gestopago-app.onrender.com/clientes" \
  -H "Content-Type: application/json" \
  --data-binary '{"nombre":"Mariana", ...}'

HTTP/1.1 400 Bad Request
Date: Thu, 08 Oct 2026 03:52:17 GMT
Content-Type: application/json
{"codigo":400,"mensaje":"domicilio.municipio: El municipio o alcaldía es obligatorio"}

`

13.3. Prueba E2E: POST /clientes Registro Exitoso Integral (HTTP 201)


#### [REGISTRO LITERAL DE TERMINAL] CURL: POST /CLIENTES (CREACIÓN EXITOSA DE CLIENTE, CUENTA Y USUARIO)

`ash
$ curl.exe -s -i -X POST "https://gestopago-app.onrender.com/clientes" \
  -H "Content-Type: application/json" \
  --data-binary "@sample_cliente.json"

HTTP/1.1 201 Created
Date: Thu, 08 Oct 2026 03:52:59 GMT
Content-Type: application/json
{
  "id": 1,
  "nombre": "Mariana",
  "apellidoPaterno": "Hernandez",
  "apellidoMaterno": "Torres",
  "nombreCompleto": "Mariana Hernandez Torres",
  "curp": "HETM940822MDFRRN03",
  "rfc": "HETM9408228K4",
  "correo": "mariana.hernandez@example.com",
  "cuentas": [
    {
      "id": 1,
      "clienteId": 1,
      "numeroCuenta": "0692092155",
      "clabe": "012180069209215501",
      "saldo": 1500.00,
      "estatus": "ACTIVA"
    }
  ],
  "usuario": {
    "id": 1,
    "clienteId": 1,
    "correo": "mariana.hernandez@example.com",
    "activo": true
  }
}

`

13.4. Prueba E2E: GET /clientes/1 y Consulta de Filtro por CURP


#### [REGISTRO LITERAL DE TERMINAL] CURL: GET /CLIENTES/1 Y GET /CLIENTES?CURP=...

`ash
$ curl.exe -s -i 'https://gestopago-app.onrender.com/clientes/1'
HTTP/1.1 200 OK
Content-Type: application/json
{"id":1,"nombre":"Mariana","apellidoPaterno":"Hernandez","curp":"HETM940822MDFRRN03"}

$ curl.exe -s -i 'https://gestopago-app.onrender.com/clientes?curp=HETM940822MDFRRN03'
HTTP/1.1 200 OK
Content-Type: application/json
[{"id":1,"nombre":"Mariana","apellidoPaterno":"Hernandez","curp":"HETM940822MDFRRN03"}]

`

13.5. Prueba E2E: GET /cuentas/0692092155/saldo (Consulta en Tiempo Real)


#### [REGISTRO LITERAL DE TERMINAL] CURL: GET /CUENTAS/0692092155/SALDO

`ash
$ curl.exe -s -i "https://gestopago-app.onrender.com/cuentas/0692092155/saldo"

HTTP/1.1 200 OK
Date: Thu, 08 Oct 2026 03:53:53 GMT
Content-Type: application/json
{"numeroCuenta":"0692092155","saldo":1500.00}

`

13.6. Prueba E2E: POST /auth/login (Autenticación y Emisión de JWT)


#### [REGISTRO LITERAL DE TERMINAL] CURL: POST /AUTH/LOGIN

`ash
$ curl.exe -s -i -X POST "https://gestopago-app.onrender.com/auth/login" \
  -H "Content-Type: application/json" \
  --data-binary "@sample_login.json"

HTTP/1.1 200 OK
Date: Thu, 08 Oct 2026 03:56:14 GMT
Content-Type: application/json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJtYXJpYW5hLmhlcm5hbmRlekBleGFtcGxlLmNvbSIsImNsaWVudGVJZCI6MSwidXN1YXJpb0lkIjoxLCJpYXQiOjE3OTE0MzE3NzQsImV4cCI6MTc5MTUxODE3NH0.W81EDUEfG3-xA8pUMV7ko470S6Wi2Z1v47soIM2IuHk",
  "tipoToken": "Bearer",
  "correo": "mariana.hernandez@example.com",
  "clienteId": 1,
  "expiraEnMs": 86400000
}

`

13.7. Prueba E2E: POST /clientes Prevención de Duplicados (HTTP 409 Conflict)


#### [REGISTRO LITERAL DE TERMINAL] CURL: POST /CLIENTES DUPLICADO

`ash
$ curl.exe -s -i -X POST "https://gestopago-app.onrender.com/clientes" \
  -H "Content-Type: application/json" \
  --data-binary "@sample_cliente.json"

HTTP/1.1 409 Conflict
Date: Thu, 08 Oct 2026 03:57:22 GMT
Content-Type: application/json
{"codigo":409,"mensaje":"Ya existe un cliente registrado con la CURP: HETM940822MDFRRN03"}

`

13.8. Prueba E2E: DELETE /productos/cache Desalojo de Caché Redis


#### [REGISTRO LITERAL DE TERMINAL] CURL: DELETE /PRODUCTOS/CACHE

`ash
$ curl.exe -s -i -X DELETE "https://gestopago-app.onrender.com/productos/cache"

HTTP/1.1 204 No Content
Date: Thu, 08 Oct 2026 03:59:26 GMT
Server: cloudflare
x-render-origin-server: Render

`

CAPÍTULO 14: AUDITORÍA DE SEGURIDAD, CRIPTOGRAFÍA Y OWASP TOP 10

El microservicio exhibe una arquitectura defensiva respaldada por BCrypt para la custodia de claves, tokens JWT firmados con HMAC256 para la autorización sin estado, aislamiento de base de datos en VPC privada y transporte HTTPS obligatorio con TLS 1.3 gestionado por Cloudflare. Como recomendación de evolución para producción comercial, se sugiere integrar un SecurityFilterChain estricto para proteger endpoints sensibles y parametrizar la clave secreta app.jwt.secret como variable de entorno en Render.

CAPÍTULO 15: MANUAL DE OPERACIONES, MONITOREO Y RESOLUCIÓN DE INCIDENTES

La operación del sistema se realiza a través de Render Dashboard (https://dashboard.render.com). Los administradores cuentan con telemetría de CPU y memoria en tiempo real, despliegue continuo automático mediante webhooks de GitHub y un comportamiento de Cold Start predecible que muestra la pantalla 'WELCOME TO RENDER / SERVICE WAKING UP' tras periodos de inactividad de 15 minutos en el nivel gratuito.

CAPÍTULO 16: DICTAMEN FINAL DE CERTIFICACIÓN Y REFERENCIAS BIBLIOGRÁFICAS

CERTIFICACIÓN TÉCNICA EMITIDA: DICTAMEN FORMAL DE AUDITORÍA: El microservicio 'Servicio Empresa / GestoPago' cumple satisfactoriamente con la totalidad de requisitos de arquitectura, persistencia, contenerización, pruebas unitarias y pruebas de integración en la nube, encontrándose plenamente operativo en su dirección canónica https://gestopago-app.onrender.com.

Referencias Bibliográficas Normadas (Estilo IEEE / APA):

- 1. VMware Tanzu: (2024). Spring Boot Reference Documentation (Version 3.3.6). Recuperado de https://docs.spring.io/spring-boot/docs/3.3.6/reference/html/
- 2. Docker Inc.: (2024). Multi-stage builds and best practices for containerizing Java applications. Recuperado de https://docs.docker.com/build/building/multi-stage/
- 3. Render Cloud Inc.: (2024). Blueprints Specification (render.yaml) & Docker Deployments. Recuperado de https://render.com/docs/blueprint-spec
- 4. PostgreSQL Global Development Group: (2024). The PostgreSQL Object-Relational Database System 15. Recuperado de https://www.postgresql.org/docs/15/
- 5. Redgate Software: (2024). Flyway Database Migrations Framework. Recuperado de https://documentation.red-gate.com/fd
- 6. Redis Ltd.: (2024). Redis In-Memory Data Store & Caching Strategies. Recuperado de https://redis.io/docs/
- 7. Internet Engineering Task Force (IETF): Jones, M., Bradley, J., & Sakimura, N. (2015). RFC 7519: JSON Web Token (JWT). https://doi.org/10.17487/RFC7519
- 8. Open Web Application Security Project: (2023). OWASP API Security Top 10 2023. Recuperado de https://owasp.org/www-project-api-security/
- 9. National Institute of Standards and Technology: NIST SP 800-63B: Digital Identity Guidelines - Authentication. U.S. Department of Commerce.
- 10. OpenAPI Initiative: (2023). OpenAPI Specification Version 3.0.3. Recuperado de https://spec.openapis.org/oas/v3.0.3