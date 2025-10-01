package co.unicauca.edu.co.contables.configuration.typesOfDocuments.domain.models;

import lombok.Getter;

/**
 * Enumeración de módulos permitidos para tipos de documentos.
 * Cada módulo tiene un ID único y un nombre.
 */
@Getter
public enum DocumentModule {
    
    INVENTARIO_PROMEDIO_PONDERADO(1, "Inventario promedio ponderado"),
    INVENTARIO_PEPS(2, "Inventario PEPS"),
    COMERCIAL(3, "Comercial"),
    TESORERIA(4, "Tesorería"),
    CARTERA(5, "Cartera"),
    CONTABLE_COMERCIAL(6, "Contable comercial"),
    CONTABLE_CARTERA(7, "Contable cartera"),
    ESTADOS_FINANCIEROS(8, "Estados financieros");

    private final Integer id;
    private final String name;

    DocumentModule(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Busca un módulo por su ID.
     * 
     * @param id ID del módulo
     * @return El módulo correspondiente
     * @throws IllegalArgumentException si el ID no existe
     */
    public static DocumentModule fromId(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del módulo no puede ser nulo");
        }
        
        for (DocumentModule module : values()) {
            if (module.id.equals(id)) {
                return module;
            }
        }
        throw new IllegalArgumentException("No existe un módulo con ID: " + id);
    }

    /**
     * Busca un módulo por su nombre (case-insensitive).
     * 
     * @param name Nombre del módulo
     * @return El módulo correspondiente
     * @throws IllegalArgumentException si el nombre no existe
     */
    public static DocumentModule fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del módulo no puede ser nulo o vacío");
        }
        
        String normalizedName = name.trim().toLowerCase();
        
        for (DocumentModule module : values()) {
            if (module.name.toLowerCase().equals(normalizedName)) {
                return module;
            }
        }
        throw new IllegalArgumentException("No existe un módulo con nombre: " + name);
    }

    /**
     * Verifica si un nombre de módulo es válido.
     * 
     * @param name Nombre del módulo a validar
     * @return true si el módulo existe, false en caso contrario
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String normalizedName = name.trim().toLowerCase();
        
        for (DocumentModule module : values()) {
            if (module.name.toLowerCase().equals(normalizedName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si un ID de módulo es válido.
     * 
     * @param id ID del módulo a validar
     * @return true si el módulo existe, false en caso contrario
     */
    public static boolean isValidId(Integer id) {
        if (id == null) {
            return false;
        }
        
        for (DocumentModule module : values()) {
            if (module.id.equals(id)) {
                return true;
            }
        }
        return false;
    }
}
