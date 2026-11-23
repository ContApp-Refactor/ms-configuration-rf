package co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy;

import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.InventoryAndBalancesBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.AccountingInfoProcessor;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.CriteriaRange;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingInfo;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Estrategia para el Libro de Inventarios y Balances.
 *
 * <p>Este libro presenta el saldo neto de cada cuenta al final del periodo
 * ({@code endDate}).  El saldo final se calcula como:</p>
 *
 * <pre>
 *   Saldo Inicial (acumulado hasta startDate - 1)
 *       + Débitos del periodo  – Créditos del periodo   (cuentas de naturaleza débito)
 *   Saldo Inicial
 *       + Créditos del periodo – Débitos del periodo    (cuentas de naturaleza crédito)
 * </pre>
 *
 * <p>Solo se emiten filas donde el saldo final es distinto de cero.</p>
 */
@NoArgsConstructor
public class InventoryAndBalancesStrategy implements IProcessStrategy {

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

        List<AccountingInfo> validData = allAccountingData.stream()
                .filter(Objects::nonNull)
                .filter(this::hasMinimumStructure)
                .toList();

        if (validData.isEmpty()) {
            return Collections.emptyList();
        }

        // ── 1. Separar datos en periodo anterior y periodo actual ────────────
        //   Periodo anterior: movimientos previos a startDate  → saldo inicial
        //   Periodo actual:   movimientos dentro [startDate, endDate]
        List<AccountingInfo> previousPeriodData = validData.stream()
                .filter(info -> toLocalDate(info.getDate()).isBefore(startDate))
                .toList();

        List<AccountingInfo> currentPeriodData = validData.stream()
                .filter(info -> {
                    LocalDate d = toLocalDate(info.getDate());
                    return !d.isBefore(startDate) && !d.isAfter(endDate);
                })
                .toList();

        // ── 2. Agrupar por el nivel contable indicado en criteriaType ──────────
        ECriteriaType criteriaType = criteria.getCriteriaType() != null
                ? criteria.getCriteriaType()
                : ECriteriaType.AUXILIARY_ACCOUNT;

        Map<Long, List<AccountingInfo>> previousByAccount = groupByCriteriaLevel(previousPeriodData, criteriaType);
        Map<Long, List<AccountingInfo>> currentByAccount = groupByCriteriaLevel(currentPeriodData, criteriaType);

        NavigableSet<Long> allAccountCodes = new TreeSet<>();
        allAccountCodes.addAll(previousByAccount.keySet());
        allAccountCodes.addAll(currentByAccount.keySet());

        // Defensive range filter: aunque el AuxiliaryBookCriteriaProcessor debería
        // haber descartado los movimientos fuera del rango, aquí garantizamos que
        // los códigos agregados que queden fuera de [from, to] nunca lleguen al
        // resultado final, sin importar cómo lleguen los criterios.
        CriteriaRange range = criteria.getCriteriaRange();
        if (range != null) {
            Long from = range.getFromRange();
            Long to = range.getToRange();
            if (from != null || to != null) {
                allAccountCodes.removeIf(code ->
                        (from != null && code.compareTo(from) < 0)
                                || (to != null && code.compareTo(to) > 0));
            }
        }

        // ── 3. Construir un DTO por cuenta ──────────────────────────────────
        List<InventoryAndBalancesBookDTO> result = new ArrayList<>();

        for (Long accountCode : allAccountCodes) {
            List<AccountingInfo> prev = previousByAccount.getOrDefault(accountCode, Collections.emptyList());
            List<AccountingInfo> curr = currentByAccount.getOrDefault(accountCode, Collections.emptyList());

            AccountingInfo reference = resolveReference(accountCode, curr, prev);
            if (reference == null) continue;

            String nature = reference.getAccount().getNature();

            BigDecimal initialBalance = calculateSignedBalance(prev, nature);
            BigDecimal periodDebit = sumDebit(curr);
            BigDecimal periodCredit = sumCredit(curr);
            BigDecimal finalBalance = calculateFinalBalance(initialBalance, periodDebit, periodCredit, nature);

            BigDecimal scaledFinal = scale(finalBalance);

            // El Libro de Inventarios solo incluye cuentas con saldo distinto de cero
            if (scaledFinal.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            AccountDTO accountDTO = new AccountDTO(
                    nature,
                    accountCode,
                    resolveDescription(accountCode, curr, prev)
            );

            String description = reference.getAccountingMovement() != null
                    ? normalizeText(reference.getAccountingMovement().getDescription())
                    : "";

            result.add(new InventoryAndBalancesBookDTO(accountDTO, description, scaledFinal));
        }

        return result.stream()
                .sorted(Comparator.comparing(dto -> dto.getAccount().getAccountCode()))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private void validateCriteria(AuxiliaryBookCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Los criterios del Libro de Inventarios y Balances son obligatorios.");
        }
        if (criteria.getStartDate() == null || criteria.getEndDate() == null) {
            throw new IllegalArgumentException(
                    "El periodo es obligatorio para generar el Libro de Inventarios y Balances.");
        }
        if (criteria.getStartDate().isAfter(criteria.getEndDate())) {
            throw new IllegalArgumentException(
                    "El periodo consultado es inválido: la fecha inicial no puede ser posterior a la fecha final.");
        }
    }

