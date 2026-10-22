package co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils;

import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @brief Servicio de agrupación de información contable por criterio.
 *
 * Reúne los datos contables ya filtrados en grupos según el nivel
 * contable seleccionado (clase, grupo, cuenta, subcuenta o auxiliar)
 * y delega en un mapper proporcionado por la estrategia la construcción
 * del DTO final con los balances acumulados de cada grupo.
 */
@Service
public class AccountingInfoProcessor {

    public <T> List<T> calculateBalancesByCriteriaGroup(
            List<AccountingInfo> filteredList,
            AuxiliaryBookCriteria criteria,
            BiFunction<String, List<AccountingInfo>, T> mapper
    ) {
        return filteredList.stream()
                .collect(Collectors.groupingBy(
                        info -> extractGroupingKey(info.getAccount().getCode().toString(), criteria.getCriteriaType()),
                        TreeMap::new,   // orden lexicográfico determinístico por código de cuenta
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> mapper.apply(entry.getKey(), entry.getValue()))
                .toList();
    }

    private String extractGroupingKey(String accountCode, ECriteriaType type) {
        return switch (type) {
            case NUMBER_CLASS -> accountCode.substring(0, 1);
            case GROUP -> accountCode.substring(0, Math.min(accountCode.length(), 2));
            case ACCOUNT -> accountCode.substring(0, Math.min(accountCode.length(), 4));
            case SUB_ACCOUNT -> accountCode.substring(0, Math.min(accountCode.length(), 6));
            case AUXILIARY_ACCOUNT -> accountCode.length() >= 8
                    ? accountCode.substring(0, 8)
                    : accountCode;
            default -> throw new IllegalArgumentException("Tipo de criterio no soportado para agrupación");
        };
    }
}