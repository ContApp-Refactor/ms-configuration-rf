-- ==============================================================================
-- CIUDADES DE GUAVIARE - COLOMBIA
-- Todos los municipios del departamento de Guaviare
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Guaviare - Municipios
('95001', '95', 'COL', 'San José del Guaviare'),
('95015', '95', 'COL', 'Calamar'),
('95025', '95', 'COL', 'El Retorno'),
('95200', '95', 'COL', 'Miraflores')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;