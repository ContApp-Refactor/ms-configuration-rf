-- ==============================================================================
-- CIUDADES DE SAN ANDRÉS Y PROVIDENCIA - COLOMBIA
-- Todos los municipios del departamento de San Andrés y Providencia
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- San Andrés y Providencia - Municipios
('88001', '88', 'COL', 'San Andrés'),
('88564', '88', 'COL', 'Providencia')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;