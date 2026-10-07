-- ===================================================================
-- Migración V3: Onboarding de Clientes Personas Físicas
-- Tablas: clientes, domicilios, cuentas, usuarios
-- ===================================================================

-- 1. Tabla de Clientes (Información Personal, Contacto y Laboral)
CREATE TABLE IF NOT EXISTS clientes (
    id                      SERIAL PRIMARY KEY,
    nombre                  VARCHAR(50)     NOT NULL,
    segundo_nombre          VARCHAR(50),
    apellido_paterno        VARCHAR(50)     NOT NULL,
    apellido_materno        VARCHAR(50)     NOT NULL,
    fecha_nacimiento        DATE            NOT NULL,
    curp                    VARCHAR(18)     NOT NULL UNIQUE,
    rfc                     VARCHAR(13)     NOT NULL UNIQUE,
    sexo                    VARCHAR(20)     NOT NULL,
    nacionalidad            VARCHAR(50)     NOT NULL DEFAULT 'Mexicana',
    estado_civil            VARCHAR(30)     NOT NULL,
    correo                  VARCHAR(100)    NOT NULL UNIQUE,
    telefono_movil          VARCHAR(10)     NOT NULL,
    telefono_alternativo    VARCHAR(10),
    ocupacion               VARCHAR(100)    NOT NULL,
    empresa                 VARCHAR(100)    NOT NULL,
    ingreso_mensual         NUMERIC(12, 2)  NOT NULL CHECK (ingreso_mensual > 0),
    activo                  BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_creacion          TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_actualizacion     TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_clientes_curp ON clientes(curp);
CREATE INDEX IF NOT EXISTS idx_clientes_rfc ON clientes(rfc);
CREATE INDEX IF NOT EXISTS idx_clientes_correo ON clientes(correo);
CREATE INDEX IF NOT EXISTS idx_clientes_activo ON clientes(activo);
CREATE INDEX IF NOT EXISTS idx_clientes_fecha_creacion ON clientes(fecha_creacion);

-- 2. Tabla de Domicilios (Relación 1 a 1 con Cliente)
CREATE TABLE IF NOT EXISTS domicilios (
    id                  SERIAL PRIMARY KEY,
    cliente_id          INTEGER         NOT NULL UNIQUE REFERENCES clientes(id) ON DELETE CASCADE,
    calle               VARCHAR(100)    NOT NULL,
    numero_exterior     VARCHAR(20)     NOT NULL,
    numero_interior     VARCHAR(20),
    colonia             VARCHAR(100)    NOT NULL,
    municipio           VARCHAR(100)    NOT NULL,
    estado              VARCHAR(50)     NOT NULL,
    codigo_postal       VARCHAR(5)      NOT NULL,
    pais                VARCHAR(50)     NOT NULL DEFAULT 'México'
);

CREATE INDEX IF NOT EXISTS idx_domicilios_cliente_id ON domicilios(cliente_id);

-- 3. Tabla de Cuentas Bancarias (Relación 1 a N con Cliente)
CREATE TABLE IF NOT EXISTS cuentas (
    id                  SERIAL PRIMARY KEY,
    cliente_id          INTEGER         NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    numero_cuenta       VARCHAR(10)     NOT NULL UNIQUE,
    clabe               VARCHAR(18)     NOT NULL UNIQUE,
    saldo               NUMERIC(14, 2)  NOT NULL DEFAULT 0.00 CHECK (saldo >= 0),
    estatus             VARCHAR(20)     NOT NULL DEFAULT 'ACTIVA',
    fecha_creacion      TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_cuentas_cliente_id ON cuentas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_cuentas_numero_cuenta ON cuentas(numero_cuenta);
CREATE INDEX IF NOT EXISTS idx_cuentas_estatus ON cuentas(estatus);

-- 4. Tabla de Usuarios de Acceso (Relación 1 a 1 con Cliente para Autenticación)
CREATE TABLE IF NOT EXISTS usuarios (
    id                  SERIAL PRIMARY KEY,
    cliente_id          INTEGER         NOT NULL UNIQUE REFERENCES clientes(id) ON DELETE CASCADE,
    correo              VARCHAR(100)    NOT NULL UNIQUE,
    password            VARCHAR(255)    NOT NULL,
    activo              BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_usuarios_correo ON usuarios(correo);
CREATE INDEX IF NOT EXISTS idx_usuarios_cliente_id ON usuarios(cliente_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_activo ON usuarios(activo);
