-- ==============================================================================
-- CIUDADES DE QUINDÍO - COLOMBIA
-- Todos los municipios del departamento de Quindío
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Quindío - Municipios
('63001', '63', 'COL', 'Armenia'),
('63111', '63', 'COL', 'Buenavista'),
('63130', '63', 'COL', 'Calarcá'),
('63190', '63', 'COL', 'Circasia'),
('63212', '63', 'COL', 'Córdoba'),
('63272', '63', 'COL', 'Filandia'),
('63302', '63', 'COL', 'Génova'),
('63401', '63', 'COL', 'La Tebaida'),
('63470', '63', 'COL', 'Montenegro'),
('63548', '63', 'COL', 'Pijao'),
('63594', '63', 'COL', 'Quimbaya'),
('63690', '63', 'COL', 'Salento')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;