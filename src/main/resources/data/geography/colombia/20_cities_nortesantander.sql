-- ==============================================================================
-- CIUDADES DE NORTE DE SANTANDER - COLOMBIA
-- Todos los municipios del departamento de Norte de Santander
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Norte de Santander - Municipios
('54001', '54', 'COL', 'Cúcuta'),
('54003', '54', 'COL', 'Abrego'),
('54051', '54', 'COL', 'Arboledas'),
('54099', '54', 'COL', 'Bochalema'),
('54109', '54', 'COL', 'Bucarasica'),
('54125', '54', 'COL', 'Cácota'),
('54128', '54', 'COL', 'Cachirá'),
('54172', '54', 'COL', 'Chinácota'),
('54174', '54', 'COL', 'Chitagá'),
('54206', '54', 'COL', 'Convención'),
('54223', '54', 'COL', 'Cucutilla'),
('54239', '54', 'COL', 'Durania'),
('54245', '54', 'COL', 'El Carmen'),
('54250', '54', 'COL', 'El Tarra'),
('54261', '54', 'COL', 'El Zulia'),
('54313', '54', 'COL', 'Gramalote'),
('54344', '54', 'COL', 'Hacarí'),
('54347', '54', 'COL', 'Herrán'),
('54377', '54', 'COL', 'Labateca'),
('54385', '54', 'COL', 'La Esperanza'),
('54398', '54', 'COL', 'La Playa'),
('54405', '54', 'COL', 'Los Patios'),
('54418', '54', 'COL', 'Lourdes'),
('54480', '54', 'COL', 'Mutiscua'),
('54498', '54', 'COL', 'Ocaña'),
('54518', '54', 'COL', 'Pamplona'),
('54520', '54', 'COL', 'Pamplonita'),
('54553', '54', 'COL', 'Puerto Santander'),
('54599', '54', 'COL', 'Ragonvalia'),
('54660', '54', 'COL', 'Salazar'),
('54670', '54', 'COL', 'San Calixto'),
('54673', '54', 'COL', 'San Cayetano'),
('54680', '54', 'COL', 'Santiago'),
('54720', '54', 'COL', 'Sardinata'),
('54743', '54', 'COL', 'Silos'),
('54800', '54', 'COL', 'Teorama'),
('54810', '54', 'COL', 'Tibú'),
('54820', '54', 'COL', 'Toledo'),
('54871', '54', 'COL', 'Villa Caro'),
('54874', '54', 'COL', 'Villa del Rosario')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;