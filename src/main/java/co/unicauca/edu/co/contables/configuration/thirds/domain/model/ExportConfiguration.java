package co.unicauca.edu.co.contables.configuration.thirds.domain.model;

import co.unicauca.edu.co.contables.configuration.thirds.domain.enums.ExportableField;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * @brief Configuración de exportación para terceros
 *
 * Implementa el patrón Builder para construcción flexible de configuraciones
 * que determinan qué campos geográficos opcionales incluir en las exportaciones.
 */
public class ExportConfiguration {

    private final Set<ExportableField> includedFields;

    // Constructor privado - solo accesible desde Builder
    private ExportConfiguration(Builder builder) {
        this.includedFields = Collections.unmodifiableSet(builder.includedFields);
    }

    /**
     * @brief Obtiene el conjunto de campos incluidos en la exportación
     * @return conjunto inmutable de campos que serán exportados
     */
    public Set<ExportableField> getIncludedFields() {
        return includedFields;
    }

    /**
     * @brief Verifica si un campo específico debe incluirse en la exportación
     * @param field campo a verificar
     * @return true si el campo debe incluirse, false en caso contrario
     */
    public boolean includes(ExportableField field) {
        return includedFields.contains(field);
    }

    /**
     * @brief Crea un nuevo Builder para construcción de configuraciones
     * @return nueva instancia del Builder para configuración fluida
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @brief Builder para construcción flexible de ExportConfiguration
     *
     * Implementa patrón Builder con método fluent para permitir
     * configuración progresiva de qué campos incluir en la exportación.
     */
    public static class Builder {
        private final Set<ExportableField> includedFields;

        private Builder() {
            this.includedFields = EnumSet.noneOf(ExportableField.class);
        }

        /**
         * @brief Incluye un campo específico en la exportación
         * @param field campo a incluir
         * @return este Builder para encadenamiento fluido
         */
        public Builder includeField(ExportableField field) {
            if (field != null) {
                this.includedFields.add(field);
            }
            return this;
        }

        /**
         * @brief Incluye múltiples campos en la exportación
         * @param fields conjunto de campos a incluir
         * @return este Builder para encadenamiento fluido
         */
        public Builder includeFields(Set<ExportableField> fields) {
            if (fields != null) {
                this.includedFields.addAll(fields);
            }
            return this;
        }

        /**
         * @brief Incluye todos los campos disponibles en la exportación
         * @return este Builder para encadenamiento fluido
         */
        public Builder includeAllFields() {
            this.includedFields.addAll(EnumSet.allOf(ExportableField.class));
            return this;
        }

        /**
         * @brief Excluye un campo específico de la exportación
         * @param field campo a excluir
         * @return este Builder para encadenamiento fluido
         */
        public Builder excludeField(ExportableField field) {
            if (field != null) {
                this.includedFields.remove(field);
            }
            return this;
        }

        /**
         * @brief Limpia todos los campos incluidos
         *
         * Reinicia la configuración, removiendo todos los campos previamente incluidos.
         * @return este Builder para encadenamiento fluido
         */
        public Builder clear() {
            this.includedFields.clear();
            return this;
        }

        /**
         * @brief Construye la configuración inmutable
         *
         * Crea una instancia final de ExportConfiguration con la configuración
         * acumulada, convirtiéndola en inmutable.
         * @return ExportConfiguration construida e inmutable
         */
        public ExportConfiguration build() {
            return new ExportConfiguration(this);
        }
    }
}
