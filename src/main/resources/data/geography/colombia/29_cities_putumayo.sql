-- ==============================================================================
-- CIUDADES DE PUTUMAYO - COLOMBIA
-- Todos los municipios del departamento de Putumayo
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Putumayo - Municipios
('86001', '86', 'COL', 'Mocoa'),
('86219', '86', 'COL', 'Colón'),
('86320', '86', 'COL', 'Orito'),
('86568', '86', 'COL', 'Puerto Asís'),
('86569', '86', 'COL', 'Puerto Caicedo'),
('86571', '86', 'COL', 'Puerto Guzmán'),
('86573', '86', 'COL', 'Puerto Leguízamo'),
('86749', '86', 'COL', 'Sibundoy'),
('86755', '86', 'COL', 'San Francisco'),
('86757', '86', 'COL', 'San Miguel'),
('86760', '86', 'COL', 'Santiago'),
('86865', '86', 'COL', 'Valle del Guamuez'),
('86885', '86', 'COL', 'Villagarzón')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;