package co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy;

import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.ThirdPartyBookDTO;
import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IThirdPartyInfoClient;
import co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.AccountingInfoProcessor;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingInfo;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.thirdParty.ThirdParty;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Estrategia para el Libro Auxiliar por Tercero.
 *
 * <p>Muestra, para un tercero dado ({@code criteria.thirdPartyId}), todos los
 * movimientos del periodo en orden cronológico con el saldo acumulado por cuenta.
 * El saldo inicial de cada cuenta se calcula a partir de los movimientos
 * anteriores a {@code startDate}.</p>
 *
 * <p>El nombre del tercero se resuelve a través de {@link IThirdPartyInfoClient},
 * que consulta el microservicio de terceros.  Si el servicio no devuelve resultado,
 * se deja el campo en blanco sin lanzar excepción.</p>
 */
@RequiredArgsConstructor
public class ThirdPartyStrategy implements IProcessStrategy {

    private static final int MONEY_SCALE = 2;

    /**
     * Cliente del microservicio de terceros.
     * Inyectado por {@code AuxiliaryBookProcessor} al construir la estrategia.
     */
    private final IThirdPartyInfoClient thirdPartyInfoClient;

    @Override
    public List<?> process(
            AuxiliaryBookCriteria criteria,
            List<AccountingInfo> allAccountingData,
            AccountingInfoProcessor accountingInfoProcessor) {

        validateCriteria(criteria);

        if (allAccountingData == null || allAccountingData.isEmpty()) {
            return Collections.emptyList();
        }

        ECriteriaType criteriaType = resolveCriteriaType(criteria);
        String selectedThirdPartyId = normalizeText(criteria.getThirdPartyId());
        LocalDate startDate = criteria.getStartDate();
        LocalDate endDate = criteria.getEndDate();

        // ── 1. Filtrar datos mínimamente estructurados y del tercero seleccionado ──
        List<AccountingInfo> validData = allAccountingData.stream()
                .filter(Objects::nonNull)
                .filter(this::hasMinimumStructure)
                .filter(info -> selectedThirdPartyId.equals(normalizeText(info.getThirdPartyId())))
                .toList();

        if (validData.isEmpty()) {
            return Collections.emptyList();
        }

        // ── 2. Separar periodo anterior (saldo inicial) y periodo actual ────
        List<AccountingInfo> previousPeriodData = validData.stream()
                .filter(info -> toLocalDate(info.getDate()).isBefore(startDate))
                .toList();

        List<AccountingInfo> currentPeriodData = validData.stream()
                .filter(info -> {
                    LocalDate d = toLocalDate(info.getDate());
                    return !d.isBefore(startDate) && !d.isAfter(endDate);
                })
                .sorted(buildChronologicalComparator(criteriaType))
                .toList();

        if (currentPeriodData.isEmpty()) {
            return Collections.emptyList();
        }

        // ── 3. Construir catálogo de cuentas y saldos iniciales ─────────────
        Map<Long, AccountDTO> accountsByGroup = buildAccountsByGroup(validData, criteriaType);
        Map<Long, BigDecimal> runningBalanceByGroup = calculateInitialBalances(
                previousPeriodData, criteriaType, accountsByGroup);

        // ── 4. Resolver nombre del tercero desde el microservicio ────────────
        String thirdPartyName = resolveThirdPartyName(selectedThirdPartyId);

        // ── 5. Construir filas del libro ─────────────────────────────────────
        List<ThirdPartyBookDTO> result = new ArrayList<>();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

        for (AccountingInfo info : currentPeriodData) {
            Long groupCode = extractGroupingKey(info, criteriaType);
            AccountDTO account = accountsByGroup.get(groupCode);

            if (account == null) {
                continue;
            }

            BigDecimal currentBalance = runningBalanceByGroup.getOrDefault(groupCode, BigDecimal.ZERO);
            BigDecimal debit = extractDebit(info);
            BigDecimal credit = extractCredit(info);
            BigDecimal updatedBalance = calculateFinalBalance(currentBalance, debit, credit, account.getNature());

            runningBalanceByGroup.put(groupCode, updatedBalance);

            result.add(new ThirdPartyBookDTO(
                    formato.format(info.getDate()),
                    account,
                    scale(debit),
                    scale(credit),
                    scale(updatedBalance),
                    normalizeText(info.getThirdPartyId()),
                    thirdPartyName,
                    resolveCostCenterCode(info),
                    resolveVoucherNumber(info)
            ));
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private void validateCriteria(AuxiliaryBookCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Los criterios del Libro Auxiliar por Tercero son obligatorios.");
        }
        if (criteria.getThirdPartyId() == null || criteria.getThirdPartyId().isBlank()) {
            throw new IllegalArgumentException(
                    "El tercero es obligatorio para generar el Libro Auxiliar por Tercero.");
        }
        if (criteria.getStartDate() == null || criteria.getEndDate() == null) {
            throw new IllegalArgumentException(
                    "El periodo es obligatorio para generar el Libro Auxiliar por Tercero.");
        }
        if (criteria.getStartDate().isAfter(criteria.getEndDate())) {
            throw new IllegalArgumentException(
                    "El periodo consultado es inválido: la fecha inicial no puede ser posterior a la fecha final.");
        }
    }

    // -------------------------------------------------------------------------
    // Third-party name resolution
    // -------------------------------------------------------------------------

    /**
     * Resuelve el nombre del tercero invocando al microservicio.
     * Si el id no es numérico, o el servicio no responde, devuelve cadena vacía.
     */
    private String resolveThirdPartyName(String thirdPartyId) {
        if (thirdPartyId == null || thirdPartyId.isBlank()) {
            return "";
        }
        try {
            Long id = Long.parseLong(thirdPartyId.trim());
            ThirdParty thirdParty = thirdPartyInfoClient.getThirdPartyById(id);
            return thirdParty != null && thirdParty.getName() != null
                    ? normalizeText(thirdParty.getName())
                    : "";
        } catch (NumberFormatException e) {
            return "";
        }
    }

    // -------------------------------------------------------------------------
    // Grouping & account catalog
    // -------------------------------------------------------------------------

    private ECriteriaType resolveCriteriaType(AuxiliaryBookCriteria criteria) {
        return criteria.getCriteriaType() != null ? criteria.getCriteriaType() : ECriteriaType.AUXILIARY_ACCOUNT;
    }

    private Map<Long, AccountDTO> buildAccountsByGroup(List<AccountingInfo> data, ECriteriaType criteriaType) {
        Map<Long, List<AccountingInfo>> grouped = data.stream()
                .collect(Collectors.groupingBy(
                        info -> extractGroupingKey(info, criteriaType),
                        TreeMap::new,
                        Collectors.toList()
                ));

        Map<Long, AccountDTO> result = new HashMap<>();
        grouped.forEach((groupCode, items) -> {
            AccountingInfo ref = resolveReference(groupCode, items);
            result.put(groupCode, new AccountDTO(
                    resolveNature(ref),
                    groupCode,
                    resolveDescription(groupCode, items)
            ));
        });
        return result;
    }

    private AccountingInfo resolveReference(Long groupCode, List<AccountingInfo> items) {
        return items.stream()
                .filter(info -> groupCode.toString().equals(info.getAccount().getCode().toString()))
                .findFirst()
                .orElseGet(() -> items.stream()
                        .min(Comparator.comparingInt(
                                        (AccountingInfo info) -> info.getAccount().getCode().toString().length())
                                .thenComparing(info -> info.getAccount().getCode()))
                        .orElse(items.get(0)));
    }

    private String resolveDescription(Long groupCode, List<AccountingInfo> items) {
        return items.stream()
                .filter(info -> groupCode.toString().equals(info.getAccount().getCode().toString()))
                .map(info -> info.getAccount().getName())
                .filter(name -> name != null && !name.isBlank())
                .findFirst()
                .orElseGet(() -> items.stream()
                        .sorted(Comparator.comparingInt(
                                        (AccountingInfo info) -> info.getAccount().getCode().toString().length())
                                .thenComparing(info -> info.getAccount().getCode()))
                        .map(info -> info.getAccount().getName())
                        .filter(name -> name != null && !name.isBlank())
                        .findFirst()
                        .orElse(""));
    }

    // -------------------------------------------------------------------------
    // Balance helpers
    // -------------------------------------------------------------------------

    private Map<Long, BigDecimal> calculateInitialBalances(
            List<AccountingInfo> previousData,
            ECriteriaType criteriaType,
            Map<Long, AccountDTO> accountsByGroup) {

        Map<Long, BigDecimal> balances = new HashMap<>();
        for (AccountingInfo info : previousData) {
            Long groupCode = extractGroupingKey(info, criteriaType);
            String nature = accountsByGroup.containsKey(groupCode)
                    ? accountsByGroup.get(groupCode).getNature()
                    : resolveNature(info);
            BigDecimal current = balances.getOrDefault(groupCode, BigDecimal.ZERO);
            balances.put(groupCode, calculateFinalBalance(current, extractDebit(info), extractCredit(info), nature));
        }
        return balances;
    }

    private BigDecimal calculateFinalBalance(BigDecimal current, BigDecimal debit, BigDecimal credit, String nature) {
        if ("credito".equalsIgnoreCase(nature)) {
            return current.subtract(debit).add(credit);
        }
        return current.add(debit).subtract(credit);
    }

    // -------------------------------------------------------------------------
    // Extractors
    // -------------------------------------------------------------------------

    private Long extractGroupingKey(AccountingInfo info, ECriteriaType criteriaType) {
        String code = info.getAccount().getCode().toString();
        String grouped = switch (criteriaType) {
            case NUMBER_CLASS     -> code.substring(0, Math.min(code.length(), 1));
            case GROUP            -> code.substring(0, Math.min(code.length(), 2));
            case ACCOUNT          -> code.substring(0, Math.min(code.length(), 4));
            case SUB_ACCOUNT      -> code.substring(0, Math.min(code.length(), 6));
            case AUXILIARY_ACCOUNT -> code.substring(0, Math.min(code.length(), 8));
        };
        return Long.parseLong(grouped);
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

    private String resolveCostCenterCode(AccountingInfo info) {
        if (info.getCostCenter() == null || info.getCostCenter().getCode() == null) return "";
        return info.getCostCenter().getCode();
    }

    private String resolveVoucherNumber(AccountingInfo info) {
        if (info.getVoucher() == null || info.getVoucher().getNumber() == null) return "";
        return info.getVoucher().getNumber();
    }

    private boolean hasMinimumStructure(AccountingInfo info) {
        return info.getAccount() != null
                && info.getAccount().getCode() != null
                && info.getDate() != null
                && info.getAccountingMovement() != null
                && info.getThirdPartyId() != null
                && !info.getThirdPartyId().isBlank();
    }

    private Comparator<AccountingInfo> buildChronologicalComparator(ECriteriaType criteriaType) {
        return Comparator.comparing(AccountingInfo::getDate)
                .thenComparing(info -> extractGroupingKey(info, criteriaType))
                .thenComparing(info -> info.getAccount().getCode())
                .thenComparing(info -> normalizeText(resolveVoucherNumber(info)))
                .thenComparing(info -> normalizeText(resolveCostCenterCode(info)));
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
