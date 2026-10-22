-- ==============================================================================
-- CIUDADES DE AMAZONAS - COLOMBIA
-- Todos los municipios del departamento de Amazonas
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Amazonas - Municipios
('91001', '91', 'COL', 'Leticia'),
('91263', '91', 'COL', 'El Encanto'),
('91405', '91', 'COL', 'La Chorrera'),
('91407', '91', 'COL', 'La Pedrera'),
('91430', '91', 'COL', 'La Victoria'),
('91460', '91', 'COL', 'Miriti - Paraná'),
('91530', '91', 'COL', 'Puerto Alegría'),
('91536', '91', 'COL', 'Puerto Arica'),
('91540', '91', 'COL', 'Puerto Nariño'),
('91669', '91', 'COL', 'Puerto Santander'),
('91798', '91', 'COL', 'Tarapacá')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;