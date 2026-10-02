package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.infraestructure.utils;

import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.ClassificationEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.FinancialStatusEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;

import lombok.NoArgsConstructor;

/**
 * @brief Utilidad para conversión de strings a enums de catálogo de cuentas
 *
 * Proporciona métodos para convertir cadenas de texto (de requests HTTP)
 * a los correspondientes enums del dominio, validando valores permitidos.
 */
@NoArgsConstructor
public class AdjustEnumAccount {

    /**
     * @brief Convierte string de clasificación a enum correspondiente
     *
     * Mapea las clasificaciones contables en español a sus equivalentes en enum.
     * Valores soportados: Activo Corriente, Activo No Corriente, Pasivo Corriente,
     * Pasivo No Corriente, Patrimonio, Ingresos Operacionales, Ingresos No Operacionales, Gastos Operacionales.
     * @param state string de clasificación en español
     * @return ClassificationEnum correspondiente al string proporcionado
     * @throws IllegalArgumentException si el string no corresponde a una clasificación válida
     */
    public ClassificationEnum adjustClassificationEnum(String state) {
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("La clasificación no puede ser nula o vacía");
        }

        switch (state.trim()) {
            case "Activo Corriente":
                return ClassificationEnum.CURRENTASSETS;
            case "Activo No Corriente":
                return ClassificationEnum.NONCURRENTASSETS;
            case "Pasivo Corriente":
                return ClassificationEnum.CURRENTLIABILITIES;
            case "Pasivo No Corriente":
                return ClassificationEnum.NONCURRENTLIABILITIES;
            case "Patrimonio":
                return ClassificationEnum.EQUITY;
            case "Ingresos No Operacionales":
                return ClassificationEnum.NONOPERATINGINCOME;
            case "Gastos Operacionales":
                return ClassificationEnum.OPERATINGEXPENSES;
            case "Ingresos Operacionales":
                return ClassificationEnum.OPERATINGREVENUES;
            default:
                throw new IllegalArgumentException("Clasificación no válida: " + state);
        }
    }

    /**
     * @brief Convierte string de estado financiero a enum correspondiente
     *
     * Mapea los estados financieros en español a sus equivalentes en enum.
     * Valores soportados: "Estado de Resultados", "Estado de Situacion Financiero".
     * @param state string de estado financiero en español
     * @return FinancialStatusEnum correspondiente al string proporcionado
     * @throws IllegalArgumentException si el string no corresponde a un estado financiero válido
     */
    public FinancialStatusEnum adjustFinancialStatusEnum(String state) {
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("El estado financiero no puede ser nulo o vacío");
        }

        switch (state.trim()) {
            case "Estado de Resultados":
                return FinancialStatusEnum.INCOMESTATEMENT;
            case "Estado de Situacion Financiero":
                return FinancialStatusEnum.STATEMENTFINANCIALPOSITION;
            default:
                throw new IllegalArgumentException("Estado financiero no válido: " + state);
        }
    }


    /**
     * @brief Convierte string de naturaleza contable a enum correspondiente
     *
     * Mapea las naturalezas contables en español a sus equivalentes en enum.
     * Valores soportados: "Credito" (CRÉDITO), "Debito" (DÉBITO).
     * @param state string de naturaleza en español
     * @return NatureEnum correspondiente al string proporcionado
     * @throws IllegalArgumentException si el string no corresponde a una naturaleza válida
     */
    public NatureEnum adjustNatureEnum(String state) {
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("La naturaleza no puede ser nula o vacía");
        }

        switch (state.trim()) {
            case "Credito":
                return NatureEnum.CREDIT;
            case "Debito":
                return NatureEnum.DEBIT;
            default:
                throw new IllegalArgumentException("Naturaleza no válida: " + state);
        }
    }

}
