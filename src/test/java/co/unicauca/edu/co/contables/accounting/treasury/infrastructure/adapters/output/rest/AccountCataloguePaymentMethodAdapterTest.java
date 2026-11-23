package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.IBankAccountService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.bankAccounts.BankAccountNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.PaymentMethodsNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodService;
import co.unicauca.edu.co.contables.accounting.treasury.domain.exception.TreasuryException;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentMethodData;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountCataloguePaymentMethodAdapterTest {

    private static final String ENTERPRISE = "55433c17-b40f-45a7-ba17-1a8dc71086e8";

    @Mock
    private IPaymentMethodService paymentMethodService;
    @Mock
    private IBankAccountService bankAccountService;
    @Mock
    private IAccountCatalogueSearchInputPort searchPort;

    private AccountCataloguePaymentMethodAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AccountCataloguePaymentMethodAdapter(paymentMethodService, bankAccountService, searchPort);
    }

    private PaymentMethod paymentMethod(Long id, Boolean status, Long accountingAccountId) {
        PaymentMethod method = new PaymentMethod();
        method.setId(id);
        method.setStatus(status);
        method.setIdEnterprise(ENTERPRISE);
        if (accountingAccountId != null) {
            AccountCatalogue account = AccountCatalogue.builder().id(accountingAccountId).build();
            method.setAccountingAccountEntity(account);
        }
        return method;
    }

    private AccountCatalogue catalogueAccount(Long id, Boolean status) {
        return AccountCatalogue.builder().id(id).status(status).build();
    }

    @Test
    @DisplayName("findActive resuelve metodo activo con cuenta contable activa")
    void findActiveResuelve() {
        when(paymentMethodService.findById(3L, ENTERPRISE))
                .thenReturn(paymentMethod(3L, true, 42L));
        when(searchPort.getAllAccountsByEnterprise(ENTERPRISE))
                .thenReturn(List.of(catalogueAccount(42L, true)));

        Optional<PaymentMethodData> data = adapter.findActive(3L, ENTERPRISE);

        assertTrue(data.isPresent());
        assertEquals(3L, data.get().id());
        assertEquals(42L, data.get().accountingAccountId());
        assertFalse(data.get().requiresBankAccount());
    }

    @Test
    @DisplayName("Metodo de pago inexistente devuelve vacio (equivale al 404 anterior)")
    void findActiveMetodoInexistente() {
        when(paymentMethodService.findById(3L, ENTERPRISE))
                .thenThrow(new PaymentMethodsNotFoundException());

        assertTrue(adapter.findActive(3L, ENTERPRISE).isEmpty());
        verify(searchPort, never()).getAllAccountsByEnterprise(ENTERPRISE);
    }

    @Test
    @DisplayName("Metodo de pago inactivo devuelve vacio")
    void findActiveMetodoInactivo() {
        when(paymentMethodService.findById(3L, ENTERPRISE))
                .thenReturn(paymentMethod(3L, false, 42L));

        assertTrue(adapter.findActive(3L, ENTERPRISE).isEmpty());
    }

    @Test
    @DisplayName("Metodo sin cuenta contable devuelve vacio")
    void findActiveSinCuentaContable() {
        when(paymentMethodService.findById(3L, ENTERPRISE))
                .thenReturn(paymentMethod(3L, true, null));

        assertTrue(adapter.findActive(3L, ENTERPRISE).isEmpty());
    }

    @Test
    @DisplayName("Cuenta contable inactiva en el catalogue devuelve vacio")
    void findActiveCuentaContableInactiva() {
        when(paymentMethodService.findById(3L, ENTERPRISE))
                .thenReturn(paymentMethod(3L, true, 42L));
        when(searchPort.getAllAccountsByEnterprise(ENTERPRISE))
                .thenReturn(List.of(catalogueAccount(42L, false)));

        assertTrue(adapter.findActive(3L, ENTERPRISE).isEmpty());
    }

    @Test
    @DisplayName("Un error del modulo catalogue no se propaga: se trata como cuenta no activa")
    void findActiveCuentaContableNoResuelta() {
        when(paymentMethodService.findById(3L, ENTERPRISE))
                .thenReturn(paymentMethod(3L, true, 42L));
        when(searchPort.getAllAccountsByEnterprise(ENTERPRISE))
                .thenThrow(new IllegalStateException("boom"));

        assertTrue(adapter.findActive(3L, ENTERPRISE).isEmpty());
    }

    @Test
    @DisplayName("validateForPayment rechaza metodo inexistente")
    void validateRechazaMetodoInexistente() {
        when(paymentMethodService.findById(3L, ENTERPRISE))
                .thenThrow(new PaymentMethodsNotFoundException());

        TreasuryException ex = assertThrows(TreasuryException.class,
                () -> adapter.validateForPayment(3L, null, ENTERPRISE));
        assertEquals("Metodo de pago inactivo o inexistente", ex.getMessage());
    }

    @Test
    @DisplayName("validateForPayment rechaza metodo inactivo")
    void validateRechazaMetodoInactivo() {
        when(paymentMethodService.findById(3L, ENTERPRISE))
                .thenReturn(paymentMethod(3L, false, 42L));

        assertThrows(TreasuryException.class,
                () -> adapter.validateForPayment(3L, null, ENTERPRISE));
    }

    @Test
    @DisplayName("validateForPayment sin cuenta contable configurada falla")
    void validateSinCuentaContable() {
        when(paymentMethodService.findById(3L, ENTERPRISE))
                .thenReturn(paymentMethod(3L, true, null));

        TreasuryException ex = assertThrows(TreasuryException.class,
                () -> adapter.validateForPayment(3L, null, ENTERPRISE));
        assertEquals("El metodo de pago no tiene una cuenta contable configurada", ex.getMessage());
    }

    @Test
    @DisplayName("validateForPayment con cuenta contable inactiva falla")
    void validateCuentaContableInactiva() {
        when(paymentMethodService.findById(3L, ENTERPRISE))
                .thenReturn(paymentMethod(3L, true, 42L));
        when(searchPort.getAllAccountsByEnterprise(ENTERPRISE))
                .thenReturn(List.of(catalogueAccount(42L, false)));

        TreasuryException ex = assertThrows(TreasuryException.class,
                () -> adapter.validateForPayment(3L, null, ENTERPRISE));
        assertEquals("La cuenta contable del metodo de pago esta inactiva o no pertenece a la empresa",
                ex.getMessage());
    }

    @Test
    @DisplayName("validateForPayment sin cuenta bancaria llega hasta el final y no falla")
    void validateSinCuentaBancariaNoFalla() {
        when(paymentMethodService.findById(3L, ENTERPRISE))
                .thenReturn(paymentMethod(3L, true, 42L));
        when(searchPort.getAllAccountsByEnterprise(ENTERPRISE))
                .thenReturn(List.of(catalogueAccount(42L, true)));

        adapter.validateForPayment(3L, null, ENTERPRISE);
    }

    @Test
    @DisplayName("validateForPayment con cuenta bancaria falla: el metodo no la admite")
    void validateConCuentaBancariaFalla() {
        when(paymentMethodService.findById(3L, ENTERPRISE))
                .thenReturn(paymentMethod(3L, true, 42L));
        when(searchPort.getAllAccountsByEnterprise(ENTERPRISE))
                .thenReturn(List.of(catalogueAccount(42L, true)));

        TreasuryException ex = assertThrows(TreasuryException.class,
                () -> adapter.validateForPayment(3L, 77L, ENTERPRISE));
        assertEquals("El metodo de pago no admite cuenta bancaria", ex.getMessage());
    }

    @Test
    @DisplayName("isActiveBankAccount: cuenta bancaria inexistente o inactiva es false")
    void isActiveBankAccountInexistente() {
        when(bankAccountService.findById(77L, ENTERPRISE))
                .thenThrow(new BankAccountNotFoundException());

        assertFalse(adapter.isActiveBankAccount(77L, ENTERPRISE));
    }

    @Test
    @DisplayName("isActiveBankAccount: cuenta activa con cuenta contable activa es true")
    void isActiveBankAccountActiva() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setStatus(true);
        bankAccount.setAccountingAccountId(42L);
        when(bankAccountService.findById(77L, ENTERPRISE)).thenReturn(bankAccount);
        when(searchPort.getAllAccountsByEnterprise(ENTERPRISE))
                .thenReturn(List.of(catalogueAccount(42L, true)));

        assertTrue(adapter.isActiveBankAccount(77L, ENTERPRISE));
    }

    @Test
    @DisplayName("isActiveBankAccount: cuenta activa pero sin cuenta contable es false")
    void isActiveBankAccountSinCuentaContable() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setStatus(true);
        bankAccount.setAccountingAccountId(null);
        when(bankAccountService.findById(77L, ENTERPRISE)).thenReturn(bankAccount);

        assertFalse(adapter.isActiveBankAccount(77L, ENTERPRISE));
    }
}
