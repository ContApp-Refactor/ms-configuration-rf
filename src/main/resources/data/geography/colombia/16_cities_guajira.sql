-- ==============================================================================
-- CIUDADES DE LA GUAJIRA - COLOMBIA
-- Todos los municipios del departamento de La Guajira
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- La Guajira - Municipios
('44001', '44', 'COL', 'Riohacha'),
('44035', '44', 'COL', 'Albania'),
('44078', '44', 'COL', 'Barrancas'),
('44090', '44', 'COL', 'Dibulla'),
('44098', '44', 'COL', 'Distracción'),
('44110', '44', 'COL', 'El Molino'),
('44279', '44', 'COL', 'Fonseca'),
('44378', '44', 'COL', 'Hatonuevo'),
('44420', '44', 'COL', 'La Jagua del Pilar'),
('44430', '44', 'COL', 'Maicao'),
('44560', '44', 'COL', 'Manaure'),
('44650', '44', 'COL', 'San Juan del Cesar'),
('44847', '44', 'COL', 'Uribia'),
('44855', '44', 'COL', 'Urumita'),
('44874', '44', 'COL', 'Villanueva')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;
