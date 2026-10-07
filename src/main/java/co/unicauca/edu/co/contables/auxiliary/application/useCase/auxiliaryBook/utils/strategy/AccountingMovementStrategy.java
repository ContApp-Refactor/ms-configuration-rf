package co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy;

import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountingMovementBookDTO;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Estrategia para el Libro de Movimientos Contables (Libro de Comprobantes).
 *
 * <p>Este libro presenta todos los movimientos contables del periodo agrupados por
 * comprobante ({@code voucherType} + {@code voucherNumber}), mostrando para cada
 * asiento dentro del comprobante el saldo acumulado de la cuenta hasta ese momento
 * (incluyendo el periodo anterior como saldo inicial).</p>
 *
 * <p>Estructura de cada fila ({@link AccountingMovementBookDTO}):</p>
 * <ul>
 *   <li>{@code voucherType} — tipo de comprobante</li>
 *   <li>{@code date} — fecha del asiento</li>
 *   <li>{@code State} — estado del comprobante (tomado del tipo de comprobante)</li>
 *   <li>{@code thirdPartyId} — identificación del tercero</li>
 *   <li>{@code thirdPartyName} — nombre del tercero (resuelto si está disponible)</li>
 *   <li>{@code account} — cuenta contable</li>
 *   <li>{@code initialBalance} — saldo de la cuenta antes del comprobante</li>
 *   <li>{@code debitMovement} — débito del asiento</li>
 *   <li>{@code creditMovement} — crédito del asiento</li>
 *   <li>{@code netMovement} — saldo neto acumulado después del asiento</li>
 * </ul>
 *
 * <p>Regla contable del saldo neto:</p>
 * <pre>
 *   Cuenta débito  → netMovement = initialBalance + debit − credit
 *   Cuenta crédito → netMovement = initialBalance − debit + credit
 * </pre>
 */
@NoArgsConstructor
public class AccountingMovementStrategy implements IProcessStrategy {

    private static final int MONEY_SCALE = 2;

    private static final String DEFAULT_STATE = "REGISTRADO";
    private static final String DEFAULT_THIRD_PARTY_ID = "N/A";
    private static final String DEFAULT_THIRD_PARTY_NAME = "Sin Tercero";
    private static final String DEFAULT_VOUCHER_TYPE = "N/A";
    private static final String DEFAULT_NATURE = "debito";

