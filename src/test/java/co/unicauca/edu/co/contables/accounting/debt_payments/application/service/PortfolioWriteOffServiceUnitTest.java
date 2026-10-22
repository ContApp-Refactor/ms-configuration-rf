package co.unicauca.edu.co.contables.accounting.debt_payments.application.service;

import co.unicauca.edu.co.contables.accounting.debt_payments.application.input.IAccountingEventPublisher;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IInvoiceProviderPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IPortfolioWriteOffPersistencePort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.output.IResourceUsageNotifierPort;
import co.unicauca.edu.co.contables.accounting.debt_payments.application.service.PortfolioWriteOffService;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.enums.WriteOffStatus;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.InvoiceNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.exception.PortfolioWriteOffNotFoundException;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.PortfolioWriteOff;
import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Replica.InvoiceReplica;
import co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para PortfolioWriteOffService")
public class PortfolioWriteOffServiceUnitTest {
    // 1. @Mock: Inyección de dependencias simuladas (falsas).
    @Mock
    private IPortfolioWriteOffPersistencePort writeOffPersistencePort;
    @Mock
    private IInvoiceProviderPort invoiceProviderPort;
    @Mock
    private IAccountingEventPublisher accountingEventPublisher;
    @Mock
    private IResourceUsageNotifierPort resourceUsageNotifier;

    // 2. @InjectMocks: inyecta los mocks en este servicio en la instancia real
    @InjectMocks 
    private PortfolioWriteOffService portfolioWriteOffService;

    // 3. @Spy: Usaremos un Spy para el objeto de dominio con sus reglas reales.
    @Spy
    private PortfolioWriteOff sampleWriteOff = new PortfolioWriteOff();

    @Nested
    @DisplayName("Caso de Uso: Confirmar Castigo")
    class ConfirmWriteOffTests {

        @Test
        @DisplayName("Debe confirmar un castigo exitosamente, orquestando todas las llamadas")
        void shouldConfirmWriteOffSuccessfully() {
            // ARRANGE (Organizar)
            Long writeOffId = 1L;
            sampleWriteOff.setId(writeOffId);
            sampleWriteOff.setStatus(WriteOffStatus.PENDING_CONFIRMATION);

            // Creamos un mock de la factura que será devuelta por el dominio al procesar la confirmación.
            InvoiceReplica mockInvoice = mock(InvoiceReplica.class);
            List<InvoiceReplica> modifiedInvoices = List.of(mockInvoice);

            // Programamos los mocks: "Cuando X método sea llamado, entonces haz Y"
            when(writeOffPersistencePort.findById(writeOffId)).thenReturn(Optional.of(sampleWriteOff));
            doReturn(modifiedInvoices).when(sampleWriteOff).processConfirmation(any());
            when(writeOffPersistencePort.update(sampleWriteOff)).thenReturn(sampleWriteOff);

            // ACT (Actuar)
            PortfolioWriteOff result = portfolioWriteOffService.confirmWriteOff(writeOffId);

            // ASSERT (Afirmar)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(sampleWriteOff);

            // Verificación de la orquestación para asegurarnos de que todo se llamó en el orden correcto.
            var inOrder = inOrder(writeOffPersistencePort, invoiceProviderPort, accountingEventPublisher, sampleWriteOff);

            inOrder.verify(writeOffPersistencePort).findById(writeOffId); // 1. Primero se busca el castigo.
            inOrder.verify(sampleWriteOff).confirm(); // 2. Luego se llama a la lógica de dominio.
            inOrder.verify(sampleWriteOff).processConfirmation(any()); // 3. Luego se procesan las facturas.
            inOrder.verify(invoiceProviderPort).updateInvoice(mockInvoice); // 4. Luego se actualiza cada factura.
            inOrder.verify(writeOffPersistencePort).update(sampleWriteOff); // 5. Luego se persiste el castigo actualizado.
            inOrder.verify(accountingEventPublisher).publishWriteOffConfirmedEvent(sampleWriteOff); // 6. Finalmente, se publica el evento.
        }

