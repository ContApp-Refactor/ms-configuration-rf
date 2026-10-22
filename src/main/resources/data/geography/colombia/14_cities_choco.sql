-- ==============================================================================
-- CIUDADES DE CHOCÓ - COLOMBIA
-- Todos los municipios del departamento de Chocó
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Chocó - Municipios
('27001', '27', 'COL', 'Quibdó'),
('27006', '27', 'COL', 'Acandí'),
('27025', '27', 'COL', 'Alto Baudó'),
('27050', '27', 'COL', 'Atrato'),
('27073', '27', 'COL', 'Bagadó'),
('27075', '27', 'COL', 'Bahía Solano'),
('27077', '27', 'COL', 'Bajo Baudó'),
('27099', '27', 'COL', 'Bojayá'),
('27135', '27', 'COL', 'El Cantón del San Pablo'),
('27150', '27', 'COL', 'Carmen del Darién'),
('27160', '27', 'COL', 'Cértegui'),
('27205', '27', 'COL', 'Condoto'),
('27245', '27', 'COL', 'El Carmen de Atrato'),
('27250', '27', 'COL', 'El Litoral del San Juan'),
('27361', '27', 'COL', 'Istmina'),
('27372', '27', 'COL', 'Juradó'),
('27413', '27', 'COL', 'Lloró'),
('27425', '27', 'COL', 'Medio Atrato'),
('27430', '27', 'COL', 'Medio Baudó'),
('27450', '27', 'COL', 'Medio San Juan'),
('27491', '27', 'COL', 'Nóvita'),
('27495', '27', 'COL', 'Nuquí'),
('27580', '27', 'COL', 'Río Iró'),
('27600', '27', 'COL', 'Río Quito'),
('27615', '27', 'COL', 'Riosucio'),
('27660', '27', 'COL', 'San José del Palmar'),
('27745', '27', 'COL', 'Sipí'),
('27787', '27', 'COL', 'Tadó'),
('27800', '27', 'COL', 'Unguía'),
('27810', '27', 'COL', 'Unión Panamericana')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;