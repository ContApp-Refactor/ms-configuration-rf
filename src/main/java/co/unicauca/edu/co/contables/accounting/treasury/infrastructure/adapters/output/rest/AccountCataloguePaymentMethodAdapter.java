package co.unicauca.edu.co.contables.accounting.treasury.infrastructure.adapters.output.rest;

import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.model.BankAccount;
import co.unicauca.edu.co.contables.accounting.catalogue.bankAccounts.domain.services.IBankAccountService;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.application.input.IAccountCatalogueSearchInputPort;
import co.unicauca.edu.co.contables.accounting.catalogue.catalogue.domain.models.AccountCatalogue;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.bankAccounts.BankAccountNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.commons.exceptions.paymentMethods.PaymentMethodsNotFoundException;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.model.PaymentMethod;
import co.unicauca.edu.co.contables.accounting.catalogue.paymentMethods.domain.services.IPaymentMethodService;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentMethodProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.domain.exception.TreasuryException;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentMethodData;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Proveedor de metodos de pago y cuentas bancarias del modulo catalogue, resuelto
 * <strong>en el mismo proceso</strong>.
 *
 * <p>Sustituye las auto-llamadas HTTP a
 * {@code /api/accountCatalogue/payment-methods/findById/{id}/{enterprise}},
 * {@code /api/accountCatalogue/bank-accounts/findById/{id}/{enterprise}} y
 * {@code /api/accountCatalogue/search/{enterpriseId}}.
 *
 * <p>Los cuatro GET utilizados no llevan {@code @PreAuthorize} ni anotacion de clase, y la
 * seguridad por URL solo exige {@code authenticated()}, que el propio flujo de treasury ya
 * satisface: la validacion de autoridad no cambia. Los servicios invocados son los mismos a los
 * que llegaban esas rutas, con sus propias anotaciones {@code @Transactional}.
 *
 * <p>La semantica de "no encontrado" se conserva: en HTTP devolvia 404 (los codigos de error
 * terminan en {@code _NOT_FOUND} y {@code GlobalExceptionHandler} los traduce a 404) y el
 * adaptador capturaba {@code HttpClientErrorException.NotFound} para devolver {@code null}; aqui
 * se capturan las excepciones de dominio equivalentes. Cualquier otro error se propaga, igual que
 * antes.
 */
@Component
@Slf4j
public class AccountCataloguePaymentMethodAdapter implements IPaymentMethodProviderPort {

    /**
     * {@code requiresBankAccount} no existe en catalogue: ni en el modelo de dominio ni en
     * {@code PaymentMethodRes}. El registro HTTP anterior leia ese campo y recibia {@code null},
     * que {@code Boolean.TRUE.equals(...)} convertia a {@code false}, asi que el valor observable
     * siempre fue este.
     */
    private static final boolean REQUIRES_BANK_ACCOUNT = false;

    private final IPaymentMethodService paymentMethodService;
    private final IBankAccountService bankAccountService;
    private final IAccountCatalogueSearchInputPort accountCatalogueSearchInputPort;

    public AccountCataloguePaymentMethodAdapter(
            IPaymentMethodService paymentMethodService,
            IBankAccountService bankAccountService,
            IAccountCatalogueSearchInputPort accountCatalogueSearchInputPort) {
        this.paymentMethodService = paymentMethodService;
        this.bankAccountService = bankAccountService;
        this.accountCatalogueSearchInputPort = accountCatalogueSearchInputPort;
    }

    @Override
    public Optional<PaymentMethodData> findActive(Long paymentMethodId, String enterpriseId) {
        PaymentMethod method = fetchPaymentMethod(paymentMethodId, enterpriseId);
        if (method == null || !Boolean.TRUE.equals(method.getStatus())) {
            return Optional.empty();
        }
        Long accountingAccountId = accountingAccountId(method);
        if (accountingAccountId == null || !isActiveAccountingAccount(accountingAccountId, enterpriseId)) {
            return Optional.empty();
        }
        return Optional.of(new PaymentMethodData(method.getId(), REQUIRES_BANK_ACCOUNT, accountingAccountId));
    }

    @Override
    public void validateForPayment(Long paymentMethodId, Long bankAccountId, String enterpriseId) {
        PaymentMethod method = fetchPaymentMethod(paymentMethodId, enterpriseId);
        if (method == null) {
            throw badRequest("Metodo de pago inactivo o inexistente");
        }
        if (!Boolean.TRUE.equals(method.getStatus())) {
            throw badRequest("Metodo de pago inactivo o inexistente");
        }
        Long accountingAccountId = accountingAccountId(method);
        if (accountingAccountId == null) {
            throw badRequest("El metodo de pago no tiene una cuenta contable configurada");
        }
        if (!isActiveAccountingAccount(accountingAccountId, enterpriseId)) {
            throw badRequest("La cuenta contable del metodo de pago esta inactiva o no pertenece a la empresa");
        }
        if (REQUIRES_BANK_ACCOUNT && bankAccountId == null) {
            throw badRequest("El metodo de pago exige cuenta bancaria");
        }
        if (!REQUIRES_BANK_ACCOUNT && bankAccountId != null) {
            throw badRequest("El metodo de pago no admite cuenta bancaria");
        }
        if (bankAccountId != null && !isActiveBankAccount(bankAccountId, enterpriseId)) {
            throw badRequest("Cuenta bancaria inactiva o inexistente");
        }
    }

    @Override
    public boolean isActiveBankAccount(Long bankAccountId, String enterpriseId) {
        BankAccount bankAccount = fetchBankAccount(bankAccountId, enterpriseId);
        if (bankAccount == null || !Boolean.TRUE.equals(bankAccount.getStatus())) {
            return false;
        }
        Long accountingAccountId = bankAccount.getAccountingAccountId();
        return accountingAccountId != null && isActiveAccountingAccount(accountingAccountId, enterpriseId);
    }

    private PaymentMethod fetchPaymentMethod(Long paymentMethodId, String enterpriseId) {
        try {
            return paymentMethodService.findById(paymentMethodId, enterpriseId);
        } catch (PaymentMethodsNotFoundException ex) {
            return null;
        }
    }

    private BankAccount fetchBankAccount(Long bankAccountId, String enterpriseId) {
        try {
            return bankAccountService.findById(bankAccountId, enterpriseId);
        } catch (BankAccountNotFoundException ex) {
            return null;
        }
    }

    private boolean isActiveAccountingAccount(Long accountingAccountId, String enterpriseId) {
        if (accountingAccountId == null || enterpriseId == null || enterpriseId.isBlank()) {
            return false;
        }
        try {
            return accountCatalogueSearchInputPort.getAllAccountsByEnterprise(enterpriseId).stream()
                    .anyMatch(item -> accountingAccountId.equals(item.getId()) && Boolean.TRUE.equals(item.getStatus()));
        } catch (Exception ex) {
            return false;
        }
    }

    /** Mismo calculo que aplica {@code PaymentMethodDomainMapper.toRes} para {@code accountingAccountId}. */
    private static Long accountingAccountId(PaymentMethod method) {
        AccountCatalogue accountingAccount = method.getAccountingAccountEntity();
        return accountingAccount != null ? accountingAccount.getId() : null;
    }

    private TreasuryException badRequest(String message) {
        return new TreasuryException(TreasuryException.Type.BAD_REQUEST, message);
    }
}
