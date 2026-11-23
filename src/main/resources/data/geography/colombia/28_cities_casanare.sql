-- ==============================================================================
-- CIUDADES DE CASANARE - COLOMBIA
-- Todos los municipios del departamento de Casanare
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Casanare - Municipios
('85001', '85', 'COL', 'Yopal'),
('85010', '85', 'COL', 'Aguazul'),
('85015', '85', 'COL', 'Chámeza'),
('85125', '85', 'COL', 'Hato Corozal'),
('85136', '85', 'COL', 'La Salina'),
('85139', '85', 'COL', 'Maní'),
('85162', '85', 'COL', 'Monterrey'),
('85225', '85', 'COL', 'Nunchía'),
('85230', '85', 'COL', 'Orocué'),
('85250', '85', 'COL', 'Paz de Ariporo'),
('85263', '85', 'COL', 'Pore'),
('85279', '85', 'COL', 'Recetor'),
('85300', '85', 'COL', 'Sabanalarga'),
('85315', '85', 'COL', 'Sácama'),
('85325', '85', 'COL', 'San Luis de Palenque'),
('85400', '85', 'COL', 'Támara'),
('85410', '85', 'COL', 'Tauramena'),
('85430', '85', 'COL', 'Trinidad'),
('85440', '85', 'COL', 'Villanueva')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;