-- ==============================================================================
-- CIUDADES DE VICHADA - COLOMBIA
-- Todos los municipios del departamento de Vichada
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Vichada - Municipios
('99001', '99', 'COL', 'Puerto Carreño'),
('99524', '99', 'COL', 'La Primavera'),
('99624', '99', 'COL', 'Santa Rosalía'),
('99773', '99', 'COL', 'Cumaribo')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;