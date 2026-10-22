package co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy;

import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountDTO;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.AccountingInfoProcessor;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.CriteriaRange;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingInfo;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @brief Estrategia para la generación del Libro Auxiliar por Cuenta.
 *
 * Genera filas cronológicas de movimientos contables agrupados al nivel
 * contable indicado (cuenta, subcuenta o auxiliar), acumulando el saldo
 * por grupo desde el periodo anterior hasta cada movimiento del periodo
 * consultado, y respetando la naturaleza (débito/crédito) de la cuenta.
 */
@NoArgsConstructor
public class AccountStrategy implements IProcessStrategy {

    private static final int MONEY_SCALE = 2;

    @Override
    public List<?> process(AuxiliaryBookCriteria criteria, List<AccountingInfo> allAccountingData, AccountingInfoProcessor accountingInfoProcessor) {
        validateCriteria(criteria);

        if (allAccountingData == null || allAccountingData.isEmpty()) {
            return Collections.emptyList();
        }

        List<AccountingInfo> validAccountingData = allAccountingData.stream()
                .filter(Objects::nonNull)
                .filter(this::hasMinimumStructure)
                .filter(info -> matchesOptionalCostCenter(info, criteria.getCostCenterId()))
                .filter(info -> matchesSelectedRange(info, criteria))
                .toList();

        if (validAccountingData.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate startDate = criteria.getStartDate();
        LocalDate endDate = criteria.getEndDate();

        List<AccountingInfo> previousPeriodData = validAccountingData.stream()
                .filter(info -> toLocalDate(info.getDate()).isBefore(startDate))
                .toList();

        List<AccountingInfo> currentPeriodData = validAccountingData.stream()
                .filter(info -> {
                    LocalDate movementDate = toLocalDate(info.getDate());
                    return !movementDate.isBefore(startDate) && !movementDate.isAfter(endDate);
                })
                .sorted(buildChronologicalComparator(criteria.getCriteriaType()))
                .toList();

        if (currentPeriodData.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, AccountDTO> accountsByGroup = buildAccountsByGroup(validAccountingData, criteria.getCriteriaType());
        Map<Long, BigDecimal> runningBalanceByGroup = calculateInitialBalances(previousPeriodData, criteria.getCriteriaType(), accountsByGroup);

        List<AccountBookDTO> result = new ArrayList<>();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

        for (AccountingInfo info : currentPeriodData) {
            Long groupCode = extractGroupingKey(info, criteria.getCriteriaType());
            AccountDTO account = accountsByGroup.get(groupCode);

            if (account == null) {
                continue;
            }

            BigDecimal currentBalance = runningBalanceByGroup.getOrDefault(groupCode, BigDecimal.ZERO);
            BigDecimal debit = extractDebit(info);
            BigDecimal credit = extractCredit(info);
            BigDecimal updatedBalance = calculateFinalBalance(currentBalance, debit, credit, account.getNature());

            runningBalanceByGroup.put(groupCode, updatedBalance);

            result.add(new AccountBookDTO(
                    formato.format(info.getDate()),
                    account,
                    scale(debit),
                    scale(credit),
                    scale(updatedBalance),
                    normalizeText(info.getThirdPartyId()),
                    "",
                    resolveCostCenterCode(info),
                    resolveVoucherNumber(info)
            ));
        }

        return result;
    }

    private void validateCriteria(AuxiliaryBookCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Los criterios del Libro Auxiliar por Cuenta son obligatorios.");
        }

        if (criteria.getStartDate() == null || criteria.getEndDate() == null) {
            throw new IllegalArgumentException("El periodo es obligatorio para generar el Libro Auxiliar por Cuenta.");
        }

        if (criteria.getStartDate().isAfter(criteria.getEndDate())) {
            throw new IllegalArgumentException("El periodo consultado es inválido: la fecha inicial no puede ser posterior a la fecha final.");
        }

        if (criteria.getCriteriaType() == null || !isSupportedCriteriaType(criteria.getCriteriaType())) {
            throw new IllegalArgumentException("El nivel contable seleccionado no es válido para el Libro Auxiliar por Cuenta.");
        }

        validateRange(criteria);
    }

    private boolean isSupportedCriteriaType(ECriteriaType criteriaType) {
        return criteriaType == ECriteriaType.ACCOUNT
                || criteriaType == ECriteriaType.SUB_ACCOUNT
                || criteriaType == ECriteriaType.AUXILIARY_ACCOUNT;
    }

    private void validateRange(AuxiliaryBookCriteria criteria) {
        CriteriaRange range = criteria.getCriteriaRange();
        if (range == null) {
            return;
        }

        Long fromRange = range.getFromRange();
        Long toRange = range.getToRange();

        if (fromRange != null && toRange != null && fromRange > toRange) {
            throw new IllegalArgumentException("El rango de cuentas es inválido: el valor desde no puede ser mayor que el valor hasta.");
        }

        if (!isCompatibleRange(criteria.getCriteriaType(), fromRange) || !isCompatibleRange(criteria.getCriteriaType(), toRange)) {
            throw new IllegalArgumentException("El rango de cuentas no es compatible con el nivel contable seleccionado.");
        }
    }

    private boolean isCompatibleRange(ECriteriaType criteriaType, Long rangeValue) {
        if (rangeValue == null) {
            return true;
        }

        int digits = String.valueOf(Math.abs(rangeValue)).length();

        return switch (criteriaType) {
            case ACCOUNT -> digits == 4;
            case SUB_ACCOUNT -> digits == 6;
            case AUXILIARY_ACCOUNT -> digits <= 8;
            default -> false;
        };
    }

    private boolean hasMinimumStructure(AccountingInfo info) {
        return info.getAccount() != null
                && info.getAccount().getCode() != null
                && info.getDate() != null
                && info.getAccountingMovement() != null;
    }

    private boolean matchesOptionalCostCenter(AccountingInfo info, String requestedCostCenterId) {
        if (requestedCostCenterId == null || requestedCostCenterId.isBlank()) {
            return true;
        }

        return info.getCostCenter() != null
                && info.getCostCenter().getCode() != null
                && requestedCostCenterId.trim().equals(info.getCostCenter().getCode().trim());
    }

    private boolean matchesSelectedRange(AccountingInfo info, AuxiliaryBookCriteria criteria) {
        if (criteria.getCriteriaRange() == null) {
            return true;
        }

        Long groupCode = extractGroupingKey(info, criteria.getCriteriaType());
        Long fromRange = criteria.getCriteriaRange().getFromRange();
        Long toRange = criteria.getCriteriaRange().getToRange();

        boolean matchesFrom = fromRange == null || groupCode >= fromRange;
        boolean matchesTo = toRange == null || groupCode <= toRange;

        return matchesFrom && matchesTo;
    }

    private Comparator<AccountingInfo> buildChronologicalComparator(ECriteriaType criteriaType) {
        return Comparator.comparing(AccountingInfo::getDate)
                .thenComparing(info -> extractGroupingKey(info, criteriaType))
                .thenComparing(info -> info.getAccount().getCode())
                .thenComparing(info -> normalizeText(resolveVoucherNumber(info)))
                .thenComparing(info -> normalizeText(info.getThirdPartyId()));
    }

    private Map<Long, AccountDTO> buildAccountsByGroup(List<AccountingInfo> validAccountingData, ECriteriaType criteriaType) {
        Map<Long, List<AccountingInfo>> groupedData = validAccountingData.stream()
                .collect(Collectors.groupingBy(
                        info -> extractGroupingKey(info, criteriaType),
                        TreeMap::new,
                        Collectors.toList()
                ));

        Map<Long, AccountDTO> accountsByGroup = new HashMap<>();

        groupedData.forEach((groupCode, groupItems) -> {
            AccountingInfo reference = resolveReference(groupCode, groupItems);
            accountsByGroup.put(groupCode, new AccountDTO(
                    resolveNature(reference),
                    groupCode,
                    resolveDescription(groupCode, groupItems)
            ));
        });

        return accountsByGroup;
    }

    private boolean matchesExactGroupCode(AccountingInfo info, Long groupCode) {
        return info.getAccount() != null
                && info.getAccount().getCode() != null
                && groupCode != null
                && groupCode.toString().equals(info.getAccount().getCode().toString());
    }

    private AccountingInfo resolveReference(Long groupCode, List<AccountingInfo> groupItems) {
        return groupItems.stream()
                .filter(info -> matchesExactGroupCode(info, groupCode))
                .findFirst()
                .orElseGet(() -> groupItems.stream()
                        .min(Comparator.comparingInt((AccountingInfo info) -> info.getAccount().getCode().toString().length())
                                .thenComparing(info -> info.getAccount().getCode()))
                        .orElse(groupItems.get(0)));
    }

    private String resolveDescription(Long groupCode, List<AccountingInfo> groupItems) {
        return groupItems.stream()
                .filter(info -> matchesExactGroupCode(info, groupCode))
                .map(info -> info.getAccount().getName())
                .filter(name -> name != null && !name.isBlank())
                .findFirst()
                .orElseGet(() -> groupItems.stream()
                        .sorted(Comparator.comparingInt((AccountingInfo info) -> info.getAccount().getCode().toString().length())
                                .thenComparing(info -> info.getAccount().getCode()))
                        .map(info -> info.getAccount().getName())
                        .filter(name -> name != null && !name.isBlank())
                        .findFirst()
                        .orElse(""));
    }

    private Map<Long, BigDecimal> calculateInitialBalances(
            List<AccountingInfo> previousPeriodData,
            ECriteriaType criteriaType,
            Map<Long, AccountDTO> accountsByGroup) {

        Map<Long, BigDecimal> balances = new HashMap<>();

        for (AccountingInfo info : previousPeriodData) {
            Long groupCode = extractGroupingKey(info, criteriaType);
            String nature = accountsByGroup.containsKey(groupCode) ? accountsByGroup.get(groupCode).getNature() : resolveNature(info);
            BigDecimal currentBalance = balances.getOrDefault(groupCode, BigDecimal.ZERO);
            BigDecimal updatedBalance = calculateFinalBalance(currentBalance, extractDebit(info), extractCredit(info), nature);
            balances.put(groupCode, updatedBalance);
        }

        return balances;
    }

    private Long extractGroupingKey(AccountingInfo info, ECriteriaType criteriaType) {
        String accountCode = info.getAccount().getCode().toString();

        String groupedCode = switch (criteriaType) {
            case ACCOUNT -> accountCode.substring(0, Math.min(accountCode.length(), 4));
            case SUB_ACCOUNT -> accountCode.substring(0, Math.min(accountCode.length(), 6));
            case AUXILIARY_ACCOUNT -> accountCode.length() >= 8 ? accountCode.substring(0, 8) : accountCode;
            default -> throw new IllegalArgumentException("El nivel contable seleccionado no es válido para el Libro Auxiliar por Cuenta.");
        };

        return Long.parseLong(groupedCode);
    }

    private String resolveNature(AccountingInfo info) {
        return info.getAccount() != null ? info.getAccount().getNature() : null;
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

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private String resolveCostCenterCode(AccountingInfo info) {
        if (info.getCostCenter() == null || info.getCostCenter().getCode() == null) {
            return "";
        }

        return info.getCostCenter().getCode();
    }

    private String resolveVoucherNumber(AccountingInfo info) {
        if (info.getVoucher() == null || info.getVoucher().getNumber() == null) {
            return "";
        }

        return info.getVoucher().getNumber();
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFinalBalance(BigDecimal currentBalance, BigDecimal debit, BigDecimal credit, String nature) {
        if ("credito".equalsIgnoreCase(nature)) {
            return currentBalance.subtract(debit).add(credit);
        }

        return currentBalance.add(debit).subtract(credit);
    }
}
