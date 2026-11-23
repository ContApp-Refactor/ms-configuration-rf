package co.unicauca.edu.co.contables.audit.infrastructure.adapters.output.export.helper;

import java.util.Map;
import java.util.Set;

public final class AuditOperationTranslationHelper {

    private AuditOperationTranslationHelper() {
    }

    private static final Map<String, String> MODULE_LABELS = Map.ofEntries(
            Map.entry("ACCOUNTING", "Contabilidad"),
            Map.entry("BANKS", "Bancos"),
            Map.entry("PAYMENT_METHODS", "Métodos de pago"),
            Map.entry("CLASSES_OF_DOCUMENTS", "Clases de documentos"),
            Map.entry("INVENTORY", "Inventario"),
            Map.entry("THIRDS", "Terceros"),
            Map.entry("COST_CENTERS", "Centros de costo"),
            Map.entry("TYPE_OF_DOCUMENTS", "Tipos de documento"),
            Map.entry("NO_COMMERCIAL_TAGS", "Etiquetas"),
            Map.entry("TAXES", "Impuestos"),
            Map.entry("ENTERPRISES", "Empresas"),
            Map.entry("CONFIGURATION", "Configuración"));

    private static final Map<String, String> TABLE_LABELS = Map.ofEntries(
            Map.entry("ACCOUNT_CATALOGUE", "Catálogo de cuentas"),
            Map.entry("BANK", "Banco"),
            Map.entry("BANK_ACCOUNT", "Cuenta bancaria"),
            Map.entry("PAYMENT_METHOD", "Método de pago"),
            Map.entry("DOCUMENT_CLASS", "Clase de documento"),
            Map.entry("DOCUMENT_TYPE", "Tipo de documento"),
            Map.entry("TAX", "Impuesto"),
            Map.entry("THIRD", "Tercero"),
            Map.entry("THIRD_TYPE", "Tipo de tercero"),
            Map.entry("TYPE_ID", "Tipo de identificación"),
            Map.entry("COST_CENTER", "Centro de costo"),
            Map.entry("PRODUCT", "Producto"),
            Map.entry("PRODUCT_TYPE", "Tipo de producto"),
            Map.entry("CATEGORY", "Categoría"),
            Map.entry("UNIT_OF_MEASURE", "Unidad de medida"),
            Map.entry("NO_COMMERCIAL_TAG", "Etiqueta no comercial"),
            Map.entry("ENTERPRISE", "Empresa"),
            Map.entry("SUBJECT", "Materia"),
            Map.entry("USER", "Usuario"),
            Map.entry("PROFILE", "Perfil"),
            Map.entry("PERMISSION", "Permiso"));

    private static final Map<String, String> OPERATION_LABELS = Map.of(
            "CREATE", "Creación",
            "UPDATE", "Actualización",
            "DELETE", "Eliminación",
            "ACTIVATE", "Activación",
            "INACTIVATE", "Inactivación");

    private static final Map<String, String> ROLE_LABELS = Map.of(
            "ADMINISTRADOR", "Administrador",
            "PROFESOR", "Profesor",
            "ESTUDIANTE", "Estudiante");

    private static final Set<String> HIDDEN_FIELDS = Set.of(
            "id", "entId", "parentId", "documentClassId", "moduleId",
            "accountingAccountId", "AccountCatalogueId",
            "salesTaxId", "purchaseTaxId", "inventoryId",
            "costId", "saleId", "returnId", "unitOfMeasureId",
            "categoryId", "productTypeId", "typeIdId",
            "salesTaxesCount", "purchaseTaxesCount",
            "crossing", "costCenter", "children");

    private static final Map<String, String> FIELD_LABELS = Map.ofEntries(
            Map.entry("code", "Código"),
            Map.entry("name", "Nombre"),
            Map.entry("description", "Descripción"),
            Map.entry("state", "Estado"),
            Map.entry("nature", "Naturaleza"),
            Map.entry("financialStatus", "Estado financiero"),
            Map.entry("classification", "Clasificación"),
            Map.entry("parentCode", "Código cuenta padre"),
            Map.entry("accountType", "Tipo de cuenta"),
            Map.entry("accountNumber", "Número de cuenta"),
            Map.entry("bank", "Banco"),
            Map.entry("idNumber", "Número de identificación"),
            Map.entry("personType", "Tipo de persona"),
            Map.entry("names", "Nombres"),
            Map.entry("lastNames", "Apellidos"),
            Map.entry("email", "Correo electrónico"),
            Map.entry("phoneNumber", "Teléfono"),
            Map.entry("address", "Dirección"),
            Map.entry("gender", "Género"));

    private static final Map<String, String> VALUE_LABELS = Map.ofEntries(
            Map.entry("DEBIT", "Débito"),
            Map.entry("CREDIT", "Crédito"),
            Map.entry("true", "Activo"),
            Map.entry("false", "Inactivo"),
            Map.entry("NATURAL_PERSON", "Persona natural"),
            Map.entry("LEGAL_ENTITY", "Persona jurídica"),
            Map.entry("M", "Masculino"),
            Map.entry("F", "Femenino"),
            Map.entry("O", "Otro / Prefiere no decir"));

    public static String translateModule(String value) {
        return MODULE_LABELS.getOrDefault(value, value);
    }

    public static String translateTable(String value) {
        return TABLE_LABELS.getOrDefault(value, value);
    }

    public static String translateOperation(String value) {
        return OPERATION_LABELS.getOrDefault(value, value);
    }

    public static String translateRole(String value) {
        return ROLE_LABELS.getOrDefault(value, value);
    }

    public static boolean isVisible(String fieldKey) {
        return !HIDDEN_FIELDS.contains(fieldKey);
    }

    public static String translateFieldKey(String key) {
        return FIELD_LABELS.getOrDefault(key, key);
    }

    public static String translateValue(Object value) {
        if (value == null)
            return "—";
        if (value instanceof Boolean b)
            return b ? "Activo" : "Inactivo";
        return VALUE_LABELS.getOrDefault(String.valueOf(value), String.valueOf(value));
    }

}
