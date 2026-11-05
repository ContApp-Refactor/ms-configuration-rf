package co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils.strategy;

import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.AccountDTO;
import co.unicauca.edu.co.contables.auxiliary.application.dto.auxiliaryBook.DiaryBookDTO;
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
 * Estrategia para el Libro Diario (Journal / Libro de primera entrada).
 *
 * <p>Registra cada movimiento contable de forma individual en orden cronológico
 * dentro del periodo ({@code startDate}–{@code endDate}). Cuando se indica un
 * {@code criteriaType}, cada asiento se clasifica al nivel contable correspondiente
 * (clase, grupo, cuenta, subcuenta o cuenta auxiliar), mostrando el código y nombre
 * del grupo en lugar del código específico de la cuenta.</p>
 */
@NoArgsConstructor
public class DiaryStrategy implements IProcessStrategy {

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

        List<AccountingInfo> validData = allAccountingData.stream()
                .filter(Objects::nonNull)
                .filter(this::hasMinimumStructure)
                .filter(info -> isWithinPeriod(info.getDate(), criteria.getStartDate(), criteria.getEndDate()))
                .filter(info -> matchesRange(info, criteria))
                .toList();

        if (validData.isEmpty()) {
            return Collections.emptyList();
        }

        ECriteriaType criteriaType = criteria.getCriteriaType();

        if (criteriaType == null) {
            return validData.stream()
                    .sorted(buildComparator())
                    .map(this::toDTO)
                    .toList();
        }

        Map<Long, AccountDTO> accountsByGroup = buildAccountsByGroup(validData, criteriaType);

        return validData.stream()
                .sorted(buildGroupedComparator(criteriaType))
                .map(info -> toGroupedDTO(info, accountsByGroup, criteriaType))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private void validateCriteria(AuxiliaryBookCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Los criterios del Libro Diario son obligatorios.");
        }
        if (criteria.getStartDate() == null || criteria.getEndDate() == null) {
            throw new IllegalArgumentException("El periodo es obligatorio para generar el Libro Diario.");
        }
        if (criteria.getStartDate().isAfter(criteria.getEndDate())) {
            throw new IllegalArgumentException(
                    "El periodo consultado es inválido: la fecha inicial no puede ser posterior a la fecha final.");
        }
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private DiaryBookDTO toDTO(AccountingInfo info) {
        AccountDTO accountDTO = new AccountDTO(
                info.getAccount().getNature(),
                info.getAccount().getCode(),
                normalizeText(info.getAccount().getName())
        );
        return buildDiaryBookDTO(info, accountDTO);
    }

    private DiaryBookDTO toGroupedDTO(AccountingInfo info, Map<Long, AccountDTO> accountsByGroup, ECriteriaType criteriaType) {
        Long groupCode = extractGroupingKey(info, criteriaType);
        AccountDTO accountDTO = accountsByGroup.getOrDefault(groupCode, new AccountDTO(
                info.getAccount().getNature(),
                info.getAccount().getCode(),
                normalizeText(info.getAccount().getName())
        ));
        return buildDiaryBookDTO(info, accountDTO);
    }

    private DiaryBookDTO buildDiaryBookDTO(AccountingInfo info, AccountDTO accountDTO) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        String voucherType = info.getVoucher() != null ? normalizeText(info.getVoucher().getType()) : "";
        String voucherNumber = info.getVoucher() != null ? normalizeText(info.getVoucher().getNumber()) : "";

        return new DiaryBookDTO(
                formato.format(info.getDate()),
                accountDTO,
                voucherType,
                voucherNumber,
                scale(extractDebit(info)),
                scale(extractCredit(info))
        );
    }

    // -------------------------------------------------------------------------
    // Grouping
    // -------------------------------------------------------------------------

    private Map<Long, AccountDTO> buildAccountsByGroup(List<AccountingInfo> data, ECriteriaType criteriaType) {
        Map<Long, List<AccountingInfo>> groupedData = data.stream()
                .collect(Collectors.groupingBy(
                        info -> extractGroupingKey(info, criteriaType),
                        TreeMap::new,
                        Collectors.toList()
                ));

        Map<Long, AccountDTO> accountsByGroup = new HashMap<>();

        groupedData.forEach((groupCode, groupItems) -> {
            AccountingInfo reference = resolveReference(groupCode, groupItems);
            accountsByGroup.put(groupCode, new AccountDTO(
                    reference.getAccount().getNature(),
                    groupCode,
                    resolveDescription(groupCode, groupItems)
            ));
        });

        return accountsByGroup;
    }

    private AccountingInfo resolveReference(Long groupCode, List<AccountingInfo> groupItems) {
        return groupItems.stream()
                .filter(info -> groupCode.toString().equals(info.getAccount().getCode().toString()))
                .findFirst()
                .orElseGet(() -> groupItems.stream()
                        .min(Comparator.comparingInt((AccountingInfo info) -> info.getAccount().getCode().toString().length())
                                .thenComparing(info -> info.getAccount().getCode()))
                        .orElse(groupItems.get(0)));
    }

    private String resolveDescription(Long groupCode, List<AccountingInfo> groupItems) {
        return groupItems.stream()
                .filter(info -> groupCode.toString().equals(info.getAccount().getCode().toString()))
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

    private Long extractGroupingKey(AccountingInfo info, ECriteriaType criteriaType) {
        String code = info.getAccount().getCode().toString();

        return Long.valueOf(switch (criteriaType) {
            case NUMBER_CLASS -> code.substring(0, 1);
            case GROUP -> code.substring(0, Math.min(code.length(), 2));
            case ACCOUNT -> code.substring(0, Math.min(code.length(), 4));
            case SUB_ACCOUNT -> code.substring(0, Math.min(code.length(), 6));
            case AUXILIARY_ACCOUNT -> code.length() >= 8 ? code.substring(0, 8) : code;
        });
    }

    // -------------------------------------------------------------------------
    // Comparators
    // -------------------------------------------------------------------------

    private Comparator<AccountingInfo> buildComparator() {
        return Comparator.comparing(AccountingInfo::getDate)
                .thenComparing(info -> info.getAccount().getCode())
                .thenComparing(info -> normalizeText(resolveVoucherNumber(info)));
    }

    private Comparator<AccountingInfo> buildGroupedComparator(ECriteriaType criteriaType) {
        return Comparator.comparing(AccountingInfo::getDate)
                .thenComparing(info -> extractGroupingKey(info, criteriaType))
                .thenComparing(info -> info.getAccount().getCode())
                .thenComparing(info -> normalizeText(resolveVoucherNumber(info)));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean matchesRange(AccountingInfo info, AuxiliaryBookCriteria criteria) {
        CriteriaRange range = criteria.getCriteriaRange();
        ECriteriaType criteriaType = criteria.getCriteriaType();
        if (range == null || criteriaType == null) {
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

    private boolean hasMinimumStructure(AccountingInfo info) {
        return info.getAccount() != null
                && info.getAccount().getCode() != null
                && info.getDate() != null
                && info.getAccountingMovement() != null;
    }

    private boolean isWithinPeriod(Date date, LocalDate startDate, LocalDate endDate) {
        LocalDate movementDate = toLocalDate(date);
        return !movementDate.isBefore(startDate) && !movementDate.isAfter(endDate);
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

    private String resolveVoucherNumber(AccountingInfo info) {
        if (info.getVoucher() == null || info.getVoucher().getNumber() == null) {
            return "";
        }
        return info.getVoucher().getNumber();
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
