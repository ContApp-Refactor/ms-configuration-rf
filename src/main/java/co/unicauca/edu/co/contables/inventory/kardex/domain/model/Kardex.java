package co.unicauca.edu.co.contables.inventory.kardex.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Random;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @brief Domain model representing an inventory movement record
 * 
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Kardex {
    private Long id;

    private String factCode;

    private int quantity;

    private BigDecimal unitPrice;

    private String details;

    private MovementType type;

    private int balanceQuantity;

    private BigDecimal balanceUnitPrice;

    private ZonedDateTime date;

    private Long productId;

    private BigDecimal totalBalance;

    public void addDate(){
        this.date = ZonedDateTime.now(ZoneId.of("America/Bogota"));
    }

    /**
     * @brief Processes a purchase transaction with weighted average calculation
     * @param lastQuantity Previous inventory quantity
     * @param lastTotalBalance Previous total inventory value
     */
    public void addPurchase(int lastQuantity, BigDecimal lastTotalBalance) {
        // 1. Calculate the new total quantity in balance
        this.balanceQuantity = lastQuantity + this.quantity;

        // If there is no total quantity, the price is zero to avoid division by zero.
        if (this.balanceQuantity == 0) {
            this.balanceUnitPrice = BigDecimal.ZERO;
            return;
        }

        // 2. Calculate the total value of the current purchase
        BigDecimal currentTotalValue = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));

        // 3. Sum both values to obtain the new total inventory value
        this.totalBalance = lastTotalBalance.add(currentTotalValue);

        // 4. Convert the total quantity to BigDecimal for division
        BigDecimal totalQuantityBigDecimal = BigDecimal.valueOf(this.balanceQuantity);

        // 5. Divide the total value by the total quantity for the weighted average
        this.balanceUnitPrice = this.totalBalance.divide(totalQuantityBigDecimal, 2, RoundingMode.HALF_UP);
        
        generateAdjustmentFactCode();
        updateDetailIfNotNull();
    }

    /**
     * @brief Processes a sale transaction with current average price
     * @param lastQuantity Previous inventory quantity
     * @param lastUnitPrice Current average unit price
     * @param lastTotalBalance Previous total inventory value
     */
    public void addSale(int lastQuantity, BigDecimal lastUnitPrice, BigDecimal lastTotalBalance) {
        // 1. Calculate the new total quantity in balance
        this.balanceQuantity = lastQuantity - this.quantity;
        
        if (this.balanceQuantity < 0) {
            this.balanceUnitPrice = BigDecimal.ZERO;
            return;
        }
        if (this.balanceQuantity == 0) {
            resetBalancesIfZero();
            
            generateAdjustmentFactCode();
            updateDetailIfNotNull();
            return;
        }
        
        // 2. The last unit price is maintained for the balance
        this.balanceUnitPrice = lastUnitPrice;

        // 3. The unit price is the same as the last balance
        this.unitPrice = lastUnitPrice;

        // 4. Calculate the total value
        this.totalBalance = lastTotalBalance.subtract(this.unitPrice.multiply(BigDecimal.valueOf(this.quantity)) );
               
        generateAdjustmentFactCode();
        updateDetailIfNotNull();
    }

    /**
     * @brief Processes a sale return with recalculated weighted average
     * @param lastQuantity Previous inventory quantity
     * @param lastUnitPrice Current average unit price
     * @param lastTotalBalance Previous total inventory value
     */
    public void returnOnSale(int lastQuantity, BigDecimal lastUnitPrice, BigDecimal lastTotalBalance) {
        // 1. Calculate the new total quantity in balance
        this.balanceQuantity = lastQuantity + this.quantity;
        
        if (this.balanceQuantity == 0) {
            this.balanceUnitPrice = BigDecimal.ZERO;
            return;
        }

        // 2. The total balance is increased
        this.totalBalance = lastUnitPrice.multiply(BigDecimal.valueOf(this.quantity)).add(lastTotalBalance);

        // 3. The new unit price is calculated
        this.balanceUnitPrice = this.totalBalance.divide(BigDecimal.valueOf(this.balanceQuantity), 2, RoundingMode.HALF_UP);
         
        generateAdjustmentFactCode();
        updateDetailIfNotNull();
    }

    /**
     * @brief Processes a purchase return with recalculated weighted average
     * @param lastQuantity Previous inventory quantity
     * @param lastTotalBalance Previous total inventory value
     */
    public void returnOnPurchase(int lastQuantity, BigDecimal lastTotalBalance) {
        // 1. Calculate the new total quantity in balance
        this.balanceQuantity = lastQuantity - this.quantity;
        
        // 
        if (this.balanceQuantity < 0) {
            this.balanceUnitPrice = BigDecimal.ZERO;
            return;
        }
        if (this.balanceQuantity == 0) {
            resetBalancesIfZero();
            return;
        }

        // 2. Calculate the total value
        this.totalBalance = lastTotalBalance.subtract(this.unitPrice.multiply(BigDecimal.valueOf(this.quantity)));

        // 3. Convert the total quantity to BigDecimal for division
        BigDecimal totalQuantityBigDecimal = BigDecimal.valueOf(this.balanceQuantity);

        // 4. Divide the total value by the total quantity for the weighted average
        this.balanceUnitPrice = this.totalBalance.divide(totalQuantityBigDecimal, 2, RoundingMode.HALF_UP);
             
        generateAdjustmentFactCode();
        updateDetailIfNotNull();
    }


    /**
     * @brief Resets all balance fields when inventory reaches zero
     */
    public void resetBalancesIfZero(){
            this.totalBalance = BigDecimal.ZERO;
            this.balanceUnitPrice = BigDecimal.ZERO;
            this.balanceQuantity = 0;   
            this.unitPrice = BigDecimal.ZERO;  
    }

    public void updateDetailIfNotNull() {
        if (this.details == null || this.details.isEmpty()) {
            // Format the detail with the movement type and code.  Sale - Invoice: 500
            this.details = String.format("%s - Factura: %s", this.type.getDescription(), this.factCode);
        }else{
            this.details = String.format("%s | %s - Factura: %s", this.details, this.type.getDescription(), this.factCode);
        }
    }

    /**
     * @brief Generates a unique fact code for inventory adjustments
     * Ensures no duplication by checking existing code
     * Format: AYYMMDDHHMMSSX (A + timestamp + random letter) 
     */
    public void generateAdjustmentFactCode() {
        if (this.factCode != null && !this.factCode.isEmpty()) {
            return; // Ya tiene un código de factura válido
        }
        String timestamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        char randomLetter = (char) ('A' + new Random().nextInt(26));
        this.factCode = String.format("A%s%c", timestamp, randomLetter);
    }

}