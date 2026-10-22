-- ==============================================================================
-- CIUDADES DE CAQUETÁ - COLOMBIA
-- Todos los municipios del departamento de Caquetá
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Caquetá - Municipios
('18001', '18', 'COL', 'Florencia'),
('18029', '18', 'COL', 'Albania'),
('18094', '18', 'COL', 'Belén de los Andaquíes'),
('18150', '18', 'COL', 'Cartagena del Chairá'),
('18205', '18', 'COL', 'Curillo'),
('18247', '18', 'COL', 'El Doncello'),
('18256', '18', 'COL', 'El Paujil'),
('18410', '18', 'COL', 'La Montañita'),
('18460', '18', 'COL', 'Milán'),
('18479', '18', 'COL', 'Morelia'),
('18592', '18', 'COL', 'Puerto Rico'),
('18610', '18', 'COL', 'San José del Fragua'),
('18753', '18', 'COL', 'San Vicente del Caguán'),
('18756', '18', 'COL', 'Solano'),
('18785', '18', 'COL', 'Solita'),
('18860', '18', 'COL', 'Valparaíso')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;