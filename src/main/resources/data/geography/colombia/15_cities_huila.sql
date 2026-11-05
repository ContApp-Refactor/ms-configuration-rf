-- ==============================================================================
-- CIUDADES DE HUILA - COLOMBIA
-- Todos los municipios del departamento de Huila
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Huila - Municipios
('41001', '41', 'COL', 'Neiva'),
('41006', '41', 'COL', 'Acevedo'),
('41013', '41', 'COL', 'Agrado'),
('41016', '41', 'COL', 'Aipe'),
('41020', '41', 'COL', 'Algeciras'),
('41026', '41', 'COL', 'Altamira'),
('41078', '41', 'COL', 'Baraya'),
('41132', '41', 'COL', 'Campoalegre'),
('41206', '41', 'COL', 'Colombia'),
('41244', '41', 'COL', 'Elías'),
('41298', '41', 'COL', 'Garzón'),
('41306', '41', 'COL', 'Gigante'),
('41319', '41', 'COL', 'Guadalupe'),
('41349', '41', 'COL', 'Hobo'),
('41357', '41', 'COL', 'Íquira'),
('41359', '41', 'COL', 'Isnos'),
('41378', '41', 'COL', 'La Argentina'),
('41396', '41', 'COL', 'La Plata'),
('41483', '41', 'COL', 'Nátaga'),
('41503', '41', 'COL', 'Oporapa'),
('41518', '41', 'COL', 'Paicol'),
('41524', '41', 'COL', 'Palermo'),
('41548', '41', 'COL', 'Palestina'),
('41551', '41', 'COL', 'Pital'),
('41615', '41', 'COL', 'Rivera'),
('41660', '41', 'COL', 'Saladoblanco'),
('41668', '41', 'COL', 'San Agustín'),
('41676', '41', 'COL', 'Santa María'),
('41770', '41', 'COL', 'Suaza'),
('41791', '41', 'COL', 'Tarqui'),
('41797', '41', 'COL', 'Tesalia'),
('41799', '41', 'COL', 'Tello'),
('41801', '41', 'COL', 'Teruel'),
('41807', '41', 'COL', 'Timaná'),
('41872', '41', 'COL', 'Villavieja'),
('41885', '41', 'COL', 'Yaguará')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;