package co.unicauca.edu.co.contables.auxiliary.application.useCase.export.report.builders;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountingMovementBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.DiaryBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.InventoryAndBalancesBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.MajorAndBalancesBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.ThirdPartyBookDTO;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.export.ExportInfo;

import lombok.NoArgsConstructor;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * @brief Constructor del origen de datos para reportes de libros auxiliares.
 *
 * Convierte los datos genéricos del libro (mapas) en DTOs tipados según el
 * tipo de libro auxiliar y los inyecta como {@link JRBeanCollectionDataSource}
 * en el reporte. Incluye utilidades de parseo seguro a {@code Long} y
 * {@code BigDecimal} para tolerar variaciones de tipo en los datos crudos.
 */
@Service
@NoArgsConstructor
public class ReportDataBuilder {
    public void setDataSource(JasperReportBuilder report, ExportInfo exportInfo) {
        List<?> typedData;

        switch(exportInfo.getAuxiliaryBook().getType()){
            case INVENTORY_AND_BALANCES -> typedData = mapAuxBookData(exportInfo.getAuxBookData(), map -> {
                            // 1. Extraer el mapa de la cuenta anidada
                            @SuppressWarnings("unchecked")
                            LinkedHashMap<String, Object> accountMap = (LinkedHashMap<String, Object>) map.get("account");

                            System.out.println("DEBUG (Datos del Mapa): " + map);

                            // 2. Crear el AccountDTO
                            AccountDTO accountDto = new AccountDTO(
                                    (String) accountMap.get("nature"),
                                    safeParseLong(accountMap.get("accountCode")),
                                    (String) accountMap.get("accountDescription")
                            );

                            // 3. Crear el DTO principal
                            return new InventoryAndBalancesBookDTO(
                                    accountDto,
                                    (String) map.get("description"),
                                    safeParseBigDecimal(map.get("value"))
                            );
                        }
                );
            case DIARY -> typedData = mapAuxBookData(exportInfo.getAuxBookData(), map -> {
                            @SuppressWarnings("unchecked")
                            LinkedHashMap<String, Object> accountMap = (LinkedHashMap<String, Object>) map.get("account");

                            AccountDTO accountDto = new AccountDTO(
                                    (String) accountMap.get("nature"),
                                    safeParseLong(accountMap.get("accountCode")),
                                    (String) accountMap.get("accountDescription")
                            );

                            return new DiaryBookDTO(
                                    (String) map.get("date"),
                                    accountDto,
                                    (String) map.get("voucherName"),
                                    (String) map.get("voucherNumber"),
                                    safeParseBigDecimal(map.get("debit")),
                                    safeParseBigDecimal(map.get("credit"))
                            );
                        }
                );
            case MAJOR_AND_BALANCES -> typedData = mapAuxBookData(exportInfo.getAuxBookData(), map -> {
                            @SuppressWarnings("unchecked")
                            LinkedHashMap<String, Object> accountMap = (LinkedHashMap<String, Object>) map.get("account");

                            AccountDTO accountDto = new AccountDTO(
                                    (String) accountMap.get("nature"),
                                    safeParseLong(accountMap.get("accountCode")),
                                    (String) accountMap.get("accountDescription")
                            );

                            return new MajorAndBalancesBookDTO(
                                    accountDto,
                                    safeParseBigDecimal(map.get("initialBalance")),
                                    safeParseBigDecimal(map.get("debitMovement")),
                                    safeParseBigDecimal(map.get("creditMovement")),
                                    safeParseBigDecimal(map.get("finalBalance"))
                            );
                        }
                );
            case ACCOUNT -> typedData = mapAuxBookData(exportInfo.getAuxBookData(), map -> {
                            @SuppressWarnings("unchecked")
                            LinkedHashMap<String, Object> accountMap = (LinkedHashMap<String, Object>) map.get("account");

                            AccountDTO accountDto = new AccountDTO(
                                    (String) accountMap.get("nature"),
                                    safeParseLong(accountMap.get("accountCode")),
                                    (String) accountMap.get("accountDescription")
                            );

                            return new AccountBookDTO(
                                    (String) map.get("date"),
                                    accountDto,
                                    safeParseBigDecimal(map.get("debitMovement")),
                                    safeParseBigDecimal(map.get("creditMovement")),
                                    safeParseBigDecimal(map.get("balanceMovement")),
                                    (String) map.get("thirdPartyId"),
                                    (String) map.get("thirdPartyName"),
                                    (String) map.get("voucherCostCenter"),
                                    (String) map.get("voucherNumber")
                            );
                        }
                );
            case THIRD_PARTY -> // Asumiendo que ThirdPartyBookDTO también tiene un AccountDTO anidado
                typedData = mapAuxBookData(exportInfo.getAuxBookData(), map -> {
                            @SuppressWarnings("unchecked")
                            LinkedHashMap<String, Object> accountMap = (LinkedHashMap<String, Object>) map.get("account");

                            AccountDTO accountDto = new AccountDTO(
                                    (String) accountMap.get("nature"),
                                    safeParseLong(accountMap.get("accountCode")),
                                    (String) accountMap.get("accountDescription")
                            );

                            return new ThirdPartyBookDTO(
                                    (String) map.get("date"),
                                    accountDto,
                                    safeParseBigDecimal(map.get("debitMovement")),
                                    safeParseBigDecimal(map.get("creditMovement")),
                                    safeParseBigDecimal(map.get("balanceMovement")),
                                    (String) map.get("thirdPartyId"),
                                    (String) map.get("thirdPartyName"),
                                    (String) map.get("voucherCostCenter"),
                                    (String) map.get("voucherNumber")
                            );
                        }
                );
            case ACCOUNTING_MOVEMENT -> // Asumiendo que AccountingMovementBookDTO también tiene un AccountDTO anidado
                typedData = mapAuxBookData(exportInfo.getAuxBookData(), map -> {
                            @SuppressWarnings("unchecked")
                            LinkedHashMap<String, Object> accountMap = (LinkedHashMap<String, Object>) map.get("account");

                            AccountDTO accountDto = new AccountDTO(
                                    (String) accountMap.get("nature"),
                                    safeParseLong(accountMap.get("accountCode")),
                                    (String) accountMap.get("accountDescription")
                            );

                            return new AccountingMovementBookDTO(
                                    (String) map.get("voucherType"),
                                    (String) map.get("date"),
                                    (String) map.get("state"),
                                    (String) map.get("thirdPartyId"),
                                    (String) map.get("thirdPartyName"),
                                    accountDto, // Campo de cuenta anidado
                                    safeParseBigDecimal(map.get("initialBalance")),
                                    safeParseBigDecimal(map.get("debitMovement")),
                                    safeParseBigDecimal(map.get("creditMovement")),
                                    safeParseBigDecimal(map.get("netMovement"))
                            );
                        }
                );
            default -> throw new IllegalArgumentException("Unsupported auxiliary book type: " + exportInfo.getAuxiliaryBook().getType());
        }

        JRDataSource dataSource = new JRBeanCollectionDataSource(typedData);
        report.setDataSource(dataSource);

    }

    /**
     * Mapea una lista de datos genéricos (usualmente LinkedHashMap) a una lista de DTOs tipados.
     */
    private <T> List<T> mapAuxBookData(List<?> rawData, Function<LinkedHashMap<String, Object>, T> mapper) {
        return rawData.stream()
                .map(item -> {
                    if (!(item instanceof LinkedHashMap<?,?> rawMap)) {
                        throw new IllegalArgumentException("Unexpected data type: " + item.getClass());
                    }
                    @SuppressWarnings("unchecked")
                    LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) rawMap;
                    return mapper.apply(map);
                })
                .toList();
    }

    /**
     * Convierte de forma segura un Objeto (que puede ser String, Integer, Long, etc.) a Long.
     */
    private Long safeParseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convierte de forma segura un Objeto (que puede ser String, Double, etc.) a BigDecimal.
     */
    private BigDecimal safeParseBigDecimal(Object obj) {
        if (obj == null) {
            return null; // O BigDecimal.ZERO si la lógica de negocio lo prefiere
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        try {
            return new BigDecimal(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
