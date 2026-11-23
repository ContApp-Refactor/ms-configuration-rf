package co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.MajorAndBalancesBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.AccountingInfoProcessor;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingInfo;

import lombok.NoArgsConstructor;

/**
 * @brief Estrategia para la generación del Libro Mayor y Balances.
 *
 * Calcula, para cada grupo contable del nivel solicitado, el saldo
 * inicial (acumulado hasta antes de {@code startDate}), los totales de
 * débito y crédito del periodo y el saldo final, descartando grupos
 * sin movimientos significativos y respetando la naturaleza de la cuenta.
 */
@NoArgsConstructor
public class MajorAndBalancesStrategy implements IProcessStrategy {

    private static final int MONEY_SCALE = 2;

    @Override
    public List<?> process(
            AuxiliaryBookCriteria criteria,
            List<AccountingInfo> allAccountingData,
            AccountingInfoProcessor accountingInfoProcessor) {

        validateCriteria(criteria);

        if (allAccountingData == null || allAccountingData.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate startDate = criteria.getStartDate();
        LocalDate endDate = criteria.getEndDate();

        List<AccountingInfo> validAccountingData = allAccountingData.stream()
                .filter(Objects::nonNull)
                .filter(info -> info.getAccount() != null && info.getAccount().getCode() != null)
                .filter(info -> info.getDate() != null)
                .toList();

        if (validAccountingData.isEmpty()) {
            return Collections.emptyList();
        }

        List<AccountingInfo> previousPeriodData = validAccountingData.stream()
                .filter(info -> {
                    LocalDate movementDate = toLocalDate(info.getDate());
                    return movementDate.isBefore(startDate);
                })
                .toList();

        List<AccountingInfo> currentPeriodData = validAccountingData.stream()
                .filter(info -> {
                    LocalDate movementDate = toLocalDate(info.getDate());
                    return !movementDate.isBefore(startDate) && !movementDate.isAfter(endDate);
                })
                .toList();

        Map<Long, List<AccountingInfo>> previousPeriodByGroup = groupByCriteriaLevel(previousPeriodData, criteria);
        Map<Long, List<AccountingInfo>> currentPeriodByGroup = groupByCriteriaLevel(currentPeriodData, criteria);

        NavigableSet<Long> allGroupCodes = new TreeSet<>();
        allGroupCodes.addAll(previousPeriodByGroup.keySet());
        allGroupCodes.addAll(currentPeriodByGroup.keySet());

        List<MajorAndBalancesBookDTO> result = new ArrayList<>();

        for (Long groupCode : allGroupCodes) {
            List<AccountingInfo> previousGroupData = previousPeriodByGroup.getOrDefault(groupCode, Collections.emptyList());
            List<AccountingInfo> currentGroupData = currentPeriodByGroup.getOrDefault(groupCode, Collections.emptyList());

            if (previousGroupData.isEmpty() && currentGroupData.isEmpty()) {
                continue;
            }

            AccountingInfo reference = resolveReference(groupCode, currentGroupData, previousGroupData);
            if (reference == null) {
                continue;
            }

            BigDecimal rawInitialBalance = calculateSignedBalance(previousGroupData);
            BigDecimal rawTotalDebit = calculateDebit(currentGroupData);
            BigDecimal rawTotalCredit = calculateCredit(currentGroupData);
            BigDecimal rawFinalBalance = rawInitialBalance.add(calculateSignedBalance(currentGroupData));

            BigDecimal initialBalance = scale(rawInitialBalance);
            BigDecimal totalDebit = scale(rawTotalDebit);
            BigDecimal totalCredit = scale(rawTotalCredit);
            BigDecimal finalBalance = scale(rawFinalBalance);

            if (isZero(initialBalance) && isZero(totalDebit) && isZero(totalCredit)) {
                continue;
            }

            AccountDTO accountDTO = new AccountDTO(
                    resolveNature(reference),
                    groupCode,
                    resolveDescription(groupCode, currentGroupData, previousGroupData)
            );

            result.add(new MajorAndBalancesBookDTO(
                    accountDTO,
                    initialBalance,
                    totalDebit,
                    totalCredit,
                    finalBalance
            ));
        }

        return result.stream()
                .sorted(Comparator.comparing(dto -> dto.getAccount().getAccountCode()))
                .toList();
    }

    private void validateCriteria(AuxiliaryBookCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Los criterios del Libro Mayor son obligatorios.");
        }

        if (criteria.getStartDate() == null || criteria.getEndDate() == null) {
            throw new IllegalArgumentException("El periodo es obligatorio para generar el Libro Mayor.");
        }

        if (criteria.getStartDate().isAfter(criteria.getEndDate())) {
            throw new IllegalArgumentException("El periodo consultado es inválido: la fecha inicial no puede ser posterior a la fecha final.");
        }

        if (criteria.getCriteriaType() == null) {
            throw new IllegalArgumentException("El nivel contable seleccionado no es válido para el Libro Mayor.");
        }

        validateRange(criteria);
    }

    private void validateRange(AuxiliaryBookCriteria criteria) {
        if (criteria.getCriteriaRange() == null) {
            return;
        }

        Long fromRange = criteria.getCriteriaRange().getFromRange();
        Long toRange = criteria.getCriteriaRange().getToRange();

        if (fromRange != null && toRange != null && fromRange > toRange) {
            throw new IllegalArgumentException("El rango de cuentas es inválido: el valor desde no puede ser mayor que el valor hasta.");
        }

        if (!isCompatibleRange(criteria, fromRange) || !isCompatibleRange(criteria, toRange)) {
            throw new IllegalArgumentException("El rango de cuentas no es compatible con el nivel contable seleccionado.");
        }
    }