        @Test
        @DisplayName("Debe lanzar PortfolioWriteOffNotFoundException si el castigo no existe")
        void shouldThrowNotFoundExceptionWhenConfirmingNonExistentWriteOff() {
            // ARRANGE
            Long nonExistentId = 99L;
            // Programamos el mock para que devuelva un Optional vacío, simulando que noencontró nada.
            when(writeOffPersistencePort.findById(nonExistentId)).thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> portfolioWriteOffService.confirmWriteOff(nonExistentId))
                    .isInstanceOf(PortfolioWriteOffNotFoundException.class)
                    .hasMessageContaining("no fue encontrado");

            // Verificamos que no se hicieron más llamadas, la ejecución se detuvo como debía.
            verify(invoiceProviderPort, never()).updateInvoice(any());
            verify(accountingEventPublisher, never()).publishWriteOffConfirmedEvent(any());
        }

        @Test
        @DisplayName("Debe propagar InvoiceNotFoundException si el dominio no encuentra una factura al confirmar")
        void shouldPropagateInvoiceNotFoundExceptionFromDomainWhenConfirming() {
            // ARRANGE
            Long writeOffId = 3L;
            sampleWriteOff.setId(writeOffId);
            sampleWriteOff.setStatus(WriteOffStatus.PENDING_CONFIRMATION);

            when(writeOffPersistencePort.findById(writeOffId)).thenReturn(Optional.of(sampleWriteOff));
            doThrow(new InvoiceNotFoundException("Invoice 99 not found"))
                .when(sampleWriteOff).processConfirmation(any());

            // ACT & ASSERT
            assertThatThrownBy(() -> portfolioWriteOffService.confirmWriteOff(writeOffId))
                .isInstanceOf(InvoiceNotFoundException.class);

            verify(invoiceProviderPort, never()).updateInvoice(any());
            verify(writeOffPersistencePort, never()).update(any());
            verify(accountingEventPublisher, never()).publishWriteOffConfirmedEvent(any());
        }

        @Test
        @DisplayName("Debe propagar excepciones de persistencia si la actualización falla al confirmar")
        void shouldPropagatePersistenceExceptionWhenUpdateFails() {
            // ARRANGE
            Long writeOffId = 4L;
            sampleWriteOff.setId(writeOffId);
            sampleWriteOff.setStatus(WriteOffStatus.PENDING_CONFIRMATION);

            InvoiceReplica mockInvoice = mock(InvoiceReplica.class);
            List<InvoiceReplica> modifiedInvoices = List.of(mockInvoice);

            when(writeOffPersistencePort.findById(writeOffId)).thenReturn(Optional.of(sampleWriteOff));
            doReturn(modifiedInvoices).when(sampleWriteOff).processConfirmation(any());
            when(writeOffPersistencePort.update(sampleWriteOff)).thenThrow(new RuntimeException("DB error"));

            // ACT & ASSERT
            assertThatThrownBy(() -> portfolioWriteOffService.confirmWriteOff(writeOffId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB error");

            verify(invoiceProviderPort).updateInvoice(mockInvoice);
            verify(writeOffPersistencePort).update(sampleWriteOff);
            verify(accountingEventPublisher, never()).publishWriteOffConfirmedEvent(any());
        }
    }

    @Nested
    @DisplayName("Caso de Uso: Anular Castigo Confirmado")
    class VoidConfirmedWriteOffTests { 

        @Test
        @DisplayName("Debe anular un castigo confirmado exitosamente")
        void shouldVoidConfirmedWriteOffSuccessfully() { 
            // ARRANGE
            Long writeOffId = 2L;
            sampleWriteOff.setId(writeOffId);
            sampleWriteOff.setStatus(WriteOffStatus.CONFIRMED);

            InvoiceReplica mockInvoice = mock(InvoiceReplica.class);
            List<InvoiceReplica> modifiedInvoices = List.of(mockInvoice);

            when(writeOffPersistencePort.findById(writeOffId)).thenReturn(Optional.of(sampleWriteOff));
            doReturn(modifiedInvoices).when(sampleWriteOff).processVoidance(any());
            when(writeOffPersistencePort.update(sampleWriteOff)).thenReturn(sampleWriteOff);

            // ACT
            PortfolioWriteOff result = portfolioWriteOffService.voidWriteOffConfirmation(writeOffId);

            // ASSERT
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(WriteOffStatus.VOIDED);

            // VERIFY
            var inOrder = inOrder(writeOffPersistencePort, invoiceProviderPort, accountingEventPublisher, sampleWriteOff);
            inOrder.verify(writeOffPersistencePort).findById(writeOffId); // 1. Buscar el castigo 
            inOrder.verify(sampleWriteOff).voidConfirmation(); // 2. Llamar a la lógica de dominio para cambiar el estado
            inOrder.verify(sampleWriteOff).processVoidance(any()); // 3. Llamar a la lógica de dominio para procesar las facturas
            inOrder.verify(invoiceProviderPort).updateInvoice(mockInvoice); // 4. Actualizar cada factura 
            inOrder.verify(writeOffPersistencePort).update(sampleWriteOff); // 5. Persistir el castigo actualizado 
            inOrder.verify(accountingEventPublisher).publishWriteOffVoidedEvent(sampleWriteOff); // 6. Publicar el evento 
        }

        @Test
        @DisplayName("Debe lanzar PortfolioWriteOffNotFoundException si el castigo a anular no existe")
        void shouldThrowNotFoundExceptionWhenVoidingNonExistentWriteOff() {
            // ARRANGE
            Long nonExistentId = 99L;
            when(writeOffPersistencePort.findById(nonExistentId)).thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> portfolioWriteOffService.voidWriteOffConfirmation(nonExistentId))
                    .isInstanceOf(PortfolioWriteOffNotFoundException.class)
                    .hasMessageContaining("no fue encontrado");

            // VERIFY
            // Nos aseguramos de que no se intentó hacer ninguna otra operación.
            verify(invoiceProviderPort, never()).updateInvoice(any());
            verify(accountingEventPublisher, never()).publishWriteOffVoidedEvent(any());
        }
    }

