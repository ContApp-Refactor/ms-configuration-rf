package co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.adapter;

import co.unicauca.edu.co.contables.accounting.debt_payments.domain.model.Receipt;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.adapter.ReceiptPersistenceAdapter;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.entity.ReceiptEntity;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.mapper.IReceiptPersistenceMapper;
import co.unicauca.edu.co.contables.accounting.debt_payments.infraestructure.output.jpa.repository.IReceiptRepository;
import co.unicauca.edu.co.contables.accounting.debt_payments.test.fixtures.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests for ReceiptPersistenceAdapter")
public class ReceiptPersistenceAdapterUnitTest {

    @Mock
    private IReceiptRepository receiptRepository;
    @Mock
    private IReceiptPersistenceMapper receiptMapper;

    @InjectMocks
    private ReceiptPersistenceAdapter adapter;

    @Test
    void saveShouldMapAndPersist() {
        Receipt domain = TestFixtures.receiptForDirectIncome("ENT-1", 1L, 1L, "o", 1000L, 200L);
        ReceiptEntity entity = new ReceiptEntity();

        when(receiptMapper.toEntity(domain)).thenReturn(entity);
        when(receiptRepository.save(entity)).thenReturn(entity);
        when(receiptMapper.toDomain(entity)).thenReturn(domain);

        Receipt out = adapter.save(domain);

        assertThat(out).isEqualTo(domain);
        verify(receiptRepository).save(entity);
    }

    @Test
    void findByIdShouldReturnMappedDomain() {
        ReceiptEntity entity = new ReceiptEntity();
        Receipt domain = TestFixtures.receiptForDirectIncome("ENT-1", 1L, 1L, "o", 100L, 10L);

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(receiptMapper.toDomain(entity)).thenReturn(domain);

        Optional<Receipt> out = adapter.findById(1L);

        assertThat(out).isPresent().contains(domain);
    }

    @Test
    void findListsShouldDelegateToMapper() {
        ReceiptEntity e1 = new ReceiptEntity();
        Receipt r1 = TestFixtures.receiptForDirectIncome("ENT-1", 1L, 1L, "o", 100L, 10L);

        when(receiptRepository.findByInvoiceId(anyLong())).thenReturn(List.of(e1));
        when(receiptMapper.toDomainList(List.of(e1))).thenReturn(List.of(r1));

        var byInvoice = adapter.findByInvoiceId("1");
        assertThat(byInvoice).containsExactly(r1);

        when(receiptRepository.findByThirdPartyIdAndEnterpriseId(anyLong(), eq("ENT-1"))).thenReturn(List.of(e1));
        when(receiptMapper.toDomainList(List.of(e1))).thenReturn(List.of(r1));

        var byThird = adapter.findByThirdPartyId("1", "ENT-1");
        assertThat(byThird).containsExactly(r1);

        when(receiptRepository.findAllByEnterpriseId(eq("ENT-1"))).thenReturn(List.of(e1));
        when(receiptMapper.toDomainList(List.of(e1))).thenReturn(List.of(r1));

        var byEnt = adapter.findByEnterpriseId("ENT-1");
        assertThat(byEnt).containsExactly(r1);
    }

    @Test
    void existsByIdDelegates() {
        when(receiptRepository.existsById(5L)).thenReturn(true);
        boolean exists = adapter.existsById(5L);
        assertThat(exists).isTrue();
    }
}

