package co.unicauca.edu.co.contables.inventory.peps.domain.port.output.command;

import java.util.List;

import co.unicauca.edu.co.contables.inventory.peps.domain.model.Kardex;

/**
 * @brief Output port for Kardex command operations
 * 
 * Defines the contract for persisting kardex movements and updating
 * inventory records in the persistence layer.
 */
public interface  IKardexCommandOutputPort {

     /**
      * @brief Registers a purchase movement
      * @param kardex Kardex record to register
      * @return Registered kardex record
      */
     Kardex registerPurchase(Kardex kardex);

     /**
      * @brief Registers a sale movement and updates affected lots
      * @param kardex Kardex record to register
      * @param lotsToUpdate List of lots (kardex records) to update
      * @return Registered kardex record
      */
     Kardex registerSale(Kardex kardex,List<Kardex> lotsToUpdate);

     /**
      * @brief Registers a purchase return movement
      * @param kardex Kardex record to register
      * @return Registered kardex record
      */
     Kardex registerPurchaseReturn(Kardex kardex);

     /**
      * @brief Registers a sale return movement
      * @param kardex Kardex record to register
      * @return Registered kardex record
      */
     Kardex registerSaleReturn(Kardex kardex);

     /**
      * @brief Registers a non-commercial exit movement
      * @param kardex Kardex record to register
      * @param lotsToUpdate List of lots to update
      * @return Registered kardex record
      */
     Kardex registerNonCommercialExit(Kardex kardex, List<Kardex> lotsToUpdate);

     /**
      * @brief Registers a non-commercial entry movement
      * @param kardex Kardex record to register
      * @return Registered kardex record
      */
     Kardex registerNonCommercialEntry(Kardex kardex);

     /**
      * @brief Updates the available amount of a kardex record
      * @param idKardex Kardex identifier
      * @param newAmount New available amount
      * @return Number of affected rows
      */
     int updateAvaliableAmount(Long idKardex, int newAmount);
     
     /**
     * @brief Deletes all kardex records
     */
     void deleteAll();
    
}
