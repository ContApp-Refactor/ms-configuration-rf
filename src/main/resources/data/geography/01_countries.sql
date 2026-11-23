-- ==============================================================================
-- PAÍSES - Datos base
-- ==============================================================================

INSERT INTO countries (co_code, co_name) VALUES
('COL', 'Colombia')
ON CONFLICT (co_code) DO NOTHING;
