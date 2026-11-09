package co.unicauca.edu.co.contables.inventory.kardex.unit.application.kardex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import co.unicauca.edu.co.contables.inventory.kardex.application.service.kardex.query.KardexQueryService;
import co.unicauca.edu.co.contables.inventory.kardex.domain.model.MovementType;
import co.unicauca.edu.co.contables.inventory.kardex.domain.port.kardex.IKardexQueryRepositoryPort;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.adapter.KardexQueryAdapter;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.entity.KardexEntity;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.mapper.IKardexEntityQueryMapper;
import co.unicauca.edu.co.contables.inventory.kardex.infrastructure.adapters.output.jpa.repository.IKardexRepository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Ultimo precio de entrada (promedio ponderado): el servicio pide SOLO PURCHASE y ADJUSTMENTENTRY y el
 * adaptador toma la fila mas reciente (fecha desc, id desc) que devuelve el repositorio. La seleccion
 * real contra la base (ignora ventas, devoluciones, entradas no comerciales; empate por fecha) se
 * verifica en {@code BillingInventoryStockIntegrationTest} con H2.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Kardex WA - ultimo precio de entrada")
class KardexLastEntryUnitPriceUnitTest {

    @Mock
    private IKardexQueryRepositoryPort repositoryPort;
    @Mock
    private IKardexRepository kardexRepository;
    @Mock
    private IKardexEntityQueryMapper mapper;

    @Test
    @DisplayName("el servicio solo considera PURCHASE y ADJUSTMENTENTRY")
    void service_asks_only_for_purchase_and_adjustment_entry() {
        KardexQueryService service = new KardexQueryService(repositoryPort);
        when(repositoryPort.findLastEntryUnitPrice(any(), any())).thenReturn(Optional.of(new BigDecimal("120.00")));

        Optional<BigDecimal> result = service.findLastEntryUnitPrice(7L);

        assertEquals(new BigDecimal("120.00"), result.orElseThrow());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<MovementType>> types = ArgumentCaptor.forClass(Collection.class);
        verify(repositoryPort).findLastEntryUnitPrice(org.mockito.ArgumentMatchers.eq(7L), types.capture());
        assertEquals(Set.of(MovementType.PURCHASE, MovementType.ADJUSTMENTENTRY), Set.copyOf(types.getValue()));
        verifyNoMoreInteractions(repositoryPort);
    }

    @Test
    @DisplayName("el servicio devuelve vacio cuando no hay entradas")
    void service_returns_empty_when_there_are_no_entries() {
        KardexQueryService service = new KardexQueryService(repositoryPort);
        when(repositoryPort.findLastEntryUnitPrice(any(), any())).thenReturn(Optional.empty());

        assertTrue(service.findLastEntryUnitPrice(7L).isEmpty());
    }

    @Test
    @DisplayName("el adaptador devuelve el unitPrice de la fila mas reciente")
    void adapter_returns_unit_price_of_the_latest_matching_row() {
        KardexQueryAdapter adapter = new KardexQueryAdapter(mapper, kardexRepository);
        List<MovementType> types = List.of(MovementType.PURCHASE, MovementType.ADJUSTMENTENTRY);
        KardexEntity latest = new KardexEntity();
        latest.setId(9L);
        latest.setType(MovementType.ADJUSTMENTENTRY);
        latest.setUnitPrice(new BigDecimal("130.50"));
        when(kardexRepository.findTopByProductIdAndTypeInOrderByDateDescIdDesc(7L, types))
                .thenReturn(Optional.of(latest));

        Optional<BigDecimal> result = adapter.findLastEntryUnitPrice(7L, types);

        assertEquals(new BigDecimal("130.50"), result.orElseThrow());
    }

    @Test
    @DisplayName("el adaptador devuelve vacio sin filas o con unitPrice nulo")
    void adapter_returns_empty_without_rows_or_with_null_price() {
        KardexQueryAdapter adapter = new KardexQueryAdapter(mapper, kardexRepository);
        List<MovementType> types = List.of(MovementType.PURCHASE, MovementType.ADJUSTMENTENTRY);
        when(kardexRepository.findTopByProductIdAndTypeInOrderByDateDescIdDesc(7L, types))
                .thenReturn(Optional.empty());
        KardexEntity noPrice = new KardexEntity();
        noPrice.setType(MovementType.PURCHASE);
        when(kardexRepository.findTopByProductIdAndTypeInOrderByDateDescIdDesc(8L, types))
                .thenReturn(Optional.of(noPrice));

        assertTrue(adapter.findLastEntryUnitPrice(7L, types).isEmpty());
        assertTrue(adapter.findLastEntryUnitPrice(8L, types).isEmpty());
    }

    @Test
    @DisplayName("promedio vigente: el servicio delega en el puerto de repositorio")
    void service_delegates_current_average_cost() {
        KardexQueryService service = new KardexQueryService(repositoryPort);
        when(repositoryPort.findCurrentAverageUnitCost(7L)).thenReturn(Optional.of(new BigDecimal("115.00")));

        assertEquals(new BigDecimal("115.00"), service.findCurrentAverageUnitCost(7L).orElseThrow());
        verify(repositoryPort).findCurrentAverageUnitCost(7L);
        verifyNoMoreInteractions(repositoryPort);
    }

    @Test
    @DisplayName("promedio vigente: el adaptador devuelve balanceUnitPrice de la fila mas reciente con balance > 0")
    void adapter_returns_balance_unit_price_of_latest_row_with_positive_balance() {
        KardexQueryAdapter adapter = new KardexQueryAdapter(mapper, kardexRepository);
        KardexEntity latest = new KardexEntity();
        latest.setId(9L);
        latest.setBalanceUnitPrice(new BigDecimal("116.67"));
        when(kardexRepository.findTopByProductIdAndBalanceUnitPriceGreaterThanOrderByDateDescIdDesc(7L, BigDecimal.ZERO))
                .thenReturn(Optional.of(latest));
        when(kardexRepository.findTopByProductIdAndBalanceUnitPriceGreaterThanOrderByDateDescIdDesc(8L, BigDecimal.ZERO))
                .thenReturn(Optional.empty());

        assertEquals(new BigDecimal("116.67"), adapter.findCurrentAverageUnitCost(7L).orElseThrow());
        assertTrue(adapter.findCurrentAverageUnitCost(8L).isEmpty());
    }
}
