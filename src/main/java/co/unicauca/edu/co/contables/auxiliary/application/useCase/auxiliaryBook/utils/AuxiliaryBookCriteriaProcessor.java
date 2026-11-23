package co.unicauca.edu.co.contables.auxiliary.application.useCase.auxiliaryBook.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import co.unicauca.edu.co.contables.auxiliary.domain.models.enums.ECriteriaType;
import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.auxiliary.application.ports.out.IAccountingInfoClient;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.AuxiliaryBook;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.AuxiliaryBookCriteria;
import co.unicauca.edu.co.contables.auxiliary.domain.models.core.criteria.CriteriaRange;
import co.unicauca.edu.co.contables.auxiliary.domain.models.external.accountingInfo.AccountingInfo;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @brief Servicio que filtra la información contable según los criterios del libro.
 *
 * Consume la información contable completa a través del cliente externo y la
 * filtra por entidad, rango de cuentas, centro de costo, tercero, tipo de
 * comprobante y fecha hasta {@code endDate}. El filtro por {@code startDate}
 * se delega a cada estrategia para permitir el cálculo del saldo inicial.
 */
@Slf4j
@Service
@NoArgsConstructor
public class AuxiliaryBookCriteriaProcessor {

    public List<AccountingInfo> processAccountingInfo(IAccountingInfoClient queryPort, AuxiliaryBook book) {
        AuxiliaryBookCriteria criteria = book.getCriteria();
        log.info("[AuxBook] Criteria recibido para filtrado: type={}, range={}, costCenter={}, thirdParty={}, startDate={}, endDate={}",
                criteria.getCriteriaType(),
                criteria.getCriteriaRange() != null
                        ? "[" + criteria.getCriteriaRange().getFromRange() + " – " + criteria.getCriteriaRange().getToRange() + "]"
                        : "null",
                criteria.getCostCenterId(),
                criteria.getThirdPartyId(),
                criteria.getStartDate(),
                criteria.getEndDate());
        List<AccountingInfo> allAccountData = queryPort.getAllAccountInfo();
        List<AccountingInfo> filtered = filterAccountingInfoByCriteria(book, criteria, allAccountData);
        log.info("[AuxBook] Movimientos tras filtro: {} de {} totales", filtered.size(), allAccountData.size());
        return filtered;
    }

    private boolean isInRange(Long value, CriteriaRange range) {
        return (range.getFromRange() == null || value.compareTo(range.getFromRange()) >= 0)
                && (range.getToRange() == null || value.compareTo(range.getToRange()) <= 0);
    }

    private boolean isWithinDateRange(AccountingInfo info, AuxiliaryBookCriteria criteria) {
        LocalDate movementDate = info.getDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate endDate = criteria.getEndDate();
        return endDate == null || !movementDate.isAfter(endDate);
    }

    private Long extractCriteriaValue(String accountCode, ECriteriaType type) {
        if (accountCode == null || accountCode.isBlank() || type == null) {
            return null;
        }

        int size = switch (type) {
            case NUMBER_CLASS -> 1;
            case GROUP -> 2;
            case ACCOUNT -> 4;
            case SUB_ACCOUNT -> 6;
            case AUXILIARY_ACCOUNT -> 8;
        };

        String prefix = accountCode.substring(0, Math.min(accountCode.length(), size));
        if (!prefix.chars().allMatch(Character::isDigit)) {
            return null;
        }

        return Long.parseLong(prefix);
    }

    private boolean matchesCriteria(AccountingInfo info, AuxiliaryBookCriteria criteria) {
        if (criteria.getCriteriaType() == null || criteria.getCriteriaRange() == null) {
            return true;
        }

        Long criteriaValue = extractCriteriaValue(info.getAccount().getCode().toString(), criteria.getCriteriaType());
        return criteriaValue != null && isInRange(criteriaValue, criteria.getCriteriaRange());
    }

    private List<AccountingInfo> filterAccountingInfoByCriteria(
            AuxiliaryBook book,
            AuxiliaryBookCriteria criteria,
            List<AccountingInfo> all) {

        return all.stream()
                .filter(Objects::nonNull)
                .filter(info -> info.getEntId() != null && info.getDate() != null)
                .filter(info -> info.getAccount() != null && info.getAccount().getCode() != null)
                .filter(info -> Objects.equals(info.getEntId(), book.getEntId()))
                .filter(info -> isWithinDateRange(info, criteria))
                .filter(info -> !criteria.hasRange() || matchesCriteria(info, criteria))
                .filter(info -> {
                    if (criteria.getThirdPartyId() == null || criteria.getThirdPartyId().isBlank()) {
                        return true;
                    }
                    return info.getThirdPartyId() != null
                            && info.getThirdPartyId().trim().equals(criteria.getThirdPartyId().trim());
                })
                .filter(info -> {
                    if (criteria.getCostCenterId() == null || criteria.getCostCenterId().isBlank()) {
                        return true;
                    }
                    return info.getCostCenter() != null
                            && info.getCostCenter().getCode() != null
                            && info.getCostCenter().getCode().trim().equals(criteria.getCostCenterId().trim());
                })
                .filter(info -> {
                    if (criteria.getVoucherType() == null || criteria.getVoucherType().isBlank()) {
                        return true;
                    }
                    return info.getVoucher() != null
                            && info.getVoucher().getType() != null
                            && info.getVoucher().getType().trim().equals(criteria.getVoucherType().trim());
                })
                .toList();
    }
}