package co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums;

import lombok.Getter;

/**
 * @brief Clasificaciones contables principales según normas internacionales
 *
 * Define las categorías de clasificación para cuentas contables basadas en
 * principios de contabilidad generalmente aceptados.
 */
@Getter
public enum ClassificationEnum {
    CURRENTASSETS("Activo Corriente"),
    NONCURRENTASSETS("Activo No Corriente"),
    CURRENTLIABILITIES("Pasivo Corriente"),
    NONCURRENTLIABILITIES("Pasivo No Corriente"),
    EQUITY("Patrimonio"),
    OPERATINGREVENUES("Ingresos Operacionales"),
    NONOPERATINGINCOME("Ingresos No Operacionales"),
    OPERATINGEXPENSES("Gastos Operacionales");

    private final String state;

    ClassificationEnum(String state){
     this.state=state;
    }
}