    private boolean isCompatibleRange(AuxiliaryBookCriteria criteria, Long rangeValue) {
        if (rangeValue == null) {
            return true;
        }

        int digits = String.valueOf(Math.abs(rangeValue)).length();

        return switch (criteria.getCriteriaType()) {
            case NUMBER_CLASS -> digits == 1;
            case GROUP -> digits == 2;
            case ACCOUNT -> digits == 4;
            case SUB_ACCOUNT -> digits == 6;
            case AUXILIARY_ACCOUNT -> digits <= 8;
            default -> false;
        };
    }

    private Map<Long, List<AccountingInfo>> groupByCriteriaLevel(List<AccountingInfo> data, AuxiliaryBookCriteria criteria) {
        Map<Long, List<AccountingInfo>> groupedData = new TreeMap<>();

        for (AccountingInfo info : data) {
            Long groupCode = extractGroupingKey(info, criteria);
            groupedData.computeIfAbsent(groupCode, ignored -> new ArrayList<>()).add(info);
        }

        return groupedData;
    }

    private Long extractGroupingKey(AccountingInfo info, AuxiliaryBookCriteria criteria) {
        String accountCode = info.getAccount().getCode().toString();
        String groupCode = switch (criteria.getCriteriaType()) {
            case NUMBER_CLASS -> accountCode.substring(0, Math.min(accountCode.length(), 1));
            case GROUP -> accountCode.substring(0, Math.min(accountCode.length(), 2));
            case ACCOUNT -> accountCode.substring(0, Math.min(accountCode.length(), 4));
            case SUB_ACCOUNT -> accountCode.substring(0, Math.min(accountCode.length(), 6));
            case AUXILIARY_ACCOUNT -> accountCode.substring(0, Math.min(accountCode.length(), 8));
            default -> throw new IllegalArgumentException("El nivel contable seleccionado no es válido para el Libro Mayor.");
        };

        return Long.parseLong(groupCode);
    }

    private AccountingInfo resolveReference(Long groupCode,List<AccountingInfo> currentGroupData,List<AccountingInfo> previousGroupData) {

        List<AccountingInfo> availableData = !currentGroupData.isEmpty()
                ? currentGroupData
                : previousGroupData;

        // Busca la cuenta cuyo código es exactamente el del nivel agrupador (cuenta padre).
        // Si no existe en los datos (caso habitual con cuentas hoja), toma el primero
        // del grupo, que ya está agrupado correctamente por extractGroupingKey.
        return availableData.stream()
                .filter(info -> matchesExactGroupCode(info, groupCode))
                .findFirst()
                .orElse(availableData.get(0));
    }

    private String resolveDescription(Long groupCode,List<AccountingInfo> currentGroupData,List<AccountingInfo> previousGroupData) {

        List<AccountingInfo> availableData = !currentGroupData.isEmpty()
                ? currentGroupData
                : previousGroupData;

        // Intenta primero el nombre de la cuenta padre del nivel.
        // Si no existe, toma el nombre de la cuenta con el código más bajo del grupo.
        return availableData.stream()
                .filter(info -> matchesExactGroupCode(info, groupCode))
                .map(info -> info.getAccount().getName())
                .filter(name -> name != null && !name.isBlank())
                .findFirst()
                .orElseGet(() -> availableData.stream()
                        .sorted(Comparator.comparing(info -> info.getAccount().getCode()))
                        .map(info -> info.getAccount().getName())
                        .filter(name -> name != null && !name.isBlank())
                        .findFirst()
                        .orElse(""));
    }

    private String resolveNature(AccountingInfo reference) {
        return reference.getAccount() != null ? reference.getAccount().getNature() : null;
    }

    private BigDecimal calculateDebit(List<AccountingInfo> movements) {
        return movements.stream()
                .map(this::extractDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateCredit(List<AccountingInfo> movements) {
        return movements.stream()
                .map(this::extractCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateSignedBalance(List<AccountingInfo> movements) {
        return movements.stream()
                .map(this::calculateSignedMovement)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateSignedMovement(AccountingInfo info) {
        BigDecimal debit = extractDebit(info);
        BigDecimal credit = extractCredit(info);
        String nature = info.getAccount() != null ? info.getAccount().getNature() : null;

        if ("credito".equalsIgnoreCase(nature)) {
            return credit.subtract(debit);
        }

        return debit.subtract(credit);
    }

    private BigDecimal extractDebit(AccountingInfo info) {
        if (info.getAccountingMovement() == null || info.getAccountingMovement().getDebit() == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(info.getAccountingMovement().getDebit());
    }

    private BigDecimal extractCredit(AccountingInfo info) {
        if (info.getAccountingMovement() == null || info.getAccountingMovement().getCredit() == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(info.getAccountingMovement().getCredit());
    }

    private boolean matchesExactGroupCode(AccountingInfo info, Long groupCode) {
        return info.getAccount() != null
                && info.getAccount().getCode() != null
                && groupCode != null
                && groupCode.toString().equals(info.getAccount().getCode().toString());
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private boolean isZero(BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }
}
