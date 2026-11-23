package co.unicauca.edu.co.contables.inventory.peps.domain.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Represents a line item in a Kardex report
 * 
 * Contains details about a movement (entry or output) and the resulting
 * inventory balance state.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KardexReport {
    private Long idKardex;
    private ZonedDateTime date;
    private String detail;

    private Integer entryQuantity;
    private BigDecimal entryUnitPrice;
    private BigDecimal entryTotalPrice;

    private List<SaleDetail> outputDetails;  

    private List<Balance> balance;


    /**
     * @brief Creates a report for an entry movement
     * @param movement The entry movement
     * @param currentBalance The state of inventory balances after the movement
     * @return New KardexReport
     */
    public static KardexReport createEntryReport(Kardex movement, List<Balance> currentBalance) {
        validateMovement(movement);
        
        BigDecimal total = movement.getUnitPrice().multiply(BigDecimal.valueOf(movement.getQuantity()));
        
        KardexReport report = KardexReport.builder()
            .idKardex(movement.getIdKardex())
            .date(movement.getDate())
            .detail(movement.getDetails())
            .entryQuantity(movement.getQuantity())
            .entryUnitPrice(movement.getUnitPrice())
            .entryTotalPrice(total)
            .balance(copyBalances(currentBalance))
            .build();
        
        return report;
    }

    /**
     * @brief Creates a report for an output movement
     * @param movement The output movement
     * @param saleDetails Details of which lots were consumed
     * @param currentBalance The state of inventory balances after the movement
     * @return New KardexReport
     */
    public static KardexReport createOutputReport(Kardex movement, List<SaleDetail> saleDetails, 
                                                 List<Balance> currentBalance) {
        validateMovement(movement);
        
        KardexReport report = KardexReport.builder()
            .idKardex(movement.getIdKardex())
            .date(movement.getDate())
            .detail(movement.getDetails())
            .outputDetails(new ArrayList<>(saleDetails))
            .balance(copyBalances(currentBalance))
            .build();
        
        return report;
    }

    /**
     * @brief Creates a report for the initial balance
     * @param date Date of the balance
     * @param initialBalance Initial inventory state
     * @return New KardexReport
     */
    public static KardexReport createInitialBalanceReport(ZonedDateTime date, List<Balance> initialBalance) {
        KardexReport report = KardexReport.builder()
            .date(date)
            .detail("SALDO INICIAL")
            .balance(copyBalances(initialBalance))
            .build();
        
        return report;
    
    }

    /**
     * @brief Sets entry data manually
     * @param quantity Quantity entered
     * @param unitPrice Unit price of entry
     */
    public void setEntryData(int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Entry quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        
        this.entryQuantity = quantity;
        this.entryUnitPrice = unitPrice;
        this.entryTotalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Establecer salida con detalles
    public void setOutputData(List<SaleDetail> details) {
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Output details cannot be empty");
        }
        this.outputDetails = new ArrayList<>(details);
    }

    // Actualizar balance
    public void updateBalance(List<Balance> newBalance) {
        this.balance = copyBalances(newBalance);
    }

    // Verificar si es entrada
    public boolean isEntry() {
        return entryQuantity != null && entryQuantity > 0;
    }

    // Verificar si es salida
    public boolean isOutput() {
        return outputDetails != null && !outputDetails.isEmpty();
    }

    //Verificar si es saldo inicial
    public boolean isInitialBalance() {
        return "SALDO INICIAL".equals(detail);
    }

    //Obtener cantidad total de salida (calculado desde detalles)
    public int getOutputQuantity() {
        if (outputDetails == null || outputDetails.isEmpty()) {
            return 0;
        }
        return outputDetails.stream()
            .mapToInt(SaleDetail::getQuantityUsed)
            .sum();
    }

    // Obtener total de salida (calculado desde detalles)
    public BigDecimal getOutputTotalPrice() {
        if (outputDetails == null || outputDetails.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return outputDetails.stream()
            .map(SaleDetail::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //Obtener precio promedio de salida
    public BigDecimal getAverageOutputPrice() {
        int quantity = getOutputQuantity();
        if (quantity == 0) {
            return BigDecimal.ZERO;
        }
        return getOutputTotalPrice().divide(
            BigDecimal.valueOf(quantity), 
            2, 
            java.math.RoundingMode.HALF_UP
        );
    }

    //Obtener cantidad total del balance (calculado)
    public int getTotalBalanceQuantity() {
        if (balance == null || balance.isEmpty()) {
            return 0;
        }
        return balance.stream()
            .mapToInt(Balance::getQuantity)
            .sum();
    }

    //Obtener valor total del balance (calculado)
    public BigDecimal getTotalBalanceValue() {
        if (balance == null || balance.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return balance.stream()
            .map(Balance::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //Obtener precio promedio del balance
    public BigDecimal getAverageBalancePrice() {
        int quantity = getTotalBalanceQuantity();
        if (quantity == 0) {
            return BigDecimal.ZERO;
        }
        return getTotalBalanceValue().divide(
            BigDecimal.valueOf(quantity), 
            2, 
            java.math.RoundingMode.HALF_UP
        );
    }

    // Verificar si tiene balance
    public boolean hasBalance() {
        return balance != null && !balance.isEmpty() && getTotalBalanceQuantity() > 0;
    }


    public List<SaleDetail> getOutputDetailsReadOnly() {
        if (outputDetails == null) return Collections.emptyList();
        return Collections.unmodifiableList(outputDetails);
    }

    public List<Balance> getBalanceReadOnly() {
        if (balance == null) return Collections.emptyList();
        return Collections.unmodifiableList(balance);
    }


    private static void validateMovement(Kardex movement) {
        Objects.requireNonNull(movement, "Movement cannot be null");
        Objects.requireNonNull(movement.getDate(), "Movement date cannot be null");
    }

    private static List<Balance> copyBalances(List<Balance> balances) {
        if (balances == null || balances.isEmpty()) {
            return new ArrayList<>();
        }
        return balances.stream()
            .map(Balance::copy)
            .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KardexReport that = (KardexReport) o;
        return Objects.equals(idKardex, that.idKardex) &&
               Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idKardex, date);
    }

    @Override
    public String toString() {
        return String.format("KardexReport{id=%d, date=%s, detail='%s', balanceQty=%d, balanceValue=%s}", 
            idKardex, date, detail, getTotalBalanceQuantity(), getTotalBalanceValue());
    }

}
