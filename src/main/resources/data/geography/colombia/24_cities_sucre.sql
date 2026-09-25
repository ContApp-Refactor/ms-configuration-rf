-- ==============================================================================
-- CIUDADES DE SUCRE - COLOMBIA
-- Todos los municipios del departamento de Sucre
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Sucre - Municipios
('70001', '70', 'COL', 'Sincelejo'),
('70110', '70', 'COL', 'Buenavista'),
('70124', '70', 'COL', 'Caimito'),
('70204', '70', 'COL', 'Coloso'),
('70215', '70', 'COL', 'Corozal'),
('70221', '70', 'COL', 'Coveñas'),
('70230', '70', 'COL', 'Chalán'),
('70233', '70', 'COL', 'El Roble'),
('70235', '70', 'COL', 'Galeras'),
('70265', '70', 'COL', 'Guaranda'),
('70400', '70', 'COL', 'La Unión'),
('70418', '70', 'COL', 'Los Palmitos'),
('70429', '70', 'COL', 'Majagual'),
('70473', '70', 'COL', 'Morroa'),
('70508', '70', 'COL', 'Ovejas'),
('70523', '70', 'COL', 'Palmito'),
('70670', '70', 'COL', 'Sampués'),
('70678', '70', 'COL', 'San Benito Abad'),
('70702', '70', 'COL', 'San Juan de Betulia'),
('70708', '70', 'COL', 'San Marcos'),
('70713', '70', 'COL', 'San Onofre'),
('70717', '70', 'COL', 'San Pedro'),
('70742', '70', 'COL', 'San Luis de Sincé'),
('70771', '70', 'COL', 'Sucre'),
('70820', '70', 'COL', 'Santiago de Tolú'),
('70823', '70', 'COL', 'Toluviejo')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;