    @Override
    public List<?> process(
            AuxiliaryBookCriteria criteria,
            List<AccountingInfo> allAccountingData,
            AccountingInfoProcessor accountingInfoProcessor) {

        validateCriteria(criteria);

        if (allAccountingData == null || allAccountingData.isEmpty()) {
            return Collections.emptyList();
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

        LocalDate startDate = criteria.getStartDate();
        LocalDate endDate = criteria.getEndDate();

        ECriteriaType criteriaType = criteria.getCriteriaType() != null
                ? criteria.getCriteriaType()
                : ECriteriaType.AUXILIARY_ACCOUNT;

        // ── 0. Filtros: estructura mínima + rango defensivo ─────────────────
        //   El rango ya se aplicó en AuxiliaryBookCriteriaProcessor, pero
        //   replicamos el filtro aquí para garantizar que ningún movimiento
        //   fuera del rango llegue al resultado, sin importar la ruta de entrada.
        List<AccountingInfo> validData = allAccountingData.stream()
                .filter(Objects::nonNull)
                .filter(this::hasMinimumStructure)
                .filter(info -> matchesRange(info, criteria, criteriaType))
                .toList();

        if (validData.isEmpty()) {
            return Collections.emptyList();
        }

        // ── 1. Separar periodo anterior (saldo inicial) y periodo actual ────
        List<AccountingInfo> previousPeriodData = validData.stream()
                .filter(info -> toLocalDate(info.getDate()).isBefore(startDate))
                .toList();

        List<AccountingInfo> currentPeriodData = validData.stream()
                .filter(info -> {
                    LocalDate d = toLocalDate(info.getDate());
                    return !d.isBefore(startDate) && !d.isAfter(endDate);
                })
                .toList();

        if (currentPeriodData.isEmpty()) {
            return Collections.emptyList();
        }

        // ── 2. Resolver naturaleza por cuenta (último valor visto, defensivo) ─
        //   La naturaleza se conserva por código de cuenta para que el cálculo
        //   del saldo sea consistente aunque algunos asientos vengan sin ella.
        Map<Long, String> natureByAccount = resolveNatureByAccount(validData);

        // ── 3. Saldo inicial por grupo contable (acumulado hasta startDate-1) ──
        //   El Libro de Movimientos agrupa saldos iniciales según el criterio
        //   contable (criteriaType) para que cuentas hijas hereden el saldo
        //   de la cuenta padre cuando se consulta a nivel de cuenta (4 dígitos).
        Map<Long, BigDecimal> initialBalanceByGroup =
                calculateInitialBalancesByGroup(previousPeriodData, natureByAccount, criteriaType);

        // ── 4. Agrupar periodo actual por comprobante (tipo + número) ───────
        Map<String, List<AccountingInfo>> byVoucher = currentPeriodData.stream()
                .collect(Collectors.groupingBy(
                        this::buildVoucherKey,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Ordenar comprobantes por la fecha del primer asiento del comprobante,
        // y como criterio de desempate, por la clave del comprobante.
        List<Map.Entry<String, List<AccountingInfo>>> sortedVouchers = byVoucher.entrySet().stream()
                .sorted(Comparator
                        .<Map.Entry<String, List<AccountingInfo>>, Date>comparing(e -> e.getValue().stream()
                                .map(AccountingInfo::getDate)
                                .min(Date::compareTo)
                                .orElse(new Date(0)))
                        .thenComparing(Map.Entry::getKey))
                .toList();

        // ── 5. Construir filas del libro ────────────────────────────────────
        //   El saldo corriente se acumula por código de cuenta a lo largo de
        //   todos los comprobantes. Cada fila refleja el saldo justo antes
        //   y justo después del asiento.
        Map<Long, BigDecimal> runningBalanceByAccount = new HashMap<>(initialBalanceByGroup);
        List<AccountingMovementBookDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<AccountingInfo>> voucherEntry : sortedVouchers) {

            List<AccountingInfo> voucherMovements = voucherEntry.getValue().stream()
                    .sorted(Comparator.comparing(AccountingInfo::getDate)
                            .thenComparing(info -> info.getAccount().getCode()))
                    .toList();

            for (AccountingInfo info : voucherMovements) {
                Long accountCode = info.getAccount().getCode();
                Long groupCode = extractGroupingKey(info, criteriaType);
                String nature = natureByAccount.getOrDefault(accountCode,
                        defaultIfBlank(info.getAccount().getNature(), DEFAULT_NATURE));

                BigDecimal initialBalance = runningBalanceByAccount.getOrDefault(groupCode, BigDecimal.ZERO);
                BigDecimal debit = extractDebit(info);
                BigDecimal credit = extractCredit(info);
                BigDecimal netMovement = calculateNetBalance(initialBalance, debit, credit, nature);
                runningBalanceByAccount.put(groupCode, netMovement);

                AccountDTO accountDTO = new AccountDTO(
                        nature,
                        accountCode,
                        defaultIfBlank(info.getAccount().getName(), "Cuenta " + accountCode)
                );

                result.add(new AccountingMovementBookDTO(
                        resolveVoucherType(info),
                        formato.format(info.getDate()),
                        DEFAULT_STATE,
                        defaultIfBlank(info.getThirdPartyId(), DEFAULT_THIRD_PARTY_ID),
                        DEFAULT_THIRD_PARTY_NAME,
                        accountDTO,
                        scale(initialBalance),
                        scale(debit),
                        scale(credit),
                        scale(netMovement)
                ));
            }
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private void validateCriteria(AuxiliaryBookCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Los criterios del Libro de Movimientos son obligatorios.");
        }
        if (criteria.getStartDate() == null || criteria.getEndDate() == null) {
            throw new IllegalArgumentException("El periodo es obligatorio para generar el Libro de Movimientos.");
        }
        if (criteria.getStartDate().isAfter(criteria.getEndDate())) {
            throw new IllegalArgumentException(
                    "El periodo consultado es inválido: la fecha inicial no puede ser posterior a la fecha final.");
        }
    }

    // -------------------------------------------------------------------------
    // Range / grouping helpers
    // -------------------------------------------------------------------------

    private Long extractGroupingKey(AccountingInfo info, ECriteriaType criteriaType) {
        String code = info.getAccount().getCode().toString();
        return Long.parseLong(switch (criteriaType) {
            case NUMBER_CLASS      -> code.substring(0, Math.min(code.length(), 1));
            case GROUP             -> code.substring(0, Math.min(code.length(), 2));
            case ACCOUNT           -> code.substring(0, Math.min(code.length(), 4));
            case SUB_ACCOUNT       -> code.substring(0, Math.min(code.length(), 6));
            case AUXILIARY_ACCOUNT -> code.substring(0, Math.min(code.length(), 8));
        });
    }

    private boolean matchesRange(AccountingInfo info, AuxiliaryBookCriteria criteria, ECriteriaType criteriaType) {
        CriteriaRange range = criteria.getCriteriaRange();
        if (range == null) {
            return true;
        }
        Long from = range.getFromRange();
        Long to = range.getToRange();
        if (from == null && to == null) {
            return true;
        }
        Long key = extractGroupingKey(info, criteriaType);
        return (from == null || key.compareTo(from) >= 0)
                && (to == null || key.compareTo(to) <= 0);
    }

    /**
     * Resuelve la naturaleza (débito/crédito) a usar para cada cuenta. Se queda
     * con la primera no-vacía vista en los datos válidos. Esto blinda el cálculo
     * del saldo cuando algunos asientos no traen explícita la naturaleza.
     */
    private Map<Long, String> resolveNatureByAccount(List<AccountingInfo> data) {
        Map<Long, String> result = new HashMap<>();
        for (AccountingInfo info : data) {
            Long code = info.getAccount().getCode();
            String nature = info.getAccount().getNature();
            if (nature != null && !nature.isBlank()) {
                result.putIfAbsent(code, nature.trim());
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Balance calculation
    // -------------------------------------------------------------------------

    /**
     * Calcula el saldo neto de cada grupo contable acumulando los movimientos
     * anteriores al inicio del periodo. La naturaleza se toma del mapa
     * pre-resuelto; si falta, se asume débito. El agrupamiento usa el
     * criteriaType para que cuentas hijas hereden saldos de la cuenta padre.
     */
    private Map<Long, BigDecimal> calculateInitialBalancesByGroup(
            List<AccountingInfo> previousPeriodData,
            Map<Long, String> natureByAccount,
            ECriteriaType criteriaType) {

        Map<Long, BigDecimal> balances = new HashMap<>();

        for (AccountingInfo info : previousPeriodData) {
            Long accountCode = info.getAccount().getCode();
            Long groupCode = extractGroupingKey(info, criteriaType);
            String nature = natureByAccount.getOrDefault(accountCode,
                    defaultIfBlank(info.getAccount().getNature(), DEFAULT_NATURE));
            BigDecimal current = balances.getOrDefault(groupCode, BigDecimal.ZERO);
            balances.put(groupCode, calculateNetBalance(current, extractDebit(info), extractCredit(info), nature));
        }

        return balances;
    }

    private BigDecimal calculateNetBalance(BigDecimal initial, BigDecimal debit, BigDecimal credit, String nature) {
        if ("credito".equalsIgnoreCase(nature) || "crédito".equalsIgnoreCase(nature)) {
            return initial.subtract(debit).add(credit);
        }
        return initial.add(debit).subtract(credit);
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

    private String buildVoucherKey(AccountingInfo info) {
        String type = resolveVoucherType(info);
        String number = resolveVoucherNumber(info);
        return type + "|" + number;
    }

    private String resolveVoucherType(AccountingInfo info) {
        return info.getVoucher() != null && info.getVoucher().getType() != null
                ? normalizeText(info.getVoucher().getType())
                : "";
    }

    private String resolveVoucherNumber(AccountingInfo info) {
        return info.getVoucher() != null && info.getVoucher().getNumber() != null
                ? normalizeText(info.getVoucher().getNumber())
                : "";
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

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }
}
