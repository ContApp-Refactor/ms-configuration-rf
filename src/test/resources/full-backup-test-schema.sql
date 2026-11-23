-- Tablas que Hibernate no puede generar en H2 (modo PostgreSQL) y que usa FullEnterpriseBackupRoundTripTest.
-- auxiliary_book_history: EState es ordinal y Hibernate emite TINYINT, tipo que H2 en ese modo no conoce.
CREATE TABLE IF NOT EXISTS auxiliary_book_history (
    state SMALLINT NOT NULL,
    auxiliary_book_id BIGINT NOT NULL UNIQUE,
    id BIGINT NOT NULL PRIMARY KEY,
    public_id VARCHAR(255) NOT NULL UNIQUE,
    delivery_way VARCHAR(20)
);
