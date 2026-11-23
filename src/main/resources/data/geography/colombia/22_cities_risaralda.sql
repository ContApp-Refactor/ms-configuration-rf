-- ==============================================================================
-- CIUDADES DE RISARALDA - COLOMBIA
-- Todos los municipios del departamento de Risaralda
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Risaralda - Municipios
('66001', '66', 'COL', 'Pereira'),
('66045', '66', 'COL', 'Apía'),
('66075', '66', 'COL', 'Balboa'),
('66088', '66', 'COL', 'Belén de Umbría'),
('66170', '66', 'COL', 'Dosquebradas'),
('66318', '66', 'COL', 'Guática'),
('66383', '66', 'COL', 'La Celia'),
('66400', '66', 'COL', 'La Virginia'),
('66440', '66', 'COL', 'Marsella'),
('66456', '66', 'COL', 'Mistrató'),
('66572', '66', 'COL', 'Pueblo Rico'),
('66594', '66', 'COL', 'Quinchía'),
('66682', '66', 'COL', 'Santa Rosa de Cabal'),
('66687', '66', 'COL', 'Santuario')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;