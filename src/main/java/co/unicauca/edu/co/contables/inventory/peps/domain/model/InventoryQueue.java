package co.unicauca.edu.co.contables.inventory.peps.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


/**
 * @brief Manages the inventory queue for FIFO valuation
 * 
 * Maintains a list of balances (lots) and provides methods to consume
 * stock according to First-In-First-Out principles.
 */
public class InventoryQueue {
   private final LinkedList<Balance> balances;
    

    public InventoryQueue() {
        this.balances = new LinkedList<>();
    }
    
    /**
     * @brief Adds a balance to the end of the queue (Purchase)
     * @param balance Balance to add
     */
    public void addBalance(Balance balance) {
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        if (balance.isEmpty()) {
            throw new IllegalArgumentException("Cannot add empty balance");
        }
        this.balances.add(balance);
    }
    
    /**
     * @brief Adds a balance to the start of the queue (Sales Return)
     * @param balance Balance to add
     */
    public void addBalanceFirst(Balance balance) {
        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }
        this.balances.addFirst(balance);
    }
    
    /**
     * @brief Consumes a quantity from the queue using FIFO logic
     * @param quantityToConsume Quantity to remove
     * @return List of sale details describing which lots were consumed
     */
    public List<SaleDetail> consumeQuantity(int quantityToConsume) {
        if (quantityToConsume <= 0) {
            throw new IllegalArgumentException("Quantity to consume must be positive");
        }
        
        if (!hasEnoughStock(quantityToConsume)) {
            throw new IllegalArgumentException(
                String.format("Insufficient stock. Required: %d, Available: %d", 
                    quantityToConsume, getTotalQuantity())
            );
        }
        
        return consumeQuantityInternal(quantityToConsume);
    }

    /**
     * @brief Consumes a quantity for reporting purposes
     * 
     * Similar to consumeQuantity but may have different validation strictness
     * for report generation.
     * 
     * @param quantityToConsume Quantity to remove
     * @return List of sale details
     */
    public List<SaleDetail> consumeQuantityForReport(int quantityToConsume) {
        if (quantityToConsume <= 0) {
            throw new IllegalArgumentException("Quantity to consume must be positive");
        }
        
       
        return consumeQuantityInternal(quantityToConsume);
    }
    
    //  compartido con la lógica real de consumo
    private List<SaleDetail> consumeQuantityInternal(int quantityToConsume) {
        List<SaleDetail> details = new ArrayList<>();
        int remaining = quantityToConsume;
        
        while (remaining > 0 && !balances.isEmpty()) {
            Balance currentLot = balances.peekFirst();
            
            int amountFromThisLot = Math.min(remaining, currentLot.getQuantity());
            
            // Crear detalle usando Factory Method
            SaleDetail detail = SaleDetail.fromBalance(currentLot, amountFromThisLot);
            details.add(detail);
            
            // Reducir cantidad del lote
            currentLot.reduceQuantity(amountFromThisLot);
            
            // Si el lote está vacío, eliminarlo
            if (currentLot.isEmpty()) {
                balances.pollFirst();
            }
            
            remaining -= amountFromThisLot;
        }
        
        return details;
    }

    
    /**
     * @brief Removes a specific quantity from lots with a matching unit price
     * 
     * Used for purchase returns where specific lots must be identified by price.
     * 
     * @param quantityToRemove Quantity to remove
     * @param unitPrice Unit price to match
     * @return List of details of removed items
     */
    public List<SaleDetail> removeByUnitPrice(int quantityToRemove, BigDecimal unitPrice) {
        if (quantityToRemove <= 0) {
            throw new IllegalArgumentException("Quantity to remove must be positive");
        }
        
        List<SaleDetail> details = new ArrayList<>();
        int remaining = quantityToRemove;
        
        Iterator<Balance> iterator = balances.iterator();
        
        while (iterator.hasNext() && remaining > 0) {
            Balance balance = iterator.next();
            
            if (balance.matchesUnitPrice(unitPrice)) {
                int amountToTake = Math.min(remaining, balance.getQuantity());
                
                SaleDetail detail = SaleDetail.fromBalance(balance, amountToTake);
                details.add(detail);
                
                balance.reduceQuantity(amountToTake);
                remaining -= amountToTake;
                
                if (balance.isEmpty()) {
                    iterator.remove();
                }
            }
        }
        
        return details;
    }
    
    /**
     * @brief Checks if there is enough total stock
     * @param requiredQuantity Quantity needed
     * @return true if total quantity >= required
     */
    public boolean hasEnoughStock(int requiredQuantity) {
        return getTotalQuantity() >= requiredQuantity;
    }
    
    /**
     * @brief Gets the total quantity across all balances
     * @return Total quantity
     */
    public int getTotalQuantity() {
        return balances.stream()
            .mapToInt(Balance::getQuantity)
            .sum();
    }
    
    /**
     * @brief Gets the total value of the inventory
     * @return Sum of total prices of all balances
     */
    public BigDecimal getTotalValue() {
        return balances.stream()
            .map(Balance::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * @brief Checks if the queue is empty
     * @return true if no balances exist
     */
    public boolean isEmpty() {
        return balances.isEmpty();
    }
    
    /**
     * @brief Creates a deep copy of the current balances
     * @return List of copied balances
     */
    public List<Balance> getBalancesCopy() {
        return balances.stream()
            .map(Balance::copy)
            .toList();
    }
    
    //Obtener balances inmutables
    public List<Balance> getBalancesReadOnly() {
        return Collections.unmodifiableList(new ArrayList<>(balances));
    }
    
    //Obtener primer balance
    public Optional<Balance> getFirstBalance() {
        return Optional.ofNullable(balances.peekFirst()); 
    }
    
    //Limpiar cola
    public void clear() {
        balances.clear();
    }
    
    //Obtener tamaño de la cola
    public int size() {
        return balances.size();
    }
    
    @Override
    public String toString() {
        return String.format("InventoryQueue{size=%d, totalQuantity=%d, totalValue=%s}", 
            balances.size(), getTotalQuantity(), getTotalValue());
    }
}
