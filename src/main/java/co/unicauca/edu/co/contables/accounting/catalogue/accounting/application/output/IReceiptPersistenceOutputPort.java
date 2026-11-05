package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.output;

import java.util.Optional;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.Receipt;

public interface IReceiptPersistenceOutputPort {

   /**
    * Guarda un recibo en la base de datos. Si el recibo ya existe (basado en su ID),
    * se actualiza; de lo contrario, se crea uno nuevo.
    * @param receipt El recibo a guardar o actualizar.
    * @return El recibo guardado con su ID asignado.
    */
   Receipt save(Receipt receipt);

   /**
    * Busca un recibo por su ID original, que es el ID proveniente del sistema externo.
    * @param originalReceiptId El ID original del recibo.
    * @return Un Optional que contiene el recibo si se encuentra.
    */
   Optional<Receipt> findByOriginalReceiptId(Long originalReceiptId);

   /**
    * Busca un recibo por su código de negocio, que es el identificador único
    * del sistema de origen.
    * @param receiptCode El código del recibo (ej. "RC-12345").
    * @return Un Optional que contiene el recibo si se encuentra.
    */
   Optional<Receipt> findByReceiptCode(String receiptCode);

   /**
    * Verifica de manera eficiente si ya existe un recibo con el código de negocio dado.
    * @param receiptCode El código del recibo a verificar.
    * @return true si el recibo ya existe, false en caso contrario.
    */
   boolean existsByReceiptCode(String receiptCode);
   
}
