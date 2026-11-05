package co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import co.unicauca.edu.co.contables.accounting.catalogue.accounting.application.input.IAccountBalanceUpdateInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingEntry;
import co.unicauca.edu.co.contables.accounting.catalogue.accounting.domain.models.AccountingMovement;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueSearchOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.output.IAccountCatalogueUpdateOutputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.enums.NatureEnum;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class AccountBalanceUpdateService implements IAccountBalanceUpdateInputPort {

    private final IAccountCatalogueSearchOutputPort accountCatalogueSearchOutputPort;
    private final IAccountCatalogueUpdateOutputPort accountCatalogueUpdateOutputPort; // Asegúrate de inyectar la
                                                                                      // interfaz para
                                                                                      // guardar/actualizar cuentas

    @Override
    @Transactional
    public void updateBalancesFromAccountingEntry(AccountingEntry accountingEntry) {
        log.info("Iniciando actualización de saldos para el asiento contable {}", accountingEntry.getCode());
        for (AccountingMovement movement : accountingEntry.getMovements()) {
            updateSingleAccountHierarchy(movement.getAccount(), movement.getDebit(), movement.getCredit(), accountingEntry.getIdEnterprise());
        }
    }

    @Override
    @Transactional
    public void reverseBalancesFromAccountingEntry(AccountingEntry accountingEntry) {
        log.info("Iniciando REVERSIÓN de saldos para el asiento contable {}", accountingEntry.getCode());
        for (AccountingMovement movement : accountingEntry.getMovements()) {
            reverseSingleAccountHierarchy(movement.getAccount(), movement.getDebit(), movement.getCredit(), accountingEntry.getIdEnterprise());
        }
    }

    private void reverseSingleAccountHierarchy(Long accountId, BigDecimal debit, BigDecimal credit, String idEnterprise) {
        log.info("Revirtiendo saldos para la jerarquía de la cuenta ID: {}", accountId);
        AccountCatalogue currentAccount = accountCatalogueSearchOutputPort.getAccountCatalogueById(accountId , idEnterprise);
        if (currentAccount == null) {
            log.error("No se encontró la cuenta contable con ID: {}. Se omite la reversión de saldo.", accountId);
            return;
        }

        while (currentAccount != null) {
            log.debug("Revirtiendo saldo para la cuenta: {} ({})", currentAccount.getCode(), currentAccount.getDescription());

            BigDecimal amountToUpdate = calculateAmountByNature(currentAccount.getNature(), debit, credit);

            // LA LÓGICA CLAVE: Usamos .subtract() en lugar de .add()
            BigDecimal newBalance = currentAccount.getAmount().subtract(amountToUpdate);
            currentAccount.setAmount(newBalance); // Actualizar el objeto en memoria para reflejar el nuevo saldo
            log.debug("Nuevo saldo (revertido) para la cuenta {}: {}", currentAccount.getCode(), newBalance);

            // Actualizar el amount de la cuenta
            accountCatalogueUpdateOutputPort.updateAmount(currentAccount.getId(), currentAccount.getAmount());

            log.debug("Nuevo saldo (revertido) para la cuenta {}: {}", currentAccount.getCode(), newBalance);

            if (currentAccount.getParent() == null || currentAccount.getParent().getId() == null) {
                break;
            }

            currentAccount = accountCatalogueSearchOutputPort.getAccountCatalogueById(currentAccount.getParent().getId(), idEnterprise);
        }
    }

    @Transactional
    public void updateSingleAccountHierarchy(Long accountId, BigDecimal debit, BigDecimal credit, String idEnterprise) {
        log.info("Actualizando saldos para la cuenta contable {}", idEnterprise);
        // 1. Obtener la cuenta auxiliar (la que recibe el movimiento directo)
        AccountCatalogue currentAccount = accountCatalogueSearchOutputPort.getAccountCatalogueById(accountId , idEnterprise);
        if (currentAccount == null) {
            log.error("No se encontró la cuenta contable con ID: {}. Se omite la actualización de saldo.", accountId);
            // Considera lanzar una excepción si esto es un estado irrecuperable
            return;
        }

        // 2. Bucle para actualizar la cuenta actual y todos sus padres
        while (currentAccount != null) {
            log.debug("Actualizando saldo para la cuenta: {} ({})", currentAccount.getCode(),
                    currentAccount.getDescription());

            // 3. Calcular el monto a afectar basado en la naturaleza de la cuenta
            BigDecimal amountToUpdate = calculateAmountByNature(currentAccount.getNature(), debit, credit);

            // 4. Actualizar el monto
            BigDecimal newBalance = currentAccount.getAmount().add(amountToUpdate);
            currentAccount.setAmount(newBalance); // Actualizar el objeto en memoria para reflejar el nuevo saldo
            log.debug("Nuevo saldo para la cuenta {}: {}", currentAccount.getCode(), newBalance);

            // 5. Persistir cambio del monto de la cuenta
            accountCatalogueUpdateOutputPort.updateAmount(currentAccount.getId(), currentAccount.getAmount());

            // 6. Moverse al padre para la siguiente iteración
            // ASUNCIÓN: El padre ya está cargado en el objeto. Si no, necesitarás buscarlo
            // por `parent.getId()`.
            //currentAccount = currentAccount.getParent();
            if (currentAccount.getParent() == null || currentAccount.getParent().getId() == null) {
                break; // Salir si no hay padre
            }

            currentAccount = accountCatalogueSearchOutputPort.getAccountCatalogueById(currentAccount.getParent().getId(), idEnterprise);
        }
    }

      /**
     * Calcula el impacto en el saldo (positivo o negativo) según la naturaleza de la cuenta.
     * - Cuentas de naturaleza DÉBITO: Suman con los débitos, restan con los créditos.
     * - Cuentas de naturaleza CRÉDITO: Suman con los créditos, restan con los débitos.
     * @param nature Naturaleza de la cuenta (DEBITO, CREDITO)
     * @param debit Valor del débito del movimiento
     * @param credit Valor del crédito del movimiento
     * @return El valor neto a sumar (puede ser negativo) al saldo de la cuenta.
     */
    private BigDecimal calculateAmountByNature(NatureEnum nature, BigDecimal debit, BigDecimal credit) {
        if (nature == NatureEnum.DEBIT) {
            return debit.subtract(credit);
        } else if (nature == NatureEnum.CREDIT) {
            return credit.subtract(debit);
        } else {
            // Manejar caso de naturaleza no definida si es posible
            log.warn("La cuenta tiene una naturaleza no especificada. Se usará la lógica Débito-Crédito estándar.");
            return debit.subtract(credit);
        }
    }
}
