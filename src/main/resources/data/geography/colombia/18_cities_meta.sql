-- ==============================================================================
-- CIUDADES DE META - COLOMBIA
-- Todos los municipios del departamento de Meta
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Meta - Municipios
('50001', '50', 'COL', 'Villavicencio'),
('50006', '50', 'COL', 'Acacías'),
('50110', '50', 'COL', 'Barranca de Upía'),
('50124', '50', 'COL', 'Cabuyaro'),
('50150', '50', 'COL', 'Castilla la Nueva'),
('50223', '50', 'COL', 'Cubarral'),
('50226', '50', 'COL', 'Cumaral'),
('50245', '50', 'COL', 'El Calvario'),
('50251', '50', 'COL', 'El Castillo'),
('50270', '50', 'COL', 'El Dorado'),
('50287', '50', 'COL', 'Fuente de Oro'),
('50313', '50', 'COL', 'Granada'),
('50318', '50', 'COL', 'Guamal'),
('50325', '50', 'COL', 'Mapiripán'),
('50330', '50', 'COL', 'Mesetas'),
('50350', '50', 'COL', 'La Macarena'),
('50370', '50', 'COL', 'Uribe'),
('50400', '50', 'COL', 'Lejanías'),
('50450', '50', 'COL', 'Puerto Concordia'),
('50568', '50', 'COL', 'Puerto Gaitán'),
('50573', '50', 'COL', 'Puerto López'),
('50577', '50', 'COL', 'Puerto Lleras'),
('50590', '50', 'COL', 'Puerto Rico'),
('50606', '50', 'COL', 'Restrepo'),
('50680', '50', 'COL', 'San Carlos de Guaroa'),
('50683', '50', 'COL', 'San Juan de Arama'),
('50686', '50', 'COL', 'San Juanito'),
('50689', '50', 'COL', 'San Martín'),
('50711', '50', 'COL', 'Vistahermosa')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;