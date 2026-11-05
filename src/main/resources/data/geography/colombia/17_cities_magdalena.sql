-- ==============================================================================
-- CIUDADES DE MAGDALENA - COLOMBIA
-- Todos los municipios del departamento de Magdalena
-- ==============================================================================

INSERT INTO cities (ci_code, st_code, co_code, ci_name) VALUES
-- Magdalena - Municipios
('47001', '47', 'COL', 'Santa Marta'),
('47030', '47', 'COL', 'Algarrobo'),
('47053', '47', 'COL', 'Aracataca'),
('47058', '47', 'COL', 'Ariguaní'),
('47161', '47', 'COL', 'Cerro San Antonio'),
('47170', '47', 'COL', 'Chivolo'),
('47189', '47', 'COL', 'Ciénaga'),
('47205', '47', 'COL', 'Concordia'),
('47245', '47', 'COL', 'El Banco'),
('47258', '47', 'COL', 'El Piñón'),
('47268', '47', 'COL', 'El Retén'),
('47288', '47', 'COL', 'Fundación'),
('47318', '47', 'COL', 'Guamal'),
('47460', '47', 'COL', 'Nueva Granada'),
('47541', '47', 'COL', 'Pedraza'),
('47545', '47', 'COL', 'Pijiño del Carmen'),
('47551', '47', 'COL', 'Pivijay'),
('47555', '47', 'COL', 'Plato'),
('47570', '47', 'COL', 'Puebloviejo'),
('47605', '47', 'COL', 'Remolino'),
('47660', '47', 'COL', 'Sabanas de San Ángel'),
('47675', '47', 'COL', 'Salamina'),
('47692', '47', 'COL', 'San Sebastián de Buenavista'),
('47703', '47', 'COL', 'San Zenón'),
('47707', '47', 'COL', 'Santa Ana'),
('47720', '47', 'COL', 'Santa Bárbara de Pinto'),
('47745', '47', 'COL', 'Sitionuevo'),
('47798', '47', 'COL', 'Tenerife'),
('47960', '47', 'COL', 'Zapayán'),
('47980', '47', 'COL', 'Zona Bananera')
ON CONFLICT (ci_code, st_code, co_code) DO NOTHING;