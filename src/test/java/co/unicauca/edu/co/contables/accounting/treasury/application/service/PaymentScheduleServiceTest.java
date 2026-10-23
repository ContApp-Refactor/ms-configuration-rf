package co.unicauca.edu.co.contables.accounting.treasury.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentVoucherCommandUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.input.IPaymentVoucherQueryUseCase;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IExecutionContextPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentMethodProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentSchedulePersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.IPaymentVoucherQueryPersistencePort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ISupplierInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITimeProviderPort;
import co.unicauca.edu.co.contables.accounting.treasury.application.output.ITransactionRunnerPort;
import co.unicauca.edu.co.contables.accounting.treasury.domain.exception.TreasuryException;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentSchedule;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentScheduleDetail;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.PaymentScheduleStatus;
import co.unicauca.edu.co.contables.accounting.treasury.domain.model.SupplierInvoiceReplica;

/**
 * Ejecucion de programaciones de pago.
 *
 * Regla: la validacion del metodo de pago se resuelve ANTES de invocar
 * {@code voucherCommands.create(...)}. Motivo: {@code TransactionalPaymentVoucherUseCase.create}
 * es {@code @Transactional} y comparte la transaccion de
 * {@code TransactionalPaymentScheduleUseCase.execute}; si esa llamada lanza, Spring marca la
 * transaccion como rollback-only y el {@code catch} posterior no puede guardar el estado
 * FAILED, con lo que la programacion queda SCHEDULED y la ejecucion responde 500.
 */
@ExtendWith(MockitoExtension.class)
class PaymentScheduleServiceTest {

    private static final String ENTERPRISE = "55433c17-b40f-45a7-ba17-1a8dc71086e8";
    private static final String INACTIVE_METHOD = "Metodo de pago inactivo o inexistente";
    private static final Long SCHEDULE_ID = 1L;
    private static final Long INVOICE_ID = 42L;
    private static final Long METHOD_ID = 7L;

    @Mock
    private IPaymentSchedulePersistencePort schedules;
    @Mock
    private ISupplierInvoiceProviderPort invoices;
    @Mock
    private IPaymentVoucherCommandUseCase voucherCommands;
    @Mock
    private IPaymentVoucherQueryUseCase voucherQueries;
    @Mock
    private IPaymentVoucherQueryPersistencePort voucherQueryPersistence;
    @Mock
    private IExecutionContextPort context;
    @Mock
    private ITransactionRunnerPort transactions;
    @Mock
    private ITimeProviderPort time;
    @Mock
    private IPaymentMethodProviderPort paymentMethods;

    private PaymentSchedule schedule;
    private PaymentScheduleService service;

    @BeforeEach
    void setUp() {
        schedule = new PaymentSchedule();
        schedule.setId(SCHEDULE_ID);
        schedule.setEnterpriseId(ENTERPRISE);
        schedule.setStatus(PaymentScheduleStatus.SCHEDULED);
        schedule.setPaymentMethodId(METHOD_ID);
        schedule.setBankAccountId(null);

        PaymentScheduleDetail detail = new PaymentScheduleDetail();
        detail.setInvoiceId(INVOICE_ID);
        detail.setSupplierId(5L);
        detail.setAmount(new BigDecimal("100000"));
        schedule.getDetails().add(detail);

        service = new PaymentScheduleService(schedules, invoices, voucherCommands, voucherQueries,
                voucherQueryPersistence, context, transactions, time, paymentMethods);

        lenient().when(schedules.findLocked(SCHEDULE_ID)).thenReturn(Optional.of(schedule));
        lenient().when(schedules.save(schedule)).thenReturn(schedule);
    }

    @Test
    @DisplayName("Metodo de pago inactivo: queda FALLIDA sin intentar crear el comprobante")
    void executeWithInactivePaymentMethodNeverCreatesVoucher() {
        SupplierInvoiceReplica invoice = new SupplierInvoiceReplica();
        invoice.setId(INVOICE_ID);
        invoice.setEnterpriseId(ENTERPRISE);
        invoice.setReference("OC-1");
        invoice.setSupplierId(5L);
        invoice.setPendingAmount(new BigDecimal("100000"));
        invoice.setReservedAmount(BigDecimal.ZERO);
        invoice.setActive(true);
        lenient().when(invoices.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
        lenient().doThrow(new TreasuryException(TreasuryException.Type.BAD_REQUEST, INACTIVE_METHOD))
                .when(paymentMethods)
                .validateForPayment(any(), any(), any());

        PaymentSchedule result = service.execute(SCHEDULE_ID);

        assertEquals(PaymentScheduleStatus.FAILED, result.getStatus(), "La programacion debe quedar FALLIDA");
        assertEquals(1, result.getRetryCount(), "Cada ejecucion fallida debe incrementar el reintento");
        assertEquals(INACTIVE_METHOD, result.getFailureReason(),
                "El motivo debe ser el de la validacion del metodo de pago");
        verify(voucherCommands, never()).create(any());
    }
}
