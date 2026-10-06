CREATE TABLE IF NOT EXISTS gestopago_productos (
    id                      SERIAL PRIMARY KEY,
    id_producto             INTEGER,
    codigo                  VARCHAR(100)    NOT NULL UNIQUE,
    descripcion             VARCHAR(255),
    categoria               VARCHAR(100),
    monto_minimo            NUMERIC(12, 2),
    monto_maximo            NUMERIC(12, 2),
    comision                NUMERIC(12, 2),
    activo                  BOOLEAN         NOT NULL DEFAULT TRUE,
    fecha_sincronizacion    TIMESTAMP       NOT NULL DEFAULT NOW()
);
