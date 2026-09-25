-- ==============================================================================
-- CIUDADES DE CÓRDOBA - COLOMBIA
-- Todos los municipios del departamento de Córdoba
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Córdoba - Municipios
('23001', '23', 'COL', 'Montería'),
('23068', '23', 'COL', 'Ayapel'),
('23079', '23', 'COL', 'Buenavista'),
('23090', '23', 'COL', 'Canalete'),
('23162', '23', 'COL', 'Cereté'),
('23168', '23', 'COL', 'Chimá'),
('23182', '23', 'COL', 'Chinú'),
('23189', '23', 'COL', 'Ciénaga de Oro'),
('23300', '23', 'COL', 'Cotorra'),
('23350', '23', 'COL', 'La Apartada'),
('23417', '23', 'COL', 'Lorica'),
('23419', '23', 'COL', 'Los Córdobas'),
('23464', '23', 'COL', 'Momil'),
('23466', '23', 'COL', 'Montelíbano'),
('23500', '23', 'COL', 'Moñitos'),
('23555', '23', 'COL', 'Planeta Rica'),
('23570', '23', 'COL', 'Pueblo Nuevo'),
('23574', '23', 'COL', 'Puerto Escondido'),
('23580', '23', 'COL', 'Puerto Libertador'),
('23586', '23', 'COL', 'Purísima'),
('23660', '23', 'COL', 'Sahagún'),
('23670', '23', 'COL', 'San Andrés Sotavento'),
('23672', '23', 'COL', 'San Antero'),
('23675', '23', 'COL', 'San Bernardo del Viento'),
('23678', '23', 'COL', 'San Carlos'),
('23682', '23', 'COL', 'San José de Uré'),
('23686', '23', 'COL', 'San Pelayo'),
('23807', '23', 'COL', 'Tierralta'),
('23815', '23', 'COL', 'Tuchín'),
('23855', '23', 'COL', 'Valencia')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;