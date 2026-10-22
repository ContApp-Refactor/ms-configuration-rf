-- ==============================================================================
-- CIUDADES DE GUAINÍA - COLOMBIA
-- Todos los municipios del departamento de Guainía
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Guainía - Municipios
('94001', '94', 'COL', 'Inírida'),
('94343', '94', 'COL', 'Barranco Minas'),
('94663', '94', 'COL', 'Mapiripana'),
('94883', '94', 'COL', 'San Felipe'),
('94884', '94', 'COL', 'Puerto Colombia'),
('94885', '94', 'COL', 'La Guadalupe'),
('94886', '94', 'COL', 'Cacahual'),
('94887', '94', 'COL', 'Pana Pana'),
('94888', '94', 'COL', 'Morichal')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;