-- ==============================================================================
-- ESTADOS/DEPARTAMENTOS DE COLOMBIA
-- Todos los 32 departamentos + Distrito Capital
-- ==============================================================================

INSERT INTO states (st_code, co_code, st_name) VALUES
('05', 'COL', 'Antioquia'),
('08', 'COL', 'Atlántico'),
('11', 'COL', 'Bogotá D.C.'),
('13', 'COL', 'Bolívar'),
('15', 'COL', 'Boyacá'),
('17', 'COL', 'Caldas'),
('18', 'COL', 'Caquetá'),
('19', 'COL', 'Cauca'),
('20', 'COL', 'Cesar'),
('23', 'COL', 'Córdoba'),
('25', 'COL', 'Cundinamarca'),
('27', 'COL', 'Chocó'),
('41', 'COL', 'Huila'),
('44', 'COL', 'La Guajira'),
('47', 'COL', 'Magdalena'),
('50', 'COL', 'Meta'),
('52', 'COL', 'Nariño'),
('54', 'COL', 'Norte de Santander'),
('63', 'COL', 'Quindío'),
('66', 'COL', 'Risaralda'),
('68', 'COL', 'Santander'),
('70', 'COL', 'Sucre'),
('73', 'COL', 'Tolima'),
('76', 'COL', 'Valle del Cauca'),
('81', 'COL', 'Arauca'),
('85', 'COL', 'Casanare'),
('86', 'COL', 'Putumayo'),
('88', 'COL', 'San Andrés y Providencia'),
('91', 'COL', 'Amazonas'),
('94', 'COL', 'Guainía'),
('95', 'COL', 'Guaviare'),
('97', 'COL', 'Vaupés'),
('99', 'COL', 'Vichada')
ON CONFLICT (st_code, co_code) DO NOTHING;
