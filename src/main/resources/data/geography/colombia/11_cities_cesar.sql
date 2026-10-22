-- ==============================================================================
-- CIUDADES DE CESAR - COLOMBIA
-- Todos los municipios del departamento de Cesar
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Cesar - Municipios
('20001', '20', 'COL', 'Valledupar'),
('20011', '20', 'COL', 'Aguachica'),
('20013', '20', 'COL', 'Agustín Codazzi'),
('20032', '20', 'COL', 'Astrea'),
('20045', '20', 'COL', 'Becerril'),
('20060', '20', 'COL', 'Bosconia'),
('20175', '20', 'COL', 'Chimichagua'),
('20178', '20', 'COL', 'Chiriguaná'),
('20228', '20', 'COL', 'Curumaní'),
('20238', '20', 'COL', 'El Copey'),
('20250', '20', 'COL', 'El Paso'),
('20295', '20', 'COL', 'Gamarra'),
('20310', '20', 'COL', 'González'),
('20383', '20', 'COL', 'La Gloria'),
('20400', '20', 'COL', 'La Jagua de Ibirico'),
('20443', '20', 'COL', 'Manaure'),
('20517', '20', 'COL', 'Pailitas'),
('20550', '20', 'COL', 'Pelaya'),
('20570', '20', 'COL', 'Pueblo Bello'),
('20614', '20', 'COL', 'Río de Oro'),
('20621', '20', 'COL', 'La Paz'),
('20710', '20', 'COL', 'San Alberto'),
('20750', '20', 'COL', 'San Diego'),
('20770', '20', 'COL', 'San Martín'),
('20787', '20', 'COL', 'Tamalameque')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;