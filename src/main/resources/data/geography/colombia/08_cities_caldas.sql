-- ==============================================================================
-- CIUDADES DE CALDAS - COLOMBIA
-- Todos los municipios del departamento de Caldas
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Caldas - Municipios
('17001', '17', 'COL', 'Manizales'),
('17013', '17', 'COL', 'Aguadas'),
('17042', '17', 'COL', 'Anserma'),
('17050', '17', 'COL', 'Aranzazu'),
('17088', '17', 'COL', 'Belalcázar'),
('17174', '17', 'COL', 'Chinchiná'),
('17272', '17', 'COL', 'Filadelfia'),
('17380', '17', 'COL', 'La Dorada'),
('17388', '17', 'COL', 'La Merced'),
('17433', '17', 'COL', 'Manzanares'),
('17442', '17', 'COL', 'Marmato'),
('17444', '17', 'COL', 'Marquetalia'),
('17446', '17', 'COL', 'Marulanda'),
('17486', '17', 'COL', 'Neira'),
('17495', '17', 'COL', 'Norcasia'),
('17513', '17', 'COL', 'Pácora'),
('17524', '17', 'COL', 'Palestina'),
('17541', '17', 'COL', 'Pensilvania'),
('17614', '17', 'COL', 'Riosucio'),
('17616', '17', 'COL', 'Risaralda'),
('17653', '17', 'COL', 'Salamina'),
('17662', '17', 'COL', 'Samaná'),
('17665', '17', 'COL', 'San José'),
('17777', '17', 'COL', 'Supía'),
('17867', '17', 'COL', 'Victoria'),
('17873', '17', 'COL', 'Villamaría'),
('17877', '17', 'COL', 'Viterbo')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;