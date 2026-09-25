-- ==============================================================================
-- CIUDADES DE BOGOTÁ D.C. - COLOMBIA
-- Todas las localidades del Distrito Capital
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Bogotá D.C. - Localidades
('11001', '11', 'COL', 'Usaquén'),
('11002', '11', 'COL', 'Chapinero'),
('11003', '11', 'COL', 'Santa Fe'),
('11004', '11', 'COL', 'San Cristóbal'),
('11005', '11', 'COL', 'Usme'),
('11006', '11', 'COL', 'Tunjuelito'),
('11007', '11', 'COL', 'Bosa'),
('11008', '11', 'COL', 'Kennedy'),
('11009', '11', 'COL', 'Fontibón'),
('11010', '11', 'COL', 'Engativá'),
('11011', '11', 'COL', 'Suba'),
('11012', '11', 'COL', 'Barrios Unidos'),
('11013', '11', 'COL', 'Teusaquillo'),
('11014', '11', 'COL', 'Los Mártires'),
('11015', '11', 'COL', 'Antonio Nariño'),
('11016', '11', 'COL', 'Puente Aranda'),
('11017', '11', 'COL', 'La Candelaria'),
('11018', '11', 'COL', 'Rafael Uribe Uribe'),
('11019', '11', 'COL', 'Ciudad Bolívar'),
('11020', '11', 'COL', 'Sumapaz')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;