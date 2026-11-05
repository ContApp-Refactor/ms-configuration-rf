package co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.InvoiceStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pruebas Unitarias para InvoiceReplica")
public class InvoiceReplicaUnitTest {

    @Nested
    @DisplayName("Comportamiento de writeOff")
    class WriteOffTests {
        @Test
        @DisplayName("writeOff debe marcar como WRITTEN_OFF y poner pendingValue en 0 cuando no está pagada")
        void writeOff_shouldMarkWrittenOffAndZeroPending() {
            InvoiceReplica invoice = TestFixtures.invoiceReplicaWith(1L, 1500L, 500L, InvoiceStatus.PENDING);
            invoice.writeOff();

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.WRITTEN_OFF);
            assertThat(invoice.getPendingValue()).isEqualTo(0L);
        }

        @Test
        @DisplayName("writeOff no debe lanzar si ya está WRITTEN_OFF (idempotencia)")
        void writeOff_shouldBeIdempotentWhenAlreadyWrittenOff() {
            InvoiceReplica invoice = TestFixtures.invoiceReplicaWith(2L, 1000L, 0L, InvoiceStatus.WRITTEN_OFF);

            // No debe lanzar
            invoice.writeOff();

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.WRITTEN_OFF);
        }

        @Test
        @DisplayName("writeOff debe lanzar IllegalStateException si la factura ya está PAGADA")
        void writeOff_shouldThrowWhenPaid() {
            InvoiceReplica invoice = TestFixtures.invoiceReplicaWith(3L, 1000L, 0L, InvoiceStatus.PAID);

            assertThatThrownBy(invoice::writeOff)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot write off a fully paid invoice");
        }
    }

    @Nested
    @DisplayName("Validación de fechas")
    class DateValidationTests {
        @Test
        @DisplayName("validateDates debe lanzar si la expiración es antes de la creación")
        void validateDates_shouldThrowWhenExpirationBeforeCreation() {
            InvoiceReplica invoice = TestFixtures.invoiceReplicaDefault(10L);
            invoice.setCreationDate(LocalDate.of(2025, 1, 10));
            invoice.setExpirationDate(LocalDate.of(2024, 12, 31));

            assertThatThrownBy(invoice::validateDates)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expiration date cannot be before creation date");
        }
    }

    @Nested
    @DisplayName("Aplicar y revertir pagos")
    class PaymentTests {
        @Test
        @DisplayName("applyPayment debe disminuir pendingValue y actualizar status a PAID cuando llega a 0")
        void applyPayment_shouldDecreasePendingAndSetPaid() {
            InvoiceReplica invoice = TestFixtures.invoiceReplicaWith(20L, 200L, 200L, InvoiceStatus.PENDING);

            invoice.applyPayment(200L);

            assertThat(invoice.getPendingValue()).isEqualTo(0L);
            assertThat(invoice.getTotalPay()).isEqualTo(200L);
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        }

        @Test
        @DisplayName("applyPayment debe lanzar si amount <= 0")
        void applyPayment_shouldThrowWhenAmountNonPositive() {
            InvoiceReplica invoice = TestFixtures.invoiceReplicaWith(21L, 100L, 100L, InvoiceStatus.PENDING);

            assertThatThrownBy(() -> invoice.applyPayment(0L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Payment amount must be positive");
        }

        @Test
        @DisplayName("reversePayment debe restaurar pendingValue y ajustar totalPay")
        void reversePayment_shouldRestorePendingAndAdjustTotalPay() {
            InvoiceReplica invoice = TestFixtures.invoiceReplicaWith(30L, 200L, 50L, InvoiceStatus.PAID);

            invoice.reversePayment(50L);

            assertThat(invoice.getTotalPay()).isEqualTo(100L);
            assertThat(invoice.getPendingValue()).isEqualTo(100L);
            // El comportamiento actual mantiene el estado PAID cuando totalPay > 0
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        }
    }

    @Nested
    @DisplayName("Reversión de castigo")
    class ReverseWriteOffTests {
        @Test
        @DisplayName("reverseWriteOff debe restaurar pendingValue cuando estaba WRITTEN_OFF")
        void reverseWriteOff_shouldRestorePendingWhenWrittenOff() {
            InvoiceReplica invoice = TestFixtures.invoiceReplicaWith(40L, 300L, 0L, InvoiceStatus.WRITTEN_OFF);

            invoice.reverseWriteOff(300L);

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
            assertThat(invoice.getPendingValue()).isEqualTo(300L);
        }

        @Test
        @DisplayName("reverseWriteOff no debe hacer nada si no está WRITTEN_OFF")
        void reverseWriteOff_shouldDoNothingWhenNotWrittenOff() {
            InvoiceReplica invoice = TestFixtures.invoiceReplicaWith(41L, 200L, 100L, InvoiceStatus.PENDING);

            invoice.reverseWriteOff(200L);

            assertThat(invoice.getPendingValue()).isEqualTo(100L);
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
        }
    }
}

