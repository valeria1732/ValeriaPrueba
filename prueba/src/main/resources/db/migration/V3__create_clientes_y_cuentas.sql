CREATE TABLE IF NOT EXISTS clientes (
    id                  SERIAL PRIMARY KEY,
    nombre              VARCHAR(100)    NOT NULL,
    segundo_nombre      VARCHAR(100),
    apellido_paterno    VARCHAR(100)    NOT NULL,
    apellido_materno    VARCHAR(100),
    fecha_nacimiento    DATE            NOT NULL,
    curp                VARCHAR(18)     NOT NULL UNIQUE,
    rfc                 VARCHAR(13)     NOT NULL UNIQUE,
    telefono            VARCHAR(20),
    email               VARCHAR(150),
    calle               VARCHAR(150),
    numero_exterior     VARCHAR(50),
    colonia             VARCHAR(100),
    codigo_postal       VARCHAR(10),
    ciudad              VARCHAR(100),
    estado              VARCHAR(100),
    puesto_laboral      VARCHAR(100),
    ingreso_mensual     NUMERIC(12, 2)  DEFAULT 0.00,
    activo              BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP       NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cuentas_bancarias (
    id                  SERIAL PRIMARY KEY,
    cliente_id          INTEGER         NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
    numero_cuenta       VARCHAR(20)     NOT NULL UNIQUE,
    clabe               VARCHAR(18)     NOT NULL UNIQUE,
    saldo               NUMERIC(12, 2)  NOT NULL DEFAULT 0.00,
    activo              BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP       NOT NULL DEFAULT NOW()
);