    @Nested
    @DisplayName("Caso de Uso: Crear Castigo")
    class CreateWriteOffTests {

        @Test
        @DisplayName("Debe crear un castigo, actualizar facturas y persistir")
        void shouldCreateWriteOffSuccessfully() {
            // ARRANGE
            InvoiceReplica mockInvoice = mock(InvoiceReplica.class);
            List<InvoiceReplica> modifiedInvoices = List.of(mockInvoice);

            // Usamos doReturn().when() para espiar un objeto real.
            doReturn(modifiedInvoices).when(sampleWriteOff).prepareInvoicesForWriteOff(any());
            when(writeOffPersistencePort.save(sampleWriteOff)).thenReturn(sampleWriteOff);

            // ACT
            PortfolioWriteOff result = portfolioWriteOffService.createWriteOff(sampleWriteOff);

            // ASSERT
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isNotNull().startsWith("CC-");

            // Verify
            verify(sampleWriteOff).prepareInvoicesForWriteOff(any());
            verify(invoiceProviderPort).updateInvoice(mockInvoice);
            verify(writeOffPersistencePort).save(sampleWriteOff);
        }

        @Test
        @DisplayName("Debe notificar el uso de recursos al crear un castigo")
        void shouldNotifyResourceUsageWhenCreatingWriteOff() {
            // ARRANGE
            InvoiceReplica mockInvoice = mock(InvoiceReplica.class);
            List<InvoiceReplica> modifiedInvoices = List.of(mockInvoice);

            doReturn(modifiedInvoices).when(sampleWriteOff).prepareInvoicesForWriteOff(any());
            when(writeOffPersistencePort.save(sampleWriteOff)).thenReturn(sampleWriteOff);

            // ACT
            portfolioWriteOffService.createWriteOff(sampleWriteOff);

            // ASSERT
            verify(resourceUsageNotifier).notifyAll(any());
        }

