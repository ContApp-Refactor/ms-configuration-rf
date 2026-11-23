-- ==============================================================================
-- CIUDADES DE ATLÁNTICO - COLOMBIA
-- Todos los municipios del departamento de Atlántico
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Atlántico - Municipios
('08001', '08', 'COL', 'Barranquilla'),
('08078', '08', 'COL', 'Baranoa'),
('08137', '08', 'COL', 'Campo de la Cruz'),
('08141', '08', 'COL', 'Candelaria'),
('08296', '08', 'COL', 'Galapa'),
('08372', '08', 'COL', 'Juan de Acosta'),
('08421', '08', 'COL', 'Luruaco'),
('08433', '08', 'COL', 'Malambo'),
('08436', '08', 'COL', 'Manatí'),
('08520', '08', 'COL', 'Palmar de Varela'),
('08549', '08', 'COL', 'Piojó'),
('08558', '08', 'COL', 'Polonuevo'),
('08560', '08', 'COL', 'Ponedera'),
('08573', '08', 'COL', 'Puerto Colombia'),
('08606', '08', 'COL', 'Repelón'),
('08634', '08', 'COL', 'Sabanagrande'),
('08638', '08', 'COL', 'Sabanalarga'),
('08675', '08', 'COL', 'Santa Lucía'),
('08685', '08', 'COL', 'Santo Tomás'),
('08758', '08', 'COL', 'Soledad'),
('08770', '08', 'COL', 'Suan'),
('08832', '08', 'COL', 'Tubará'),
('08849', '08', 'COL', 'Usiacurí')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;