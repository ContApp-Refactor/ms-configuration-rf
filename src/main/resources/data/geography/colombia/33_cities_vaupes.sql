-- ==============================================================================
-- CIUDADES DE VAUPÉS - COLOMBIA
-- Todos los municipios del departamento de Vaupés
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Vaupés - Municipios
('97001', '97', 'COL', 'Mitú'),
('97161', '97', 'COL', 'Caruru'),
('97511', '97', 'COL', 'Pacoa'),
('97666', '97', 'COL', 'Taraira'),
('97777', '97', 'COL', 'Papunaua'),
('97889', '97', 'COL', 'Yavaraté')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;