    // -------------------------------------------------------------------------
    // Balance calculation
    // -------------------------------------------------------------------------

    /**
     * Calcula el saldo neto (con signo) de una lista de movimientos según la
     * naturaleza contable de la cuenta.
     */
    private BigDecimal calculateSignedBalance(List<AccountingInfo> movements, String nature) {
        BigDecimal debit = sumDebit(movements);
        BigDecimal credit = sumCredit(movements);
        return calculateFinalBalance(BigDecimal.ZERO, debit, credit, nature);
    }

    private BigDecimal calculateFinalBalance(BigDecimal initial, BigDecimal debit, BigDecimal credit, String nature) {
        if ("credito".equalsIgnoreCase(nature)) {
            return initial.subtract(debit).add(credit);
        }
        // naturaleza débito (o desconocida) → débito aumenta, crédito disminuye
        return initial.add(debit).subtract(credit);
    }

    private BigDecimal sumDebit(List<AccountingInfo> movements) {
        return movements.stream()
                .map(info -> info.getAccountingMovement() != null
                        && info.getAccountingMovement().getDebit() != null
                        ? BigDecimal.valueOf(info.getAccountingMovement().getDebit())
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumCredit(List<AccountingInfo> movements) {
        return movements.stream()
                .map(info -> info.getAccountingMovement() != null
                        && info.getAccountingMovement().getCredit() != null
                        ? BigDecimal.valueOf(info.getAccountingMovement().getCredit())
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // -------------------------------------------------------------------------
    // Grouping & reference resolution
    // -------------------------------------------------------------------------

    private Map<Long, List<AccountingInfo>> groupByCriteriaLevel(List<AccountingInfo> data, ECriteriaType criteriaType) {
        return data.stream()
                .collect(Collectors.groupingBy(
                        info -> extractGroupingKey(info, criteriaType),
                        TreeMap::new,
                        Collectors.toList()
                ));
    }

    private Long extractGroupingKey(AccountingInfo info, ECriteriaType criteriaType) {
        String code = info.getAccount().getCode().toString();
        return Long.parseLong(switch (criteriaType) {
            case NUMBER_CLASS   -> code.substring(0, Math.min(code.length(), 1));
            case GROUP          -> code.substring(0, Math.min(code.length(), 2));
            case ACCOUNT        -> code.substring(0, Math.min(code.length(), 4));
            case SUB_ACCOUNT    -> code.substring(0, Math.min(code.length(), 6));
            case AUXILIARY_ACCOUNT -> code.substring(0, Math.min(code.length(), 8));
        });
    }

    private AccountingInfo resolveReference(Long groupCode,
                                            List<AccountingInfo> current,
                                            List<AccountingInfo> previous) {
        List<AccountingInfo> available = !current.isEmpty() ? current : previous;
        if (available.isEmpty()) return null;

        return available.stream()
                .filter(info -> groupCode.toString().equals(info.getAccount().getCode().toString()))
                .findFirst()
                .orElseGet(() -> available.stream()
                        .min(Comparator.comparingInt((AccountingInfo info) -> info.getAccount().getCode().toString().length())
                                .thenComparing(info -> info.getAccount().getCode()))
                        .orElse(available.get(0)));
    }

    private String resolveDescription(Long groupCode,
                                      List<AccountingInfo> current,
                                      List<AccountingInfo> previous) {
        List<AccountingInfo> available = !current.isEmpty() ? current : previous;
        return available.stream()
                .filter(info -> groupCode.toString().equals(info.getAccount().getCode().toString()))
                .map(info -> info.getAccount().getName())
                .filter(name -> name != null && !name.isBlank())
                .findFirst()
                .orElseGet(() -> available.stream()
                        .sorted(Comparator.comparingInt((AccountingInfo info) -> info.getAccount().getCode().toString().length())
                                .thenComparing(info -> info.getAccount().getCode()))
                        .map(info -> info.getAccount().getName())
                        .filter(name -> name != null && !name.isBlank())
                        .findFirst()
                        .orElse(""));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean hasMinimumStructure(AccountingInfo info) {
        return info.getAccount() != null
                && info.getAccount().getCode() != null
                && info.getDate() != null
                && info.getAccountingMovement() != null;
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}
