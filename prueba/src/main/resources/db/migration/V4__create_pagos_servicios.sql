-- ===================================================================
-- Migración V4: Tabla de Registro de Pagos de Servicios (SendTx)
-- ===================================================================

CREATE TABLE IF NOT EXISTS pagos_servicios (
    id                  SERIAL PRIMARY KEY,
    numero_cuenta       VARCHAR(10)     NOT NULL,
    cliente_id          INTEGER,
    id_servicio         INTEGER         NOT NULL,
    id_producto         INTEGER         NOT NULL,
    precio              NUMERIC(12, 2)  NOT NULL,
    telefono_referencia VARCHAR(50)     NOT NULL,
    distribuidor        INTEGER         NOT NULL,
    bandera             VARCHAR(20),
    codigo_respuesta    INTEGER         NOT NULL,
    mensaje_respuesta   VARCHAR(255),
    folio_autorizacion  VARCHAR(100),
    firma_aes           TEXT,
    saldo_anterior      NUMERIC(14, 2)  NOT NULL,
    saldo_posterior     NUMERIC(14, 2)  NOT NULL,
    fecha_creacion      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pagos_numero_cuenta ON pagos_servicios(numero_cuenta);
CREATE INDEX IF NOT EXISTS idx_pagos_cliente_id ON pagos_servicios(cliente_id);
