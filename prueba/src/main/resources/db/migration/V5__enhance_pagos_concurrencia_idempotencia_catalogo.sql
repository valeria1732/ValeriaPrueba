-- ===================================================================
-- Migración V5: Idempotencia, Conciliación de Transacciones y Catálogo
-- ===================================================================

ALTER TABLE gestopago_productos
    ADD COLUMN IF NOT EXISTS id_servicio INTEGER;

ALTER TABLE pagos_servicios
    ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100) UNIQUE;

ALTER TABLE pagos_servicios
    ADD COLUMN IF NOT EXISTS estatus_transaccion VARCHAR(40) NOT NULL DEFAULT 'EXITOSA';

ALTER TABLE pagos_servicios
    ADD COLUMN IF NOT EXISTS fecha_actualizacion TIMESTAMP DEFAULT NOW();

CREATE INDEX IF NOT EXISTS idx_pagos_idempotency_key ON pagos_servicios(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_pagos_estatus_tx ON pagos_servicios(estatus_transaccion);
CREATE INDEX IF NOT EXISTS idx_productos_id_producto ON gestopago_productos(id_producto);

-- Productos iniciales en catálogo para validación de montos y servicios
INSERT INTO gestopago_productos (id_producto, id_servicio, codigo, descripcion, categoria, monto_minimo, monto_maximo, comision, activo)
SELECT 202, 101, 'PROD-202', 'Pago de Servicio CFE / Recarga GestoPago', 'SERVICIOS', 10.00, 10000.00, 0.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM gestopago_productos WHERE codigo = 'PROD-202');

INSERT INTO gestopago_productos (id_producto, id_servicio, codigo, descripcion, categoria, monto_minimo, monto_maximo, comision, activo)
SELECT 101, 101, 'PROD-101', 'Recarga Tiempo Aire Telcel', 'TELEFONIA', 10.00, 500.00, 1.50, TRUE
WHERE NOT EXISTS (SELECT 1 FROM gestopago_productos WHERE codigo = 'PROD-101');

