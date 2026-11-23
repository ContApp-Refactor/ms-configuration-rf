package co.unicauca.edu.co.contables.accounting.debt_payments.domain.model;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.WriteOffStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.InvoiceNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.WriteOffDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

// 1. Habilitamos la extensión de Mockito para JUnit 5.
// Esto inicializa cualquier campo anotado con @Mock.
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para la Entidad de Dominio PortfolioWriteOff")
public class PortfolioWriteOffUnitTest {
    // Variables comunes para las pruebas
    private String enterpriseId = "ENT-01";
    private Long thirdId = 100L;
    private String justification = "Cliente en bancarrota";

    // 2. Creamos un mock de InvoiceReplica. No necesitamos una instancia real,
    // solo un objeto que podamos controlar para nuestras pruebas.
    @Mock
    private InvoiceReplica mockInvoice1;
    @Mock
    private InvoiceReplica mockInvoice2;

    @Nested
    @DisplayName("Pruebas de Creación (Factory Method)")
    class CreationTests {

        @Test
        @DisplayName("Debe crear un castigo exitosamente con estado PENDING y total calculado")
        void shouldCreateWriteOffSuccessfully() {
            // ARRANGE
                List<WriteOffDetail> details = List.of(
                    writeOffDetailWithAccount(1L, 1000L, 13050513L),
                    writeOffDetailWithAccount(2L, 500L, 13050513L));

            // ACT
            PortfolioWriteOff writeOff = PortfolioWriteOff.create(enterpriseId, thirdId, justification, details, 1L);

            // ASSERT
            assertThat(writeOff).isNotNull();
            assertThat(writeOff.getEnterpriseId()).isEqualTo(enterpriseId);
            assertThat(writeOff.getThirdId()).isEqualTo(thirdId);
            assertThat(writeOff.getJustification()).isEqualTo(justification);
            assertThat(writeOff.getStatus()).isEqualTo(WriteOffStatus.PENDING_CONFIRMATION);
            assertThat(writeOff.getWriteOffDate()).isEqualTo(LocalDate.now());
            assertThat(writeOff.getTotalAmount()).isEqualTo(1500L);
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si la lista de detalles es nula")
        void shouldThrowExceptionWhenDetailsAreNull() {
            // ACT & ASSERT
            assertThatThrownBy(() -> PortfolioWriteOff.create(enterpriseId, thirdId, justification, null, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A write-off must have at least one invoice detail.");
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException si la justificación está en blanco")
        void shouldThrowExceptionWhenJustificationIsBlank() {
            // ARRANGE
            List<WriteOffDetail> details = List.of(writeOffDetailWithAccount(1L, 1000L, 13050513L));

            // ACT & ASSERT
            assertThatThrownBy(() -> PortfolioWriteOff.create(enterpriseId, thirdId, "   ", details, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("A justification is required for a write-off.");
        }
    }

    @Nested
    @DisplayName("Pruebas de Transición de Estado")
    class StateTransitionTests {

        private PortfolioWriteOff writeOff;

        @BeforeEach
        void setUp() {
            List<WriteOffDetail> details = List.of(writeOffDetailWithAccount(1L, 100L, 13050513L));
            writeOff = PortfolioWriteOff.create(enterpriseId, thirdId, justification, details, 1L);
        }

        @Test
        @DisplayName("Debe confirmar un castigo PENDING_CONFIRMATION")
        void shouldConfirmPendingWriteOff() {
            // ACT
            writeOff.confirm();
            // ASSERT
            assertThat(writeOff.getStatus()).isEqualTo(WriteOffStatus.CONFIRMED);
        }

        @Test
        @DisplayName("Debe anular un castigo CONFIRMED")
        void shouldVoidConfirmedWriteOff() {
            // ARRANGE
            writeOff.confirm();

            // ACT
            writeOff.voidConfirmation();
            // ASSERT
            assertThat(writeOff.getStatus()).isEqualTo(WriteOffStatus.VOIDED);
        }

        @Test
        @DisplayName("Debe lanzar IllegalStateException al confirmar un castigo no pendiente")
        void shouldThrowExceptionWhenConfirmingNonPendingWriteOff() {
            // ARRANGE
            writeOff.confirm();

            // ACT & ASSERT
            assertThatThrownBy(writeOff::confirm) // Sintaxis corta para () -> writeOff.confirm()
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Only a write-off with PENDING_CONFIRMATION status can be confirmed.");
        }
    }

    @Nested
    @DisplayName("Pruebas de Interacción con Facturas")
    class InvoiceInteractionTests {

        private PortfolioWriteOff writeOff;
        private WriteOffDetail detail1;
        private WriteOffDetail detail2;

        @BeforeEach
        void setUp() {
            // El escenario aquí es clave: los detalles llegan con un 'invoiceId', pero el
            // 'amountWrittenOff' y 'accountingAccount' serán poblados por la lógica de dominio.
            // Por eso los inicializamos a 0 o null.
            detail1 = writeOffDetailWithAccount(1L, 0L, null);
            detail2 = writeOffDetailWithAccount(2L, 0L, null);
            writeOff = PortfolioWriteOff.create(enterpriseId, thirdId, justification, List.of(detail1, detail2), 1L);
        }

        @Test
        @DisplayName("prepareInvoicesForWriteOff debe actualizar detalles y marcar facturas como pendientes")
        void prepareInvoicesForWriteOff_shouldUpdateDetailsAndMarkInvoices() {
            // ARRANGE
            when(mockInvoice1.getPendingValue()).thenReturn(1200L);
            when(mockInvoice1.getAccountingAccount()).thenReturn(112233L);
            when(mockInvoice2.getPendingValue()).thenReturn(800L);
            when(mockInvoice2.getAccountingAccount()).thenReturn(13050513L);

            Function<Long, Optional<InvoiceReplica>> invoiceFinder = invoiceId -> {
                if (invoiceId.equals(1L))
                    return Optional.of(mockInvoice1);
                if (invoiceId.equals(2L))
                    return Optional.of(mockInvoice2);
                return Optional.empty();
            };

            // ACT
            List<InvoiceReplica> modifiedInvoices = writeOff.prepareInvoicesForWriteOff(invoiceFinder);

            // ASSERT
            assertThat(writeOff.getTotalAmount()).isEqualTo(2000L);

            // Verificamos que los objetos 'detail' fueron mutados correctamente
            assertThat(detail1.getAmountWrittenOff()).isEqualTo(1200L);
            assertThat(detail1.getAccountingAccount()).isEqualTo(112233L);
            assertThat(detail2.getAmountWrittenOff()).isEqualTo(800L);
            assertThat(detail2.getAccountingAccount()).isEqualTo(13050513L);

            verify(mockInvoice1).markAsPendingWriteOff();
            verify(mockInvoice2).markAsPendingWriteOff();

            assertThat(modifiedInvoices).containsExactlyInAnyOrder(mockInvoice1, mockInvoice2);
        }

        @Test
        @DisplayName("prepareInvoicesForWriteOff debe lanzar InvoiceNotFoundException si una factura no existe")
        void prepareInvoicesForWriteOff_shouldThrowWhenInvoiceNotFound() {
            // ARRANGE
            Function<Long, Optional<InvoiceReplica>> failingInvoiceFinder = invoiceId -> Optional.empty();

            // ACT & ASSERT
            assertThatThrownBy(() -> writeOff.prepareInvoicesForWriteOff(failingInvoiceFinder))
                    .isInstanceOf(InvoiceNotFoundException.class)
                    .hasMessage("Invoice " + detail1.getInvoiceId() + " not found.");
        }

        @Test
        @DisplayName("processConfirmation debe llamar a writeOff() en cada factura")
        void processConfirmation_shouldCallWriteOffOnInvoices() {
            // ARRANGE
            writeOff.confirm();
            Function<Long, Optional<InvoiceReplica>> invoiceFinder = id -> {
                if (id.equals(1L) || id.equals(2L))
                    return Optional.of(mockInvoice1);
                return Optional.empty();
            };

            // ACT
            writeOff.processConfirmation(invoiceFinder);

            // ASSERT
            // Verificamos que el método se llamó 2 veces, una por cada detalle de la lista.
            verify(mockInvoice1, times(2)).writeOff();
        }
    }

    @Nested
    @DisplayName("Pruebas de Cálculo de Total")
    class TotalCalculationTests {

        @Test
        @DisplayName("calculateTotalAmount debe sumar correctamente los montos de los detalles")
        void calculateTotalAmount_shouldSumDetailAmountsCorrectly() {
            // ARRANGE
            WriteOffDetail detail1 = writeOffDetailWithAccount(1L, 700L, 13050513L);
            WriteOffDetail detail2 = writeOffDetailWithAccount(2L, 300L, 13050513L);
            PortfolioWriteOff writeOff = PortfolioWriteOff.create(enterpriseId, thirdId, justification,
                    List.of(detail1, detail2), 1L);

            // ACT
            // total is calculated during creation, no need to call non-public calculateTotalAmount()

            // ASSERT
            assertThat(writeOff.getTotalAmount()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("calculateTotalAmount debe manejar detalles con monto cero")
        void calculateTotalAmount_shouldHandleDetailsWithZeroAmount() { 
            // ARRANGE
            WriteOffDetail detail1 = writeOffDetailWithAccount(1L, 0L, 13050513L);
            WriteOffDetail detail2 = writeOffDetailWithAccount(2L, 0L, 13050513L);
            PortfolioWriteOff writeOff = PortfolioWriteOff.create(enterpriseId, thirdId, justification,
                    List.of(detail1, detail2), 1L);

            // ACT
            // total is calculated during creation, no need to call non-public calculateTotalAmount()

            // ASSERT
            assertThat(writeOff.getTotalAmount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("Pruebas de Notificaciones de Uso de Recursos")
    class ResourceUsageNotificationTests {

        @Test
        @DisplayName("getUsageNotifications debe incluir tercero y centro de costo cuando se especifica")
        void getUsageNotifications_shouldIncludeThirdAndCostCenter() {
            // ARRANGE
            Long costCenterId = 999L;
            WriteOffDetail detail = writeOffDetailWithAccount(1L, 100L, 13050513L);
            PortfolioWriteOff writeOff = PortfolioWriteOff.create(enterpriseId, thirdId, justification, List.of(detail), costCenterId);

            // ACT
            var notifications = writeOff.getUsageNotifications();

            // ASSERT
            assertThat(notifications).hasSize(2);
            assertThat(notifications).anySatisfy(n -> {
                assertThat(n.getClass().getSimpleName()).isEqualTo("ThirdPartyUsedNotification");
            });
            assertThat(notifications).anySatisfy(n -> {
                assertThat(n.getClass().getSimpleName()).isEqualTo("CostCenterUsedNotification");
            });
        }

        @Test
        @DisplayName("getUsageNotifications debe incluir solo tercero cuando no hay centro de costo")
        void getUsageNotifications_shouldIncludeOnlyThirdWhenNoCostCenter() {
            // ARRANGE
            WriteOffDetail detail = writeOffDetailWithAccount(1L, 100L, 13050513L);
            PortfolioWriteOff writeOff = PortfolioWriteOff.create(enterpriseId, thirdId, justification, List.of(detail), null);

            // ACT
            var notifications = writeOff.getUsageNotifications();

            // ASSERT
            assertThat(notifications).hasSize(1);
            assertThat(notifications).anySatisfy(n -> {
                assertThat(n.getClass().getSimpleName()).isEqualTo("ThirdPartyUsedNotification");
            });
        }
    }

}