        @Test
        @DisplayName("Debe generar código único que comienza con CC-")
        void shouldGenerateUniqueCodeStartingWithCC() {
            // ARRANGE
            var writeOff1 = new PortfolioWriteOff();
            var writeOff2 = new PortfolioWriteOff();
            PortfolioWriteOff spyWriteOff1 = spy(writeOff1);
            PortfolioWriteOff spyWriteOff2 = spy(writeOff2);
            
            InvoiceReplica mockInvoice = mock(InvoiceReplica.class);
            List<InvoiceReplica> modifiedInvoices = List.of(mockInvoice);

            doReturn(modifiedInvoices).when(spyWriteOff1).prepareInvoicesForWriteOff(any());
            doReturn(modifiedInvoices).when(spyWriteOff2).prepareInvoicesForWriteOff(any());
            when(writeOffPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // ACT
            PortfolioWriteOff result1 = portfolioWriteOffService.createWriteOff(spyWriteOff1);
            
            // Simular una pequeña pausa para asegurar timestamp diferente
            try { Thread.sleep(5); } catch (InterruptedException e) {}
            
            PortfolioWriteOff result2 = portfolioWriteOffService.createWriteOff(spyWriteOff2);

            // ASSERT
            assertThat(result1.getCode()).startsWith("CC-");
            assertThat(result2.getCode()).startsWith("CC-");
            // Los códigos deben ser diferentes debido al timestamp
            assertThat(result1.getCode()).isNotEqualTo(result2.getCode());
        }

        @Test
        @DisplayName("Debe propagar excepciones de persistencia al crear")
        void shouldPropagateExceptionWhenSavingWriteOff() {
            // ARRANGE
            InvoiceReplica mockInvoice = mock(InvoiceReplica.class);
            List<InvoiceReplica> modifiedInvoices = List.of(mockInvoice);

            doReturn(modifiedInvoices).when(sampleWriteOff).prepareInvoicesForWriteOff(any());
            when(writeOffPersistencePort.save(sampleWriteOff)).thenThrow(new RuntimeException("DB error"));

            // ACT & ASSERT
            assertThatThrownBy(() -> portfolioWriteOffService.createWriteOff(sampleWriteOff))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB error");

            verify(invoiceProviderPort).updateInvoice(mockInvoice);
            verify(resourceUsageNotifier).notifyAll(any());
        }
    }

    @Nested
    @DisplayName("Consultas: Querys de PortfolioWriteOffService")
    class QueryTests {

        @Test
        @DisplayName("findById debe devolver el castigo si existe")
        void shouldReturnWriteOffWhenFindByIdExists() {
            // ARRANGE
            Long writeOffId = 10L;
            var sample = TestFixtures.portfolioWriteOffWithDetails("ENT-1", 1L, "just", List.of(TestFixtures.writeOffDetail(1L, 100L)));
            sample.setId(writeOffId);

            when(writeOffPersistencePort.findById(writeOffId)).thenReturn(Optional.of(sample));

            // ACT
            PortfolioWriteOff result = portfolioWriteOffService.findById(writeOffId);

            // ASSERT
            assertThat(result).isEqualTo(sample);
            verify(writeOffPersistencePort).findById(writeOffId);
        }

        @Test
        @DisplayName("findById debe lanzar PortfolioWriteOffNotFoundException si no existe")
        void shouldThrowWhenFindByIdNotExists() {
            // ARRANGE
            Long missingId = 999L;
            when(writeOffPersistencePort.findById(missingId)).thenReturn(Optional.empty());

            // ACT/ASSERT
            assertThatThrownBy(() -> portfolioWriteOffService.findById(missingId))
                    .isInstanceOf(PortfolioWriteOffNotFoundException.class)
                    .hasMessageContaining("no fue encontrado");

            verify(writeOffPersistencePort).findById(missingId);
        }

        @Test
        @DisplayName("findByEnterpriseId debe delegar en el puerto de persistencia y devolver la lista")
        void shouldReturnListWhenFindByEnterpriseId() {
            // ARRANGE
            String entId = "ENT-42";
            var w1 = TestFixtures.portfolioWriteOffWithDetails(entId, 1L, "j", List.of(TestFixtures.writeOffDetail(1L, 100L)));
            var w2 = TestFixtures.portfolioWriteOffWithDetails(entId, 2L, "j2", List.of(TestFixtures.writeOffDetail(2L, 200L)));
            List<PortfolioWriteOff> list = List.of(w1, w2);

            when(writeOffPersistencePort.findByEnterpriseId(entId)).thenReturn(list);

            // ACT
            List<PortfolioWriteOff> result = portfolioWriteOffService.findByEnterpriseId(entId);

            // ASSERT
            assertThat(result).isNotNull().hasSize(2).containsExactlyElementsOf(list);
            verify(writeOffPersistencePort).findByEnterpriseId(entId);
        }
    }
}

