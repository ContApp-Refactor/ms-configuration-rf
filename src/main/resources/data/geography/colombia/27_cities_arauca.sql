-- ==============================================================================
-- CIUDADES DE ARAUCA - COLOMBIA
-- Todos los municipios del departamento de Arauca
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Arauca - Municipios
('81001', '81', 'COL', 'Arauca'),
('81065', '81', 'COL', 'Arauquita'),
('81220', '81', 'COL', 'Cravo Norte'),
('81300', '81', 'COL', 'Fortul'),
('81591', '81', 'COL', 'Puerto Rondón'),
('81736', '81', 'COL', 'Saravena'),
('81794', '81', 'COL', 'Tame')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;